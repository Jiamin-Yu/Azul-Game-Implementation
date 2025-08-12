package de.lmu.ifi.sosylab.shared;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Categorize the json messages for the communication between user and server.
 */
public enum JsonMessage {
  LOGIN("login"), LOGIN_SUCCESS("login success"), LOGIN_FAILED("login failed"),
  USER_JOINED("user joined"), USER_LEFT_ROOM("user left room"), READY_FOR_GAME("ready for game"),
  NOT_READY_FOR_GAME("not ready for game"), GAME_START("game start"), TURN("turn"),
  COLLECT_TILES("collect tiles"), PLACE_TILES_REQUEST("place tiles request"),
  SOMEBODY_COLLECTED_TILES("somebody collected tiles"), USER_LEFT_GAME("user left game"),
  SOMEBODY_PLACED_TILES("somebody placed tiles"), START_NEXT_ROUND("start next round"),
  VALID_MOVE("valid move"), INVALID_MOVE("invalid move"), RESTART_REQUEST("restart request"),
  UPDATE_WALLS_AND_SCORES("update walls and scores"), END_OF_GAME("end of game"),
  FILL_FACTORY_DISPLAYS("fill factory displays"), QUIT_GAME("quit game"), RESTART("restart"),
  REPLY_TO_RESTART_REQUEST("reply to restart request"), RESTART_REJECTED("restart rejected");

  public static final String TYPE_FIELD = "type";
  public static final String NICK_FIELD = "nick";
  public static final String ROOM_NAME_FIELD = "room name";
  public static final String LOGGED_USERS_FIELD = "logged users";
  public static final String IS_READY_FOR_GAME_FIELD = "is ready for game";
  public static final String CURRENT_PLAYER_FIELD = "current player";
  public static final String CAUSE_FIELD = "cause";
  public static final String COLLECT_PLACE_FIELD = "collect place";
  public static final String TILE_FIELD = "tile";
  public static final String PLAYER_ID_FIELD = "player id";
  public static final String TILE_INDEX_FIELD = "tile index";
  public static final String PLACE_LOCATION_FIELD = "place location";
  public static final String TILES_ADDED_ON_PATTERN_LINE_FIELD = "tiles added on pattern line";
  public static final String INDEX_OF_PATTERN_LINE_FIELD = "index of pattern line";
  public static final String TILES_FIELD = "tiles";
  public static final String TILES_ADDED_ON_FLOOR_LINE_FIELD = "tiles added on floor line";
  public static final String UPDATED_CONTENT_FIELD = "updated content";
  public static final String UPDATED_WALL_FIELD = "updated wall";
  public static final String ROW_OF_WALL_FIELD = "row of wall";
  public static final String COLUMN_OF_WALL_FIELD = "column of wall";
  public static final String UPDATED_SCORE_FIELD = "updated score";
  public static final String FACTORY_DISPLAYS_FIELD = "factory displays";
  public static final String REPLY_FIELD = "reply";
  public static final String RANKINGS_FIELD = "rankings";
  public static final String RANKING_FIELD = "ranking";

  private final String jsonName;

  /**
   * Initialize a new json message with given name.
   *
   * @param jsonName
   */
  JsonMessage(String jsonName) {
    this.jsonName = jsonName;
  }

  /**
   * Get the name of JsonMessage constant.
   *
   * @return
   */
  public String getJsonName() {
    return jsonName;
  }

