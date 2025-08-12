package de.lmu.ifi.sosylab.shared.events;

/**
 * Event that is sent by the model to the listener. It notifies the listener that the state
 * of the factory displays has changed.
 */

public class FactoryDisplaysChangeEvent extends GameEvent {

  public String getName() {
    return "FactoryDisplayChangeEvent";
  }

}