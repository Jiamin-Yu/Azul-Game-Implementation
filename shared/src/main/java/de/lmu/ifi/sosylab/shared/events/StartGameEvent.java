package de.lmu.ifi.sosylab.shared.events;

/**
 * Event that is sent by the model to the listener. It notifies the listener that the current
 * player has changed.
 */
public class StartGameEvent extends GameEvent {
  public String getName() {
    return "StartGameEvent";
  }

}