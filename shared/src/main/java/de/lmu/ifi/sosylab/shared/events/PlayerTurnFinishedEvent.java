package de.lmu.ifi.sosylab.shared.events;

/**
 * Event that is sent by the model to the listener. It notifies the listener the turn of a player
 * is finished.
 */

public class PlayerTurnFinishedEvent extends GameEvent {

  @Override
  public String getName() {
    return "PlayerTurnFinishedEvent";
  }
}
