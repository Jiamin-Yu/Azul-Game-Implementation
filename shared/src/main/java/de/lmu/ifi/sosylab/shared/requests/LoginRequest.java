package de.lmu.ifi.sosylab.shared.requests;

public class LoginRequest extends Request {

  private String nickname;
  private String roomName;
  private String ipAdresse;

  public LoginRequest(String nickname, String roomName, String ipAdresse){

    this.nickname = nickname;
    this.roomName = roomName;
    this.ipAdresse = ipAdresse;
  }

  public String getRoomName() {
    return roomName;
  }

  public String getNickname() {
    return nickname;
  }

  public String getIpAdresse() {
    return ipAdresse;
  }
}
