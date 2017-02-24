/******************************************************************************
 *  Compilation:  javac KdTree.java
 *  Execution:    java KdTree
 *  Dependencies: Point2D, RectHV, SET
 *
 *  a data type to represent a set of points in the unit square
 *  (all points have x- and y-coordinates between 0 and 1)
 *
 *
 *  Brute-force implementation:
 *  Implement the API by using a red-black BST (SET)
 *
 ******************************************************************************/

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;
import java.awt.Color;
import edu.princeton.cs.algs4.SET;
import edu.princeton.cs.algs4.StdOut;
import edu.princeton.cs.algs4.In;


public class KdTree {
    private SET<Point2D> points;
    private int size;
    private Node root;

    private static class Node {
        private Point2D p;      // the point
        private RectHV rect;    // the axis-aligned rectangle corresponding to this node
        private Node left;        // the left/bottom subtree
        private Node right;        // the right/top subtree
        private boolean isVertical;

        public Node(Point2D p, Shape s, boolean isSmaller) {
            // boolean hasParent  = parent != null;
            // boolean isVertical = s.isVertical;
            // boolean isVertical = !hasParent || !s.isVertical;

            // if (!hasParent) StdOut.println(" has no parent!!" + p);
            // else {
            //     StdOut.println(" has parent!!" + p + " and " + parent.p);
            //     String msg1 = isVertical ? "vertical" : "horizontal";
            //     String msg2 = isSmaller ? "(smaller) left or bottom" : "(bigger) right or top";
            //     StdOut.println(msg1 + "\n" + msg2);
            // }
            // double cord       = isVertical ? p.x() : p.y();
            // double parentCord = 0;
            // if (hasParent)
            //     parentCord    = isVertical ? parent.p.y() : parent.p.x();

            this.p          = p;
            this.isVertical = s.isVertical;
            // this.rect = new RectHV(s.left, s.bottom, s.right, s.top);

            this.rect = s.isVertical
                ? new RectHV(p.x(), s.bottom, p.x(), s.top)
                : new RectHV(s.left, p.y(), s.right, p.y());

            // if (isSmaller) {
                // this.rect = s.isVertical
                //     ? new RectHV(p.x(), s.bottom, p.x(), s.top)
                //     : new RectHV(s.left, p.y(), s.right, p.y());
            // } else {
            //     this.rect = s.isVertical
            //         ? new RectHV(p.x(), s.bottom, p.x(), s.top)
            //         : new RectHV(s.left, p.y(), s.right, p.y());
            // }

            // if (s.right - s.left - 1 < 0.000001) this.rect = new RectHV(s.left, s.bottom, s.right, s.top);
            // if (hasParent) {
                // if (isSmaller) {
                //     this.rect = isVertical
                //         ? new RectHV(p.x(), 0, p.x(), parent.p.y())
                //         : new RectHV(0, p.y(), parent.p.x(), p.y());
                // } else {
                //     this.rect = isVertical
                //         ? new RectHV(p.x(), parent.p.y(), p.x(), 1)
                //         : new RectHV(parent.p.x(), p.y(), 1, p.y());
                // }
            // } else {
            //     this.rect = new RectHV(cord, 0, cord, 1.0);
            // }
        }
    }

    private static class Shape {
        private double top;
        private double right;
        private double bottom;
        private double left;
        private boolean isVertical;
        public Shape(double top, double right, double bottom, double left, boolean isVertical) {
            this.top        = top;
            this.right      = right;
            this.bottom     = bottom;
            this.left       = left;
            this.isVertical = isVertical;
        }
    }


    /**
     * construct an empty set of points
     */
    public KdTree() {
        size = 0;
    }

    /**
     * is the set empty?
     *
     * @return boolean of whether it's empty
     */
    public boolean isEmpty() { return size == 0; }

    /**
     * number of points in the set
     *
     * @return number of points in the set
     */
    public int size() { return size; }

    /**
     * add the point to the set
     * (if it is not already in the set)
     *
     * @param <Point2D> p - point to insert
     */
    public void insert(Point2D p) {
        if (p == null) throw new NullPointerException();
        if (contains(p)) return;

        Shape s = new Shape(1.0, 1.0, 0.0, 0.0, true);
        root = insert(p, root, s, false);
        ++size;
    }

    private Node insert(Point2D p, Node tree, Shape s, boolean shouldGoLeft) {
        if (tree == null) return new Node(p, s, shouldGoLeft);

        boolean isVertical = tree.isVertical;
        boolean isSmaller  = isVertical
            ? tree.p.x() > p.x()
            : tree.p.y() > p.y();

        s.isVertical = !isVertical;
        if (isVertical) {
            if (isSmaller) s.right  = tree.p.x();
            else           s.left   = tree.p.x();
        } else {
            if (isSmaller) s.top    = tree.p.y();
            else           s.bottom = tree.p.y();
        }


        // NOTE: No equal cases
        if (isSmaller) tree.left  = insert(p, tree.left,  s, isSmaller);
        else           tree.right = insert(p, tree.right, s, isSmaller);

        return tree;
    }

