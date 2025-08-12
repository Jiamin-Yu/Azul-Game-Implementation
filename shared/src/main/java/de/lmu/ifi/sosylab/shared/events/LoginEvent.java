package de.lmu.ifi.sosylab.shared.events;


public class LoginEvent extends GameEvent {

  private String nickname;

  public LoginEvent(String nickname){
	this.nickname = nickname;
  }

  public String getNickname() {
	return nickname;
  }

  @Override
  public String getName() {
	return "LoginEvent";
  }
}
