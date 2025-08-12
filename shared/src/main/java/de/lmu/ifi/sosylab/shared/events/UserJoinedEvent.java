package de.lmu.ifi.sosylab.shared.events;

public class UserJoinedEvent extends GameEvent{

  private final String nickname;

  public UserJoinedEvent(String nickname){
	this.nickname = nickname;
  }

  public String getNickname() {
	return nickname;
  }

  @Override
  public String getName() {
	return "UserJoinedEvent";
  }
}
