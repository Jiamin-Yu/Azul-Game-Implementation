package de.lmu.ifi.sosylab.shared.events;

public class LoginFailedEvent extends GameEvent {

  private final String cause;

  public LoginFailedEvent(String cause){
    this.cause = cause;
  }

  public String getCause() {
    return cause;
  }

  @Override
  public String getName() {
	return "LoginFailedEvent";
  }
}
