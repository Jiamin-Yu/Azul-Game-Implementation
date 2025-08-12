package de.lmu.ifi.sosylab.shared;

import java.util.Arrays;

/**
 * The five different kinds of tiles and the starting player marker.
 */
public enum Tiles {
  RED, BLUE, GREEN, YELLOW, DARK, START;

  /**
   * Get tile according to tile name/tile type.
   *
   * @param tile the name/type of the tile.
   */
  public static Tiles getTileWithName(String tile) {
    return Arrays.stream(Tiles.values()).filter(e -> e.toString().equalsIgnoreCase(tile))
            .findFirst().orElseThrow();
  }
}
