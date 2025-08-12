package de.lmu.ifi.sosylab.server;

public class User {
  private final String nickName;
  private boolean readyToPlay;
  private boolean restartGame;

  public User(String name) {
    readyToPlay = false;
    restartGame = false;
    this.nickName = name;
  }

  public void setRestartGame() {
    restartGame = true;
  }

  public boolean getRestartGame() {
    return restartGame;
  }

  public String getNickName() {
    return nickName;
  }

  public boolean isReadyToPlay() {
    return readyToPlay;
  }

  public void setReadyToPlay(boolean readyToPlay) {
    this.readyToPlay = readyToPlay;
  }

}
