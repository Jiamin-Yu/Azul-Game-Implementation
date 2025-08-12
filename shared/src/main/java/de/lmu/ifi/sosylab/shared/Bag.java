package de.lmu.ifi.sosylab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manages the bag of tiles.
 */
public class Bag {
  private final List<Tiles> bag;

  /**
   * Construct the bag with 100 tiles.
   */
  public Bag() {
    int totalTileNumber = 100;
    bag = new ArrayList<>(totalTileNumber);
    for (int tileIndex = 0; tileIndex < 20; tileIndex++) {
      bag.add(Tiles.RED);
    }
    for (int tileIndex = 20; tileIndex < 40; tileIndex++) {
      bag.add(Tiles.BLUE);
    }
    for (int tileIndex = 40; tileIndex < 60; tileIndex++) {
      bag.add(Tiles.GREEN);
    }
    for (int tileIndex = 60; tileIndex < 80; tileIndex++) {
      bag.add(Tiles.YELLOW);
    }
    for (int tileIndex = 80; tileIndex < 100; tileIndex++) {
      bag.add(Tiles.DARK);
    }

  }

  /**
   * Get the current size of the bag.
   *
   * @return the size of the bag.
   */
  public int getBagSize() {
    return bag.size();
  }

  /**
   * Get a random tile from the bag.
   *
   * @return a random tile.
   */
  public Tiles getRandomTile() {
    int upperbound = bag.size();
    int randomInt = ThreadLocalRandom.current().nextInt(upperbound);

    Tiles randomTile = bag.get(randomInt);
    bag.remove(randomInt);
    return randomTile;
  }

  /**
   * Add discarded tiles to bag.
   *
   * @param discardTiles the discarded tiles.
   */
  public void addDiscardTiles(List<Tiles> discardTiles) {
    bag.addAll(discardTiles);
  }


  
}
