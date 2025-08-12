package de.lmu.ifi.sosylab.client.controller;

import de.lmu.ifi.sosylab.client.model.GameClientOnlineModel;
import java.io.IOException;
import org.json.JSONException;

/**
 * When an online game is played, the GameControllerOnline handles user inputs by forwarding them to
 * the corresponding model.
 */
public class GameControllerOnline implements Controller {

  GameClientOnlineModel model;

  /**
   * Construct a GameController to navigate between GameClientModel and GameView.
   *
   * @param model the GameClientModel.
   */
  public GameControllerOnline(GameClientOnlineModel model) {
    this.model = model;
  }

  /**
   * {@inheritDocn}
   */
  @Override
  public void collectTilesFromDisplay(int displayIndex, int tileIndex) {
    if (model.getFactoryDisplays().getAllDisplays().get(displayIndex).size() > 0) {
      try {
        model.signalTilesCollected(displayIndex, tileIndex);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * {@inheritDocn}
   */
  @Override
  public void collectTilesFromTable(int tileIndex) {
    try {
      model.signalTilesCollected(-1, tileIndex);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * {@inheritDocn}
   */
  @Override
  public void placeTilesToPatternLines(int row) {
    try {
      model.requestPlaceTiles(row);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * {@inheritDocn}
   */
  @Override
  public void changeCurrentPlayer() {

  }

  /**
   * {@inheritDocn}
   */
  @Override
  public void placeTilesToFloorLine() {
    try {
      model.requestPlaceTiles(-1);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Stop the game.
   */
  @Override
  public void stopGame() {
    model.dispose();
  }

  /**
   * Send a request to restart the game.
   */
  @Override
  public void restart() {
    model.restartGame();
  }

  @Override
  public void replyRestartRequest(boolean answer) {

  }

  /**
   * Sends a login-request to the model.
   *
   * @param username the user's name
   * @param roomName the room's name
   * @param address the room's IP-address
   */
  public void login(String username, String roomName, String address) {
    try{
      model.requestLogin(username, roomName, address);
    } catch (IOException e){
      e.printStackTrace();
    }
  }

  /**
   * Signal that this user is ready to play.
   *
   * @throws IOException
   */
  public void signalReadyToPlay() throws IOException {
      model.signalReadyToPlay();
  }

  /**
   * Signal that this player is not ready to play.
   *
   * @throws JSONException
   * @throws IOException
   */
  public void signalNotReady() throws JSONException, IOException {
    model.signalNotReady();
  }

  /**
   * Quit the game.
   */
  public void leaveRoom() throws IOException {
    model.signalLogout();
  }


}
