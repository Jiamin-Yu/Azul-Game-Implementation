package de.lmu.ifi.sosylab.client.controller;

import de.lmu.ifi.sosylab.client.model.GameClientModel;
import java.util.ArrayList;

/**
 * Handle hotseat-mode user inputs and forwards them to the corresponding model.
 */
public class GameController implements Controller {
  private final GameClientModel model;

  /**
   * Construct a GameController to navigate between GameClientModel and GameView.
   *
   * @param model the GameClientModel.
   */
  public GameController(GameClientModel model) {

   this.model = model;
  }

  /**
   * Send a start game request to the model.
   */
  public void startGame(int playerNumber, ArrayList<String> usernames) {
    model.setPlayerNumber(playerNumber);
    model.setUsernames(usernames);
    model.startGame();
  }

  /**
   * Send a collect-tiles-from-display request to the model.
   *
   * @param displayIndex the index of the factory display.
   * @param tileIndex the index of one of the tile(s).
   */
  @Override
  public void collectTilesFromDisplay(int displayIndex, int tileIndex) {
    model.tilesFromDisplay(displayIndex, tileIndex);
  }

  /**
   * Send a collect-tiles-from-game-table request to the model.
   *
   * @param tileIndex the index of one of the tile(s).
   */
  @Override
  public void collectTilesFromTable(int tileIndex) {
    model.tilesFromTable(tileIndex);
  }

  /**
   * Send a place-tiles-to-pattern-lines request to the model.
   *
   * @param row the row of the pattern line.
   */
  @Override
  public void placeTilesToPatternLines(int row) {
    model.placeTilesToPatternLines(row);
  }

  /**
   * Send a place-tiles-to-floor-line request to the model.
   */
  @Override
  public void placeTilesToFloorLine() {
    model.placeTilesToFloorLine();
  }

  /**
   * GUI sends a change current player request to the model
   * and ask the model to change the current player.
   */
  @Override
  public void changeCurrentPlayer() {
    model.changeCurrentPlayer();
  }

  /**
   * Stop the game.
   */
  @Override
  public void stopGame() {

  }

  /**
   * Restart the game.
   */
  @Override
  public void restart() {
    model.restartGame();
  }

  @Override
  public void replyRestartRequest(boolean answer) {

  }
}
