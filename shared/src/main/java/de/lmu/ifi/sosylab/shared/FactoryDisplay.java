package de.lmu.ifi.sosylab.shared;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Manage the factory displays of the game.
 */
public class FactoryDisplay {
  private static final int maxTiles = 4;
  private final ArrayList<ArrayList<Tiles>> allDisplays;


  /**
   * Construct factory displays according to player number.
   *
   * @param playerNumber the number of players in the game.
   */
  public FactoryDisplay(int playerNumber) {

    int displaysNumber = playerNumber * 2 + 1;
    allDisplays = new ArrayList<>(displaysNumber);
    for (int displayIndex = 0; displayIndex < displaysNumber; displayIndex++) {
      allDisplays.add(new ArrayList<>());
    }

  }

  /**
   * get all factory displays in the game.
   *
   * @return all factory displays.
   */
  public ArrayList<ArrayList<Tiles>> getAllDisplays() {

    return allDisplays;
  }

  /**
   * remove the chosen tiles from the chosen factory display.
   *
   * @param factoryDisplay the chosen factory display.
   * @param removeTilesIndex the index of (one of) the chosen tile(s).
   * @return an array of the removed tiles.
   */
  public Tiles[] removeTiles(ArrayList<Tiles> factoryDisplay, int removeTilesIndex) {
    if (removeTilesIndex < 0) {
      throw new IllegalArgumentException("Parameter for index of removed tile may not be negative");
    } else if (removeTilesIndex >= maxTiles) {
      throw new IllegalArgumentException("Parameter for index of removed tile may not"
          + " exceed the maximum number of tiles in a factory display");
    }

    Tiles removeTileType = factoryDisplay.get(removeTilesIndex);

    int countTiles = 0;
    for (int tileIndex = 0; tileIndex < maxTiles; tileIndex++) {
      if (factoryDisplay.get(tileIndex) == removeTileType) {
        countTiles = countTiles + 1;
      }
    }

    factoryDisplay.removeIf(n -> (n == removeTileType));

    Tiles[] removeTiles = new Tiles[countTiles];

    Arrays.fill(removeTiles, removeTileType);
    return removeTiles;
  }

  /**
   * Get the remaining tiles of the factory display.
   *
   * @param factoryDisplay the chosen factory display.
   * @param removeTiles the number of tiles that are collected.
   * @return the remaining tiles.
   */
  public ArrayList<Tiles> remainTiles(ArrayList<Tiles> factoryDisplay, int removeTiles) {
    int remainTilesCount = maxTiles - removeTiles;
    return new ArrayList<>(factoryDisplay.subList(0, remainTilesCount));
  }



}
