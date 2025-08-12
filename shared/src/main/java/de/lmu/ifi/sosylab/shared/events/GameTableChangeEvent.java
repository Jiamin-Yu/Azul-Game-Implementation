package de.lmu.ifi.sosylab.shared.events;

/**
 * Event that is sent by the model to the listener. It notifies the listener that
 * the state of the middle of the game table has changed.
 */

public class GameTableChangeEvent extends GameEvent {
  public String getName() {
    return "GameTableChangeEvent";
  }
}