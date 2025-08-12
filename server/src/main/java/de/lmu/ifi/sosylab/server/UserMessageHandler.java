package de.lmu.ifi.sosylab.server;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.shared.JsonMessage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.json.JSONObject;


//Source: the structure of this class is
//largely taken from the example solution of the 'Chat' project.

/**
 * Class that stores information of a single connected client. The information includes
 * the personal information of the client. And a writer, a reader that allows the client
 * to exchange messages with the server.
 */
public class UserMessageHandler implements Runnable {
  //the server connection that the client should connect to
  private final ServerNetworkConnection serverNetworkConnection;
  //the socket of the client
  private final Socket socket;
  //the buffer reader of the client
  private final BufferedReader reader;
  //the buffer writer of the client
  private final BufferedWriter writer;
  //the game room of this user
  private GameRoom gameRoom;
  //the nickname of this user
  private String nickname;

  /**
   * Construct a {@link UserMessageHandler} to handle the messages exchange between
   * a single client and the server.
   *
   * @param serverNetworkConnection the network layer of the server.
   * @param socket the socket that belongs to this single client.
   * @throws IOException is thrown if retrieving from In- or Output-stream fails.
   */
  public UserMessageHandler(ServerNetworkConnection serverNetworkConnection, Socket socket)
      throws IOException {
    System.out.println("constructor: UserMessageHandler");
    this.serverNetworkConnection = requireNonNull(serverNetworkConnection);
    this.socket = requireNonNull(socket);
    this.reader =
        new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    this.writer = new BufferedWriter(
        new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
  }


  @Override
  public void run() {
    System.out.println("run.");
    try {
      while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }
        JSONObject object = new JSONObject(line);
        handleUserMessage(object);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      close();
    }
  }

