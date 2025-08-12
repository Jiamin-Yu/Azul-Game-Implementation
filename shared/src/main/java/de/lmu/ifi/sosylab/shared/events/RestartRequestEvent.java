package de.lmu.ifi.sosylab.shared.events;

public class RestartRequestEvent extends GameEvent {

  private String nickname;

  public RestartRequestEvent(String nickname) {
    this.nickname = nickname;
  }

  public String getNickname() {
    return nickname;
  }

  @Override
  public String getName() {
    return "RestartRequestEvent";
  }
}
