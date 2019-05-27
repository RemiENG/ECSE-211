package ca.mcgill.ecse211.lab4;

// Simple data structure for representing the grid coordinates.
// Will be helpful when making the point arrays for the paths.

/**
 * Nothing special going on here. Just a little data type to represent the waypoints without having to deal with separate coordinates.
 * 
 * @author Remi Carriere
 *
 */
public class MapPoint {
  public int x;
  public int y;
  
  /**
   * Constructor
   * @param x the position in x (in grid lines)
   * @param y the position in y (in grid lines)
   */
  public MapPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }
}