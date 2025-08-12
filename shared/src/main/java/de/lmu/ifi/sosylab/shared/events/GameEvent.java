package de.lmu.ifi.sosylab.shared.events;

/**
 * Manage game events that model sends to the listener.
 * It notifies the listener that the state of the model has changed.
 */

public abstract class GameEvent {
  public abstract String getName();


}