    /**
     * does the set contain point p?
     *
     * @param <Point2D> p - point to test
     * @return boolean of whether the set contain point p
     */
    public boolean contains(Point2D p) {
        if (p == null) throw new NullPointerException();
        return find(root, p) != null;
    }

    private Node find(Node parent, Point2D p) {
        Node curr = parent;

        while (curr != null) {
            int cmp = p.compareTo(curr.p);

            if      (cmp < 0) curr = curr.left;
            else if (cmp > 0) curr = curr.right;
            else              return curr;
        }

        return null;
    }

    /**
     * draw all points to standard draw
     */
    public void draw() {
        draw(root);
    }

    private void draw(Node curr) {
        if (curr == null) return;

        prepareDrawLine(curr.isVertical);
        curr.rect.draw();
        prepareDrawPoint();
        curr.p.draw();

        draw(curr.left);
        draw(curr.right);
    }

    private void prepareDrawPoint() {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.01);
    }

    private void prepareDrawLine(boolean isVertical) {
        Color color = isVertical
            ? StdDraw.RED
            : StdDraw.BLUE;
        StdDraw.setPenColor(color);
        StdDraw.setPenRadius();
    }

    /**
     * all points that are inside the rectangle
     *
     * @param <RectHV> rect - rectangle to test
     * @return <Iterable<Point2D>> all points in the rectangle
     */
    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) throw new NullPointerException();
        return search(rect, root);
    }

    private SET<Point2D> search(RectHV rect, Node tree) {
        SET<Point2D> result = new SET<Point2D>();

        if (tree == null) return result;

        if (rect.contains(tree.p)) result.add(tree.p);
        // Default search the left tree
        result = result.union(search(rect, tree.left));

        // If intersect, search the right tree
        if (rect.intersects(tree.rect))
            result = result.union(search(rect, tree.right));

        return result;
    }

    /**
     * a nearest neighbor in the set to point p
     *
     * @param <Point2D> target - point to test the nearest
     * @return <Point2D> the nearest point; null if the set is empty
     */
    public Point2D nearest(Point2D p) {
        if (p == null) throw new NullPointerException();

        if (size == 0) return null;
        if (root.p.equals(p)) return root.p;

        return nearest(p, root, p.distanceSquaredTo(root.p));
    }

    private Point2D nearest(Point2D p, Node tree, double min) {
        Point2D closest = tree.p;
        Point2D mayCloser;
        double dist;

        // Could have closer in left tree
        if (tree.left != null) {
            dist = p.distanceSquaredTo(tree.left.p);
            if (dist < min || tree.left.left != null) {
                mayCloser = nearest(p, tree.left, Math.min(dist, min));
                if (mayCloser.distanceSquaredTo(p) < dist) closest = mayCloser;
            }
        }

        // Could have closer in right tree
        if (tree.right != null) {
            dist = p.distanceSquaredTo(tree.right.p);
            if (dist < min || !tree.isVertical) {
                mayCloser = nearest(p, tree.right, Math.min(dist, min));
                if (mayCloser.distanceSquaredTo(p) < dist) closest = mayCloser;
                closest = nearest(p, tree.right, Math.min(dist, min));
            }
        }

        return closest;
    }

    public static void main(String[] args) {
        String filename = args[0];
        In in = new In(filename);

        // RectHV rect = new RectHV(0.0, 0.0, 1.0, 1.0);

        StdDraw.enableDoubleBuffering();

        KdTree kdtree = new KdTree();
        // SET<Point2D> ps = new SET<Point2D>();

        // SET<Point2D> result = new SET<Point2D>();

        // while (!in.isEmpty()) {
        //     double x = in.readDouble();
        //     double y = in.readDouble();

        //     Point2D p = new Point2D(x, y);

        //     StdOut.printf("%8.6f %8.6f\n", x, y);

        //     if (rect.contains(p)) {
        //         kdtree.insert(p);
        //         StdDraw.clear();
        //         kdtree.draw();
        //         StdDraw.show();
        //     }
        //     StdDraw.pause(200);
        // }

         while (!in.isEmpty()) {
            double x = in.readDouble();
            double y = in.readDouble();

            Point2D p = new Point2D(x, y);
            // kdtree.insert(p);
        }

        RectHV search = new RectHV(0, 0, 0.5, 0.7);

        for (Point2D p : kdtree.range(search)) {
            StdOut.println(p);
        }


        // for (Point2D p : ps) {
        //     StdOut.println(" --- current point: " + p);
        //     boolean has = kdtree.contains(p);

        //     StdOut.println(" has " + p + " ? :" + has);
        // }



        StdOut.println(" size " + kdtree.size());


        // kdtree.draw();
        // StdDraw.show();
    }

}
