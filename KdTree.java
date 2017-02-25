/******************************************************************************
 *  Compilation:  javac KdTree.java
 *  Execution:    java KdTree
 *  Dependencies: Point2D, RectHV, SET, StdDraw
 *
 *  a data type to represent a set of points in the unit square
 *  (all points have x- and y-coordinates between 0 and 1)
 *
 *
 *  time complexity: log(n)
 *
 ******************************************************************************/

import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.SET;
import java.awt.Color;


public class KdTree {
    private int  size;
    private Node root;

    private static class Node {
        private Point2D p;          // the point
        private RectHV  rect;       // the axis-aligned rectangle corresponding to this node
        private Node    left;       // the left/bottom subtree
        private Node    right;      // the right/top subtree
        private boolean isVertical;

        public Node(Point2D p, Shape s) {
            this.p          = p;
            this.isVertical = s.isVertical;
            this.rect       = s.isVertical
                ? new RectHV(p.x(), s.bottom, p.x(), s.top)
                : new RectHV(s.left, p.y(), s.right, p.y());
        }
    }

    // Helper class to construct 'Node'
    private static class Shape {
        private double  top;
        private double  right;
        private double  bottom;
        private double  left;
        private boolean isVertical;
        public Shape(double top, double right, double bottom, double left, boolean isVertical) {
            this.top        = top;
            this.right      = right;
            this.bottom     = bottom;
            this.left       = left;
            this.isVertical = isVertical;
        }
    }

    // Ctor
    public KdTree() { size = 0; }

    /**
     * is the tree empty?
     *
     * @return boolean of whether it's empty
     */
    public boolean isEmpty() { return size == 0; }

    /**
     * number of points in the tree
     *
     * @return number of points in the tree
     */
    public int size() { return size; }

    /**
     * add the point to the tree
     * (if the tree doesn't contain the point)
     *
     * @param <Point2D> p - point to insert
     */
    public void insert(Point2D p) {
        if (p == null) throw new NullPointerException();
        if (contains(p)) return;

        Shape shape = new Shape(1.0, 1.0, 0.0, 0.0, true);
        root = insert(p, root, shape);
        ++size;
    }

    /**
     * Recursive method to insert Node
     *
     * @param <Point2D> p - point to insert
     * @param <Node> tree - current subtree
     * @param <Shape> shape - current available area to construct the node
     * @return new appended Node
     */
    private Node insert(Point2D p, Node tree, Shape shape) {
        if (tree == null) return new Node(p, shape);

        boolean isVertical = tree.isVertical;
        boolean isSmaller  = isSmaller(tree, p);

        shape.isVertical = !isVertical;
        if (isVertical) {
            if (isSmaller) shape.right  = tree.p.x();
            else           shape.left   = tree.p.x();
        } else {
            if (isSmaller) shape.top    = tree.p.y();
            else           shape.bottom = tree.p.y();
        }

        // NOTE: No equal cases
        if (isSmaller) tree.left  = insert(p, tree.left,  shape);
        else           tree.right = insert(p, tree.right, shape);

        return tree;
    }

    /**
     * does the kd-tree contain point p?
     *
     * @param <Point2D> p - point to test
     * @return boolean of whether point p in the kd-tree
     */
    public boolean contains(Point2D p) {
        if (p == null) throw new NullPointerException();
        return contains(root, p);
    }

    /**
     * Recursive method to traverse the tree
     *
     * @param <Node> parent - current subtree
     * @param <Point2D> p - point to test
     * @return boolean of whether point p in the kd-tree
     */
    private boolean contains(Node parent, Point2D p) {
        Node node = parent;

        while (node != null) {
            if (p.equals(node.p))   return true;
            if (isSmaller(node, p)) node = node.left;
            else                    node = node.right;
        }

        return false;
    }

    /**
     * draw all points to standard draw
     */
    public void draw() {
        draw(root);
    }