  /**
   * Close the connection of this message handler.
   */
  public void close() {
    try {
      if (nickname != null) {
        //check whether game is ongoing
        if (gameRoom.getPhase() == Phase.ONGOING_GAME) {
          //if game is ongoing, left game and set other users to be not ready for game
          gameRoom.removeLeftUserFromRoom(nickname);
          gameRoom.removeUserMessageHandler(this);
          //check whether game room is empty after user left
          //if yes, remove game room in server connection
          if (gameRoom.isGameRoomEmpty()) {
            serverNetworkConnection.removeGameRoom(gameRoom);
          } else {
            //if game room is not empty
            //broadcast user left message to other users
            gameRoom.broadcastToAll(JsonMessage.userLeftGame(nickname));
            //move other users to waiting room
            gameRoom.handleUserLeftAndRoomNotEmpty();
          }
        }
        if (gameRoom.getPhase() == Phase.WAITING_FOR_START) {
          gameRoom.removeLeftUserFromRoom(nickname);
          gameRoom.removeUserMessageHandler(this);
          //check whether game room is empty after user left
          //if yes, remove game room in server connection
          // if no, inform other users in room about user left
          if (gameRoom.isGameRoomEmpty()) {
            serverNetworkConnection.removeGameRoom(gameRoom);
          } else {
            gameRoom.broadcastToAll(JsonMessage.userLeftRoom(nickname));
          }
        }
        nickname = null;
      }
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  /**
   * Handle messages received from this user.
   *
   * @param object a {@link JSONObject} containing a message from this user.
   * @throws IOException thrown when failing to access the input or output stream.
   */
  private void handleUserMessage(JSONObject object) throws IOException {
    System.out.println("handleUserMessage");
    switch (JsonMessage.typeOf(object)) {
      case LOGIN -> handleLoginRequest(object);
      case READY_FOR_GAME -> handleReadyForGame();
      case NOT_READY_FOR_GAME -> handleNotReadyForGame();
      case COLLECT_TILES -> handleCollectTilesSignal(object);
      case PLACE_TILES_REQUEST -> handlePlaceTilesRequest(object);
      case RESTART_REQUEST -> handleRestartGameRequest();
      case REPLY_TO_RESTART_REQUEST -> handleReplyToRestartRequest(object);
      default -> throw new AssertionError("cannot handle user's message" + object);
    }
  }


  private void handleRestartGameRequest() throws IOException {
    for (User user : gameRoom.getAllUsersInGame()) {
      if (Objects.equals(user.getNickName(), nickname)) {
        //set restart game of this user to be true
        user.setRestartGame();
      }
    }
    //send restart request to other users
    gameRoom.broadcast(this, JsonMessage.restartRequestToOtherUsers(nickname));
  }

  private void handleReplyToRestartRequest(JSONObject object) throws IOException {
    boolean agreeToRestart = object.getBoolean("reply");
    if (!agreeToRestart) {
      //server informs all users that the restart game request is rejected
      gameRoom.broadcastToAll(JsonMessage.restartRejected());
      return;
    }
    //server sets the corresponding user true
    for (User singlePlayer : gameRoom.getAllUsersInGame()) {
      if (Objects.equals(singlePlayer.getNickName(), nickname)) {
        singlePlayer.setRestartGame();
        break;
      }
    }
    //check whether other users also agree to restart game
    restartGameIfAllUsersAgree();
  }

  private void restartGameIfAllUsersAgree() throws IOException {
    //server checks whether all users agree to restart game
    //if so, restart game and inform all users
    //if not, nothing happens
    for (User singleUser : gameRoom.getAllUsersInGame()) {
      if (!singleUser.getRestartGame()) {
        return;
      }
    }
    //all users agree to restart game
    gameRoom.restartGame();
    //inform all users restart game
    int indexOfStartingPlayer = gameRoom.getIndexOfCurrentPlayerInGame();
    gameRoom.broadcastToAll(
        JsonMessage.restartGame(gameRoom.getAllFactoryDisplays(), indexOfStartingPlayer));
  }


  private void handleCollectTilesSignal(JSONObject object) throws IOException {
    int collectPlace = object.getInt("collect place");
    int tileIndex = object.getInt("tile index");
    //the user collects tiles from game table
    if (collectPlace == -1) {
      gameRoom.collectTilesFromGameTable(tileIndex);
      return;
    }
    //the user collects tiles from factory display
    gameRoom.collectTilesFromDisplay(collectPlace, tileIndex);
  }

  /**
   * Handle login request from this user.
   *
   * @param object a {@link JSONObject} containing a message with login data.
   * @throws IOException thrown when failing to access the input or output stream.
   */
  private void handleLoginRequest(JSONObject object) throws IOException {
    System.out.println("handleLoginRequest");
    String loginRoomName = (String) object.get("room name");

    //if the room name is still available, new a game room
    if (serverNetworkConnection.isRoomNameAvailable(loginRoomName)) {
      handleLoginWhenRoomNameIsAvailable(object);
    } else {
      handleLoginWhenRoomNameIsUsed(object);
    }
  }

  private void handleLoginWhenRoomNameIsAvailable(JSONObject object) throws IOException {
    String loginRoomName = (String) object.get("room name");
    String nickname = (String) object.get("nick");
    //if room name is not taken, create a new room and a new user
    GameRoom newRoom = new GameRoom(loginRoomName);
    User newUser = new User(nickname);
    setNicknameForThisUser(nickname);

    //add this client to the new game room
    newRoom.addUser(newUser);
    newRoom.addUserMessageHandler(this);

    //add the new room to the list of present rooms
    serverNetworkConnection.addNewRoom(newRoom);
    setGameRoom(newRoom);

    //inform this client of login success and who are currently in the game room
    //and their corresponding status (namely, whether they are ready for game)
    //in this case, there are no other users in room
    List<String> allOtherUsersInRoom = new ArrayList<>();
    List<Boolean> readyStatusOfAllOtherUsers = new ArrayList<>();
    send(JsonMessage.loginSuccess(allOtherUsersInRoom, readyStatusOfAllOtherUsers));
    //broadcast to other clients that new user joined the room
    gameRoom.broadcast(this, JsonMessage.userJoined(nickname));
  }

  private void handleLoginWhenRoomNameIsUsed(JSONObject object) throws IOException {
    System.out.println("room name is used: send login fail in handler.");
    List<GameRoom> presentRooms = serverNetworkConnection.getGameRooms();
    //if the room name has been used
    for (GameRoom presentRoom : presentRooms) {
      if (presentRoom.getRoomName().equals(object.get("room name"))) {
        //if there is a present room with the same room name
        //check the game status and the number of players in game
        //if the room has an ongoing game, inform client that her login request fail
        if (presentRoom.getPhase() == Phase.ONGOING_GAME) {
          //inform client that login request has failed
          send(JsonMessage.loginFailed(
              "The room name is used and the game in the corresponding room has started."));
          System.out.println("send json message to client when fail room name is used");
        } else {
          System.out.println("go to method handleLoginWhenNoActiveGameInRoom");
          handleLoginWhenNoActiveGameInRoom(presentRoom, object);
        }
      }
    }
  }

  private void handleLoginWhenNoActiveGameInRoom(GameRoom presentRoom, JSONObject object)
      throws IOException {
    System.out.println("no active game: send login fail in handler.");
    String nickname = (String) object.get("nick");
    //if there is no ongoing game, check whether room is full
    if (presentRoom.isRoomFull()) {
      //if room is full, inform user that login failed
      send(JsonMessage.loginFailed("The room is full."));
    } else {
      //if room is not full, check whether user's nickname has been used in the game room
      if (!presentRoom.isNicknameAvailable(nickname)) {
        System.out.println("check double nickname successful ");
        //if nickname has been used, inform user login failed
        send(JsonMessage.loginFailed("The nickname has been used in room."));
        return;
      }
      //if nickname has not been used, join room and inform login success
      List<User> allOtherUsersInRoom = presentRoom.getAllUsersInGame();
      List<String> nickNamesOfAllOtherUsers = new ArrayList<>();

      List<Boolean> readyStatusOfAllOtherUsers = new ArrayList<>();
      for (User user : allOtherUsersInRoom) {
        boolean readyStatusOfSingleUser = user.isReadyToPlay();
        readyStatusOfAllOtherUsers.add(readyStatusOfSingleUser);

        String nicknameOfSingleUser = user.getNickName();
        nickNamesOfAllOtherUsers.add(nicknameOfSingleUser);
      }
      User newUser = new User(nickname);
      setNicknameForThisUser(nickname);
      presentRoom.addUser(newUser);
      presentRoom.addUserMessageHandler(this);
      setGameRoom(presentRoom);
      //inform this client of login success and who are currently in the game room
      //and their corresponding status (namely, whether they are ready for game)
      send(JsonMessage.loginSuccess(nickNamesOfAllOtherUsers, readyStatusOfAllOtherUsers));
      //broadcast to other clients that new user joined the room
      gameRoom.broadcast(this, JsonMessage.userJoined(nickname));
    }
  }

  private void setNicknameForThisUser(String nickname) {
    this.nickname = nickname;
  }

  private void setGameRoom(GameRoom gameRoom) {
    this.gameRoom = gameRoom;
  }

  private void handleReadyForGame() throws IOException {
    for (User user : gameRoom.getAllUsersInGame()) {
      if (Objects.equals(user.getNickName(), nickname)) {
        user.setReadyToPlay(true);
      }
    }
    //broadcast to all users in the game room that this user is ready for game
    gameRoom.broadcastToAll(JsonMessage.somebodyReadyForGame(nickname));
    //check whether all users in room are ready for game, if so start game and broadcast to all
    //users that game is started
    if (checkStartGame()) {
      gameRoom.startGame();
      //broadcast to all users in room (factory display random fill, start game player)
      int indexOfStartingPlayer = gameRoom.getIndexOfCurrentPlayerInGame();
      gameRoom.broadcastToAll(
          JsonMessage.gameStart(gameRoom.getAllFactoryDisplays(), indexOfStartingPlayer));
      /*
      if (nicknameOfCurrentPlayerInGame.equals(nickname)) {
        //send to the random starting player that she starts game
        send(JsonMessage.yourTurn());
        // (Diego) (?) set current player in online model
        // so that GUI can get current player from model to repaint pattern lines/floor line
        // oder den nickname in json message an GUI weiterleiten (tian changes jason message)

        //broadcast her turn to other players
        gameRoom.broadcast(this, JsonMessage.othersTurn(nickname));
      }
       */
    }
  }

  private void handleNotReadyForGame() throws IOException {
    for (User user : gameRoom.getAllUsersInGame()) {
      if (Objects.equals(user.getNickName(), nickname)) {
        user.setReadyToPlay(false);
      }
    }
    //broadcast to other users in the game room that this user is not ready for game
    gameRoom.broadcastToAll(JsonMessage.somebodyNotReadyForGame(nickname));
  }

  private void handlePlaceTilesRequest(JSONObject object) throws IOException {
    int placeTilesLocation = object.getInt("place location");
    if (placeTilesLocation == -1) {
      //request place tiles to floor line
      handlePlaceTilesToFloorLine();
    }
    //request place tiles to pattern lines
    handlePlaceTilesToPatternLines(placeTilesLocation);
  }

  private void handlePlaceTilesToFloorLine() throws IOException {
    gameRoom.placeTilesToFloorLine();
  }

  private void handlePlaceTilesToPatternLines(int row) throws IOException {
    gameRoom.placeTilesToPatternLine(row);
  }

  /**
   * check whether the start game condition is met, namely, whether all users in room are ready
   * to play.
   *
   * @return <code>true</code> if all users are ready, <code>false</code> otherwise.
   */
  private boolean checkStartGame() {
    return gameRoom.areAllUsersReady();
  }

  /**
   * Send a message to the user.
   *
   * @param messageToUser the message that needs to be sent to the user.
   * @throws IOException thrown when writing to the output stream fails.
   */
  public void send(JSONObject messageToUser) throws IOException {
    String string = messageToUser + System.lineSeparator();
    if (socket.isClosed()) {
      return;
    }
    writer.write(string);
    writer.flush();
  }

}
