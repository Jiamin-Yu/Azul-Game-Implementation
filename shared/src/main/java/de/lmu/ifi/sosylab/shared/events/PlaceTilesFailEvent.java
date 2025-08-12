package de.lmu.ifi.sosylab.shared.events;

/**
 * Event that is sent by the model to the listener. It notifies the listener that the attempt
 * to place tiles on pattern lines has failed.
 */

public class PlaceTilesFailEvent extends GameEvent {
  public String getName() {
    return "PlaceTilesFailEvent";
  }

}