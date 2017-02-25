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
    private int size;
    private Node root;

    private static class Node {
        private Point2D p;      // the point
        private RectHV rect;    // the axis-aligned rectangle corresponding to this node
        private Node left;        // the left/bottom subtree
        private Node right;        // the right/top subtree
        private boolean isVertical;

        public Node(Point2D p, Shape s) {
            this.p          = p;
            this.isVertical = s.isVertical;

            this.rect = s.isVertical
                ? new RectHV(p.x(), s.bottom, p.x(), s.top)
                : new RectHV(s.left, p.y(), s.right, p.y());
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
        if (tree == null) return new Node(p, s);

        boolean isVertical = tree.isVertical;
        boolean isSmaller  = isSmaller(tree, p);

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
        return contains(root, p);
    }

    private boolean contains(Node parent, Point2D p) {
        Node curr = parent;

        while (curr != null) {
            if (p.equals(curr.p))   return true;
            if (isSmaller(curr, p)) curr = curr.left;
            else                    curr = curr.right;
        }

        return false;
    }

    private boolean isSmaller(Node node, Point2D p) {
        return node.isVertical
            ? node.p.x() > p.x()
            : node.p.y() > p.y();
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
        SET<Point2D> result = new SET<Point2D>();

        range(rect, root, result, new RectHV(0.0, 0.0, 1.0, 1.0));

        return result;
    }


    private void range(RectHV queryRect, Node tree, SET<Point2D> set, RectHV nodeRect) {
        if (tree == null || !queryRect.intersects(nodeRect)) return;

        if (queryRect.contains(tree.p)) set.add(tree.p);

        RectHV leftRect;
        RectHV rightRect;

        if (tree.isVertical) {
            leftRect  = new RectHV(nodeRect.xmin(), nodeRect.ymin(), tree.p.x(), nodeRect.ymax());
            rightRect = new RectHV(tree.p.x(), nodeRect.ymin(), nodeRect.xmax(), nodeRect.ymax());
        } else {
            leftRect  = new RectHV(nodeRect.xmin(), nodeRect.ymin(), nodeRect.xmax(), tree.p.y());
            rightRect = new RectHV(nodeRect.xmin(), tree.p.y(), nodeRect.xmax(), nodeRect.ymax());
        }
        range(queryRect, tree.left,  set, leftRect);
        range(queryRect, tree.right, set, rightRect);
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

        RectHV rect = new RectHV(0.0, 0.0, 1.0, 1.0);

        StdDraw.enableDoubleBuffering();

        KdTree kdtree = new KdTree();
        // SET<Point2D> ps = new SET<Point2D>();

        // SET<Point2D> result = new SET<Point2D>();

        while (!in.isEmpty()) {
            double x = in.readDouble();
            double y = in.readDouble();

            Point2D p = new Point2D(x, y);

            StdOut.printf("%8.6f %8.6f\n", x, y);

            if (rect.contains(p)) {
                kdtree.insert(p);
                StdDraw.clear();
                kdtree.draw();
                StdDraw.show();
            }
            StdDraw.pause(200);
        }

        //  while (!in.isEmpty()) {
        //     double x = in.readDouble();
        //     double y = in.readDouble();

        //     Point2D p = new Point2D(x, y);
        //     kdtree.insert(p);
        // }

        // RectHV search = new RectHV(0, 0, 0.5, 0.7);

        // for (Point2D p : kdtree.range(search)) {
        //     StdOut.println(p);
        // }


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
