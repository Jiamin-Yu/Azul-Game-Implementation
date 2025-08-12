package de.lmu.ifi.sosylab.shared.events;

public class UserLeftRoomEvent extends GameEvent{

  @Override
  public String getName() {
    return "UserLeftRoomEvent";
  }

}