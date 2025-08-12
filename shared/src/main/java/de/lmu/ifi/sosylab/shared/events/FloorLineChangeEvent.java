package de.lmu.ifi.sosylab.shared.events;

/**
 * Event that is sent by the model to the listener. It notifies the listener that the state
 * of the floor line has changed.
 */
public class FloorLineChangeEvent extends GameEvent {
  public String getName() {
    return "FloorLineChangeEvent";
  }

}