package de.lmu.ifi.sosylab.shared.events;

/**
 * Event that is sent by the model to the listener. It notifies the listener that the state
 * of the pattern lines has changed.
 */

public class PatternLinesChangeEvent extends GameEvent {

  public String getName() {
    return "PatternLinesChangeEvent";
  }

}