  /**
   * Get the type of the given json message.
   *
   * @param message The json message
   * @return The type of the json message
   */
  public static JsonMessage typeOf(JSONObject message) {
    String typeName;
    try {
      typeName = message.getString(TYPE_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException(String.format("Unknown message type '%s'", message), e);
    }

    Optional<JsonMessage> opt =
        Arrays.stream(JsonMessage.values()).filter(x -> x.getJsonName().equals(typeName))
            .findFirst();
    return opt.orElseThrow(
        () -> new IllegalArgumentException(String.format("Unknown message type '%s'", typeName)));
  }

  /**
   * Create a json object of a message in certain type.
   *
   * @param type The type of the message
   * @return The json object
   * @throws JSONException If the creating of the json object fails
   */
  private static JSONObject createMessageOfType(JsonMessage type) throws JSONException {
    return new JSONObject().put(TYPE_FIELD, type.getJsonName());
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from a user to the server.
   * 2. It informs the server that a user trys to log in.
   *
   * @param nickname the nickname of the user that trys to login
   * @param roomName the name of the room that the user trys to enter
   * @return The json object
   */
  public static JSONObject login(String nickname, String roomName) {
    try {
      JSONObject jsonObject = createMessageOfType(LOGIN);
      jsonObject.put(NICK_FIELD, nickname);
      jsonObject.put(ROOM_NAME_FIELD, roomName);
      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from the server to the user that just tried to log in.
   * 2. It informs the user that her/his login succeeded and who has already logged in this room.
   *
   * @param loggedUsers A list of names of logged users
   * @param loggedUsersStates A list of states of the logged users
   * @return a json object
   */
  public static JSONObject loginSuccess(List<String> loggedUsers, List<Boolean> loggedUsersStates) {
    try {
      JSONObject jsonObject = createMessageOfType(LOGIN_SUCCESS);
      JSONArray jsonArray = new JSONArray();

      for (int i = 0; i < loggedUsers.size(); i++) {
        JSONObject loggedUserAndState = new JSONObject();
        loggedUserAndState.put(NICK_FIELD, loggedUsers.get(i));
        loggedUserAndState.put(IS_READY_FOR_GAME_FIELD, loggedUsersStates.get(i));
        jsonArray.put(loggedUserAndState);
      }
      jsonObject.put(LOGGED_USERS_FIELD, jsonArray);
      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from the server to the user that just tried to log in.
   * 2. It informs the user that her/his login failed.
   *
   * @param cause Tells why her/his login failed
   * @return The json object
   */
  public static JSONObject loginFailed(String cause) {
    try {
      return createMessageOfType(LOGIN_FAILED).put(CAUSE_FIELD, cause);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from the server to all users in a certain room, except the one that
   * just came in.
   * 2. It informs them that a user just joined.
   *
   * @param nickname The nickname of the user that just joined
   * @return The json object
   */
  public static JSONObject userJoined(String nickname) {
    try {
      return createMessageOfType(USER_JOINED).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }
  public static JSONObject userLeftRoom(String nickname) {
    try {
      return createMessageOfType(USER_LEFT_ROOM).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject userLeftGame(String nickname) {
    try {
      return createMessageOfType(USER_LEFT_GAME).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from a user to the server.
   * 2. It informs the server that the user is ready for game.
   *
   * @return the json object
   */
  public static JSONObject readyForGame() {
    try {
      return createMessageOfType(READY_FOR_GAME);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from a user to the server.
   * 2. It informs the server that the user is not ready for game.
   *
   * @return the json object
   */
  public static JSONObject notReadyForGame() {
    try {
      return createMessageOfType(NOT_READY_FOR_GAME);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from the server to all users in a certain room, except the user that
   * is just ready for game.
   * 2. It informs them that a user in their room is ready for game.
   *
   * @param nickname The nickname of the user that is just ready for game
   * @return The json object
   */
  public static JSONObject somebodyReadyForGame(String nickname) {
    try {
      return createMessageOfType(READY_FOR_GAME).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from the server to all users in a certain room, except the user that
   * is just not ready for game.
   * 2. It informs them that a user in their room is not ready for game.
   *
   * @param nickname The nickname of the user that is just not ready for game
   * @return The json object
   */
  public static JSONObject somebodyNotReadyForGame(String nickname) {
    try {
      return createMessageOfType(NOT_READY_FOR_GAME).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from the server to all users in a certain room.
   * 2. It informs them that the game starts.
   *
   * @return The json object
   */
  public static JSONObject gameStart(FactoryDisplay factoryDisplays, int currentPlayer) {
    return startNewRound(GAME_START, factoryDisplays, currentPlayer);
  }

  public static JSONObject restartGame(FactoryDisplay factoryDisplays, int currentPlayer) {
    return startNewRound(RESTART, factoryDisplays, currentPlayer);
  }

  public static JSONObject startNextRound(FactoryDisplay factoryDisplays, int currentPlayer) {
    return startNewRound(START_NEXT_ROUND, factoryDisplays, currentPlayer);
  }

  private static JSONObject startNewRound(JsonMessage type,
      FactoryDisplay factoryDisplays, int currentPlayer) {
    try {
      JSONObject jsonObject = createMessageOfType(type);

      JSONArray jsonArray = new JSONArray();
      ArrayList<ArrayList<Tiles>> allDisplays = factoryDisplays.getAllDisplays();
      for (ArrayList<Tiles> factoryDisplay : allDisplays) {
        jsonArray.put(new JSONArray().putAll(factoryDisplay));
      }
      jsonObject.put(FACTORY_DISPLAYS_FIELD, jsonArray);
      jsonObject.put(CURRENT_PLAYER_FIELD, currentPlayer);

      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject updateWallsAndScores(List<List<Point>> updatedWalls,
      List<Integer> scores) {
    try {
      JSONObject jsonObject = createMessageOfType(UPDATE_WALLS_AND_SCORES);

      JSONArray updatedContents = new JSONArray();
      for (int i = 0; i < scores.size(); i++) {
        JSONObject updatedContent = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (Point point : updatedWalls.get(i)) {
          JSONObject updatedWallTile = new JSONObject();
          updatedWallTile.put(ROW_OF_WALL_FIELD, point.x);
          updatedWallTile.put(COLUMN_OF_WALL_FIELD, point.y);
          jsonArray.put(updatedWallTile);
        }
        updatedContent.put(UPDATED_WALL_FIELD, jsonArray);
        updatedContent.put(UPDATED_SCORE_FIELD, scores.get(i));
        updatedContents.put(updatedContent);
      }
      jsonObject.put(UPDATED_CONTENT_FIELD, updatedContents);

      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject collectTiles(int collectPlace, int tileIndex) {
    try {
      JSONObject jsonObject = createMessageOfType(COLLECT_TILES);
      jsonObject.put(COLLECT_PLACE_FIELD, collectPlace);
      jsonObject.put(TILE_INDEX_FIELD, tileIndex);
      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from a user to the server.
   * 2. It informs the server where s/he wants to place tiles.
   *
   * @param placeLocation The index of pattern line or floor line
   * @return The json object
   */
  public static JSONObject placeTilesRequest(int placeLocation) {
    try {
      return createMessageOfType(PLACE_TILES_REQUEST).put(PLACE_LOCATION_FIELD, placeLocation);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }


  public static JSONObject turn(int currentPlayer) {
    try {
      return createMessageOfType(TURN).put(CURRENT_PLAYER_FIELD, currentPlayer);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from the server to a user.
   * 2. It informs the user that his move is valid.
   *
   * @return the json object
   */
  public static JSONObject validMove(int placeLocation, List<Tiles> tilesAddedOnPatternLine,
      List<Tiles> tilesAddedOnFloorLine) {
    try {
      JSONObject jsonObject = createMessageOfType(VALID_MOVE);

      jsonObject.put(PLACE_LOCATION_FIELD, placeLocation);
      jsonObject.put(
          TILES_ADDED_ON_PATTERN_LINE_FIELD, createJsonArrayFromTiles(tilesAddedOnPatternLine));
      jsonObject.put(
          TILES_ADDED_ON_FLOOR_LINE_FIELD, createJsonArrayFromTiles(tilesAddedOnFloorLine));

      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject somebodyPlacedTiles(String nickname, int placeLocation,
      List<Tiles> tilesAddedOnPatternLine, List<Tiles> tilesAddedOnFloorLine) {
    try {
      JSONObject jsonObject = createMessageOfType(SOMEBODY_PLACED_TILES);

      jsonObject.put(NICK_FIELD, nickname);
      jsonObject.put(PLACE_LOCATION_FIELD, placeLocation);
      jsonObject.put(
          TILES_ADDED_ON_PATTERN_LINE_FIELD, createJsonArrayFromTiles(tilesAddedOnPatternLine));
      jsonObject.put(
          TILES_ADDED_ON_FLOOR_LINE_FIELD, createJsonArrayFromTiles(tilesAddedOnFloorLine));

      return jsonObject;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  private static JSONArray createJsonArrayFromTiles(List<Tiles> tiles) {
    JSONArray jsonArray = new JSONArray();

    for (Tiles tile : tiles) {
      jsonArray.put(tile.toString());
    }

    return jsonArray;
  }

  /**
   * Create a json object containing a message with the following characteristics.
   * 1. It is sent from the server to a user.
   * 2. It informs the user that his move is invalid.
   *
   * @return the json object
   */
  public static JSONObject invalidMove() {
    try {
      return createMessageOfType(INVALID_MOVE);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject somebodyCollectedTiles(String nickname, int collectPlace,
      int tileIndex) {
    try {
      JSONObject object = createMessageOfType(SOMEBODY_COLLECTED_TILES);
      object.put(NICK_FIELD, nickname);
      object.put(COLLECT_PLACE_FIELD, collectPlace);
      object.put(TILE_INDEX_FIELD, tileIndex);
      return object;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject endOfGame(LinkedHashMap<Integer, Integer> rankings) {
    try {
      JSONObject object = createMessageOfType(END_OF_GAME);

      JSONArray rankingsJsAr = new JSONArray();
      for (Integer playerId : rankings.keySet()) {
        JSONObject ranking = new JSONObject();
        ranking.put(PLAYER_ID_FIELD, playerId);
        ranking.put(RANKING_FIELD, rankings.get(playerId));
        rankingsJsAr.put(ranking);
      }
      object.put(RANKINGS_FIELD, rankingsJsAr);

      return object;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject quitGame(String nickname) {
    try {
      return createMessageOfType(QUIT_GAME).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject restartRequestToServer() {
    try {
      return createMessageOfType(RESTART_REQUEST);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject restartRequestToOtherUsers(String nickname) {
    try {
      return createMessageOfType(RESTART_REQUEST).put(NICK_FIELD, nickname);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject replyToRestartRequest(boolean reply) {
    try {
      return createMessageOfType(REPLY_TO_RESTART_REQUEST).put(REPLY_FIELD, reply);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }

  public static JSONObject restartRejected() {
    try {
      return createMessageOfType(RESTART_REJECTED);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to create a json object.", e);
    }
  }
  public static String getNick(JSONObject object) {
    try {
      return object.getString(NICK_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  public static String getRoomName(JSONObject object) {
    try {
      return object.getString(ROOM_NAME_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  public static JSONArray getLoggedUsers(JSONObject object) {
    try {
      return object.getJSONArray(LOGGED_USERS_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }

  public static Boolean getIsReadyForGame(JSONObject object) {
    try {
      return object.getBoolean(IS_READY_FOR_GAME_FIELD);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to read a json object.", e);
    }
  }
}











