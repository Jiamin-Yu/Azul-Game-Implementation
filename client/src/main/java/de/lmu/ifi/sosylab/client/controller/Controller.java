package de.lmu.ifi.sosylab.client.controller;

/**
 * The controller of the game-UI.
 */
public interface Controller {

  /**
   * Send a collect-tiles-from-display request to the model.
   *
   * @param displayIndex the index of the factory display.
   * @param tileIndex the index of one of the tile(s).
   */
  void collectTilesFromDisplay(int displayIndex, int tileIndex);

  /**
   * Send a collect-tiles-from-game-table request to the model.
   *
   * @param tileIndex the index of one of the tile(s).
   */
  void collectTilesFromTable(int tileIndex);

  /**
   * Send a place-tiles-to-pattern-lines request to the model.
   *
   * @param row the row of the pattern line.
   */
  void placeTilesToPatternLines(int row);

  /**
   * GUI sends a change current player request to the model
   * and ask the model to change the current player.
   */
  void changeCurrentPlayer();

  /**
   * Send a place-tiles-to-floor-line request to the model.
   */
  void placeTilesToFloorLine();

  /**
   * Stop the game.
   */
  void stopGame();

  /**
   * Restart the game.
   */
  void restart();

  void replyRestartRequest(boolean answer);
}
