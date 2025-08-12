package de.lmu.ifi.sosylab.shared.events;

/**
 * Event that is sent by the model to the listener. It notifies the listener that
 * the model has started next round.
 */

public class StartNextRoundEvent extends GameEvent {

  @Override
  public String getName() {
    return "StartNextRoundEvent";
  }
}