    /**
     * draw all points to standard draw
     *
     * @param <Node> root tree to draw
     */
    private void draw(Node curr) {
        if (curr == null) return;

        prepareDrawLine(curr.isVertical);
        curr.rect.draw();
        prepareDrawPoint();
        curr.p.draw();

        draw(curr.left);
        draw(curr.right);
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

    /**
     * Recursive method to add points according to NodeRect
     *
     * @param <RectHV> queryRect - rectangle to test
     * @param <Node> tree - subtree for search
     * @param <SET<Point2D>> all points inside the rectangle
     * @param <RectHV> nodeRect - rectangle corresponding to parentNode
     */
    private void range(RectHV queryRect, Node tree, SET<Point2D> set, RectHV nodeRect) {
        if (tree == null || !queryRect.intersects(nodeRect)) return;

        if (queryRect.contains(tree.p)) set.add(tree.p);

        RectHV leftRect  = getNodeRect(tree, nodeRect, true);
        RectHV rightRect = getNodeRect(tree, nodeRect, false);

        range(queryRect, tree.left,  set, leftRect);
        range(queryRect, tree.right, set, rightRect);
    }

    /**
     * the nearest neighbor to the query point p
     *
     * @param <Point2D> p - point to test
     * @return <Point2D> the nearest point; null if the tree is empty
     */
    public Point2D nearest(Point2D p) {
        if (p == null) throw new NullPointerException();
        return nearest(p, root, Double.MAX_VALUE, new RectHV(0.0, 0.0, 1.0, 1.0));
    }

    /**
     * Recursive method to find the nearest neighbor to the query point p
     *
     * Pruning rule: a node is searched only if it might contain a point
     *               that is closer than the best one found so far
     *
     * @param <Point2D> p - point to test
     * @param <Node> tree - current subtree
     * @param <double> minDist - current minimum distance
     * @param <RectHV> nodeRect - rectangle corresponding to parentNode
     * @return <Point2D> the nearest point of current subtree and its subtrees
     */
    private Point2D nearest(Point2D p, Node tree, double minDist, RectHV nodeRect) {
        if (tree == null || minDist < nodeRect.distanceSquaredTo(p)) return null;

        Point2D closestPoint = null;
        Point2D mayCloser;

        double dist = p.distanceSquaredTo(root.p);
        if (dist < minDist) {
            minDist      = dist;
            closestPoint = tree.p;
        }

        Node searchNode1, searchNode2;
        RectHV nodeRect1, nodeRect2;

        if (isSmaller(tree, p)) {
            searchNode1 = tree.left;
            searchNode2 = tree.right;
            nodeRect1   = getNodeRect(tree, nodeRect, true);
            nodeRect2   = getNodeRect(tree, nodeRect, false);
        } else {
            searchNode1 = tree.right;
            searchNode2 = tree.left;
            nodeRect1   = getNodeRect(tree, nodeRect, false);
            nodeRect2   = getNodeRect(tree, nodeRect, true);
        }

        mayCloser = nearest(p, searchNode1, minDist, nodeRect1);
        if (mayCloser != null) {
            dist = p.distanceSquaredTo(mayCloser);
            if (dist < minDist) {
                minDist      = dist;
                closestPoint = mayCloser;
            }
        }

        // Could have closer in right tree
        mayCloser = nearest(p, searchNode2, minDist, nodeRect2);
        if (mayCloser != null) {
            dist = p.distanceSquaredTo(mayCloser);
            if (dist < minDist) {
                closestPoint = mayCloser;
            }
        }

        return closestPoint;
    }

    /**
     * Determine should go left/bottom subtree
     * or right/top subtree
     *
     * @param <Node> node - node to test
     * @param <Point2D> p - point to test
     * @return the point is at left/bottom or right/top to the node
     */
    private boolean isSmaller(Node node, Point2D p) {
        return node.isVertical
            ? node.p.x() > p.x()
            : node.p.y() > p.y();
    }

    /**
     * Helper function for drawing points
     */
    private void prepareDrawPoint() {
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(0.01);
    }

    /**
     * Helper function for drawing lines
     *
     * @param <boolean> isVertical - determine the line color
     */
    private void prepareDrawLine(boolean isVertical) {
        Color color = isVertical
            ? StdDraw.RED
            : StdDraw.BLUE;
        StdDraw.setPenColor(color);
        StdDraw.setPenRadius();
    }

    /**
     * Helper function to get NodeRect
     *
     * @param <Node> node - the separating node
     * @param <RectHV> prevRect - available rectangle area of the node
     * @param <boolean> isSmaller - should return left/bottom or right/top
     * @return the Node rectangle
     */
    private RectHV getNodeRect(Node node, RectHV prevRect, boolean isSmaller) {
        if (node.isVertical) {
            return isSmaller
                ? new RectHV(prevRect.xmin(), prevRect.ymin(), node.p.x(), prevRect.ymax())
                : new RectHV(node.p.x(), prevRect.ymin(), prevRect.xmax(), prevRect.ymax());
        } else {
            return isSmaller
                ? new RectHV(prevRect.xmin(), prevRect.ymin(), prevRect.xmax(), node.p.y())
                : new RectHV(prevRect.xmin(), node.p.y(), prevRect.xmax(), prevRect.ymax());
        }
    }


}
