package de.lmu.ifi.sosylab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manage the excess tiles in floor line. Every time when a floor line is full, discard excess tiles
 * to the lid. The discarded tiles in lid is used to fill factory displays when the bag is empty.
 */
public class Lid {
  List<Tiles> lid;

  /**
   * Construct the lid (game box).
   */
  public Lid() {
    lid = new ArrayList<>();
  }

  /**
   * Discard tiles to lid.
   *
   * @param excessTilesInFloorLine the excess tiles in floor line or the discarded tiles during the process of wall-tiling.
   *
   */

  public void discardTilesToLid(List<Tiles> excessTilesInFloorLine) {
    lid.addAll(excessTilesInFloorLine);
  }

  public int getLidSize() {
    return lid.size();
  }

  public Tiles getRandomTileFromLid() {
    int upperbound = lid.size();
    int randomInt = ThreadLocalRandom.current().nextInt(upperbound);

    Tiles randomTile = lid.get(randomInt);
    lid.remove(randomInt);
    return randomTile;
  }

}
