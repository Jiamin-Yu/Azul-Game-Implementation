package de.lmu.ifi.sosylab.shared.events;

import java.util.LinkedHashMap;

/**
 * Event that is sent by the model to the listener. It notifies the listener that
 * the end game condition has been reached.
 */
public class GameEndEvent extends GameEvent {
  private final LinkedHashMap<Integer, Integer> ranking;

  public GameEndEvent(LinkedHashMap<Integer, Integer> ranking){
    this.ranking = ranking;
  }

  public String getName() {
    return "GameEndEvent";
  }

  public LinkedHashMap<Integer, Integer> getRanking() {
    return ranking;
  }
}