package de.lmu.ifi.sosylab.client.model;

import de.lmu.ifi.sosylab.shared.FactoryDisplay;
import de.lmu.ifi.sosylab.shared.GamePlayers;
import de.lmu.ifi.sosylab.shared.PlayerBoard;
import de.lmu.ifi.sosylab.shared.Tiles;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides methods necessary for the GUI to be updated correctly.
 */
public interface Model {

//  String getNickname();

  int getPlayerCount();

  GamePlayers getGamePlayers();

  FactoryDisplay getFactoryDisplays();

  int getCurrentPlayer();

  int getCurrentDisplay();

  List<Tiles> getGameTable();

  PlayerBoard getPlayerBoard(int player);

  ArrayList<Integer> getScores();

  void restartGame();

  String getNickname();

  String getRoomName();

  /**
   * Add a {@link PropertyChangeListener} to the model to notify subscribed listener about
   * any changes that are published by the model.
   *
   * @param listener the view that subscribes itself to the model.
   */
  void addPropertyChangeListener(PropertyChangeListener listener);

  /**
   * Remove the subscribed listener from the model. The listener will not get notified about
   * any changes that are published by the model.
   *
   * @param listener the view that is to be unsubscribed from the model.
   */
  void removePropertyChangeListener(PropertyChangeListener listener);
}
