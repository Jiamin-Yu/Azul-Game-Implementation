package de.lmu.ifi.sosylab.shared;

/**
 * Manage tiles on Wall.
 */
public class WallTile {
  private final Tiles tile;
  private boolean isOnWall;

  /**
   * Construct a wall tile according to the tile type and whether it is on wall.
   *
   * @param tile the type of the tile.
   * @param isOnWall whether the tile is on wall, true if it is on wall, false if it is not on wall.
   */
  public WallTile(Tiles tile, boolean isOnWall) {
    this.tile = tile;
    this.isOnWall = isOnWall;
  }

  /**
   * Get the tile.
   *
   * @return the tile.
   */
  public Tiles getTile() {
    return tile;
  }

  /**
   * Get whether the tile is on wall.
   *
   * @return true if the tile is on wall, false if not.
   */
  public boolean getIsOnWall() {
    return isOnWall;
  }

  /**
   * Set that the tile is on wall.
   */
  public void setIsOnWall() {
    isOnWall = true;
  }


}