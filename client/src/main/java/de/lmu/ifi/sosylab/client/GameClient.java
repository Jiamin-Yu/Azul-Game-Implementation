package de.lmu.ifi.sosylab.client;

import de.lmu.ifi.sosylab.client.view.GameFrame;
import org.json.JSONException;

/**
 * The client's main class.
 */
public class GameClient {

  /**
   * Starts the game-client.
   *
   * @param args the command line arguments
   * @throws JSONException when something goes wrong
   */
  public static void main(String[] args) throws JSONException {

    GameFrame gameFrame = new GameFrame();

    gameFrame.setVisible(true);

  }
}
