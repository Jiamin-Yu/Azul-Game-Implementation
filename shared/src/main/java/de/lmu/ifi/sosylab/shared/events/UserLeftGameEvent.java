package de.lmu.ifi.sosylab.shared.events;

public class UserLeftGameEvent extends GameEvent {

  @Override
  public String getName() {
	return "UserLeftGameEvent";
  }
}
