/******************************************************************************
 *  Compilation:  javac PointSET.java
 *  Execution:    java PointSET
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
import edu.princeton.cs.algs4.SET;


public class PointSET {
    private SET<Point2D> points;

    /**
     * construct an empty set of points
     */
    public PointSET() { points = new SET<Point2D>(); }

    /**
     * is the set empty?
     *
     * @return boolean of whether it's empty
     */
    public boolean isEmpty() { return points.isEmpty(); }

    /**
     * number of points in the set
     *
     * @return number of points in the set
     */
    public int size() { return points.size(); }

    /**
     * add the point to the set
     * (if it is not already in the set)
     *
     * @param <Point2D> p - point to insert
     */
    public void insert(Point2D p) {
        if (p == null) throw new NullPointerException();
        points.add(p);
    }

    /**
     * does the set contain point p?
     *
     * @param <Point2D> p - point to test
     * @return boolean of whether the set contain point p
     */
    public boolean contains(Point2D p) {
        if (p == null) throw new NullPointerException();
        return points.contains(p);
    }

    /**
     * draw all points to standard draw
     */
    public void draw() {
        for (Point2D p : points) { p.draw(); }
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
        for (Point2D p : points) {
            if (rect.contains(p)) result.add(p);
        }
        return result;
    }

    /**
     * a nearest neighbor in the set to point p
     *
     * @param <Point2D> target - point to test the nearest
     * @return <Point2D> the nearest point; null if the set is empty
     */
    public Point2D nearest(Point2D target) {
        if (target == null) throw new NullPointerException();

        double min = Double.MAX_VALUE;
        Point2D result = null;
        for (Point2D p : points) {
            double currDist = target.distanceSquaredTo(p);
            if (min == Double.MAX_VALUE || currDist < min) {
                result = p;
                min    = currDist;
            }
        }
        return result;
    }

}
