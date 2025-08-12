package de.lmu.ifi.sosylab.client;

import static java.nio.charset.StandardCharsets.UTF_8;

import de.lmu.ifi.sosylab.client.model.GameClientOnlineModel;
import de.lmu.ifi.sosylab.shared.FactoryDisplay;
import de.lmu.ifi.sosylab.shared.JsonMessage;
import de.lmu.ifi.sosylab.shared.Tiles;
import de.lmu.ifi.sosylab.shared.requests.LoginRequest;
import de.lmu.ifi.sosylab.shared.requests.PlaceTilesRequest;
import de.lmu.ifi.sosylab.shared.signals.TilesCollectedSignal;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * The network-connection of the client. Establishes a connection to the server and takes care of
 * sending and receiving messages in JSON format.
 */
public class ClientNetworkConnection {

  private static final String HOST = "localhost";
  private static final int PORT = 8080;
  private Socket socket;
  private GameClientOnlineModel model;
  private BufferedReader reader;
  private OutputStreamWriter writer;
  boolean socketConnected = false;

  /**
   * Construct for the client network connection.
   *
   * @param model the model that controls the communication between the network connection
   *              and the GUI
   * @throws IOException
   * @throws JSONException
   */
  public ClientNetworkConnection(GameClientOnlineModel model) throws IOException, JSONException {
	this.model = model;
  }

  /**
   * Send a json-message/object to the server. This can be a signal or a request.
   *
   * @param jsonMessage contains the information about the signal/request
   * @throws IOException if the json-object doesn't contain the right information
   */
  private void sendJsonMessageToServer(JSONObject jsonMessage) throws IOException {

	writer.write(jsonMessage + "\n");
	writer.flush();

  }

  /**
   * Read the direct response to a request that the server sent only to this client.
   *
   * @return a Json-Object that contains the response to the made request
   * @throws IOException if the json message doesn't contain the right information
   */
  private JSONObject readDirectResponseFromServer() throws IOException {

	String line = reader.readLine();
	JSONObject jsonObject = new JSONObject(line);
	return jsonObject;

  }

  /**
   * Activates a thread that starts reading all the updates that the server sends about the new
   * events in the game. It allows the model to react to the moves and events of the other players
   * and inform the GUI accordingly.
   */
  public void readEventsFromOtherUsers() {
	Thread thread =
		new Thread(
			() -> {
			  while (socketConnected) {
				String line = null;

				try {
				  // Await the response from the server.
				  line = reader.readLine();
				} catch (IOException e) {
				  e.getStackTrace();
				}

				JSONObject jsonObject = null;

				try {
				  assert line != null;
				  if (socketConnected == true) {
					jsonObject = new JSONObject(line);
				  } else {
					break;
				  }
				} catch (JSONException e) {
				  e.getStackTrace();
				}

				try {
				  assert jsonObject != null;

				  if (jsonObject.get("type").equals("user joined")) {

					model.informUserJoined((String) jsonObject.get("nick"));

					System.out.println(
						jsonObject.get("nick")
							+ " has joined the room "
							+ jsonObject.get("room name"));

				  } else if (jsonObject.getString("type").equals("ready for game")) {
            model.informUserReady(jsonObject.getString("nick"));
          } else if (jsonObject.getString("type").equals("not ready for game")) {
            model.informUserNotReady(jsonObject.getString("nick"));
          } else if (jsonObject.get("type").equals("game start")) {
            FactoryDisplay factoryDisplays = new FactoryDisplay(
                model.getUsersAndTheirReadinessState().size());
            JSONArray factoryDisplaysJsAr = jsonObject.getJSONArray("factory displays");
            for (int i = 0; i < factoryDisplaysJsAr.length(); i++) {
              JSONArray factoryDisplayJsAr = factoryDisplaysJsAr.getJSONArray(i);
              for (int j = 0; j < factoryDisplayJsAr.length(); j++) {
                factoryDisplays.getAllDisplays().get(i).
                    add(Tiles.getTileWithName(factoryDisplayJsAr.getString(j)));
              }
            }
            model.startGame(jsonObject.getInt("current player"), factoryDisplays);
				  } else if (jsonObject.get("type").equals("logout")) {
					model.informUserLeftGame((String) jsonObject.get("nick"));
				  } else if (jsonObject.get("type").equals("somebody collected tiles")) {
            model.collectTilesAndInformTheView(
                jsonObject.getInt("collect place"), jsonObject.getInt("tile index"));
          } else if (jsonObject.get("type").equals("invalid move")) {
            model.informInvalidMove();
          } else if (jsonObject.get("type").equals("valid move")
              || jsonObject.get("type").equals("somebody placed tiles")) {
            int placeLocation = jsonObject.getInt("place location");

            ArrayList<Tiles> tilesAddedOnPatternLine = new ArrayList<>();
            JSONArray tilesAddedOnPatternLineJsAr =
                jsonObject.getJSONArray("tiles added on pattern line");
            for (int i = 0; i < tilesAddedOnPatternLineJsAr.length(); i++) {
              tilesAddedOnPatternLine.add(
                  Tiles.getTileWithName(tilesAddedOnPatternLineJsAr.getString(i))
              );
            }

            ArrayList<Tiles> tilesAddedOnFloorLine = new ArrayList<>();
            JSONArray tilesAddedOnFloorLineJsAr =
                jsonObject.getJSONArray("tiles added on floor line");
            for (int i = 0; i < tilesAddedOnFloorLineJsAr.length(); i++) {
              tilesAddedOnFloorLine.add(
                  Tiles.getTileWithName(tilesAddedOnFloorLineJsAr.getString(i))
              );
            }
            model.informChangeInPatternLines(
                placeLocation, tilesAddedOnPatternLine, tilesAddedOnFloorLine);
          } else if (jsonObject.get("type").equals("turn")) {
            model.informTurn(jsonObject.getInt("current player"));
          } else if (jsonObject.get("type").equals("update walls and scores")) {
            ArrayList<ArrayList<Point>> updatedWalls = new ArrayList<>();
            ArrayList<Integer> updatedScores = new ArrayList<>();
            JSONArray updatedContentJsAr = jsonObject.getJSONArray("updated content");
            for (int i = 0; i < updatedContentJsAr.length(); i++) {
              JSONObject updatedContentOfOneUser = updatedContentJsAr.getJSONObject(i);

              ArrayList<Point> updatedWall = new ArrayList<>();
              JSONArray updatedWallJsAr = updatedContentOfOneUser.getJSONArray("updated wall");
              for (int j = 0; j < updatedWallJsAr.length(); j++) {
                JSONObject updatedWallTile = updatedWallJsAr.getJSONObject(j);
                updatedWall.add(new Point(updatedWallTile.getInt("row of wall"),
                    updatedWallTile.getInt("column of wall")));

              }
              updatedWalls.add(updatedWall);

              updatedScores.add(updatedContentOfOneUser.getInt("updated score"));
            }

            model.updateWallsAndScores(updatedWalls, updatedScores);
          } else if (jsonObject.get("type").equals("start next round")) {
            FactoryDisplay factoryDisplays = new FactoryDisplay(
                model.getUsersAndTheirReadinessState().size());
            JSONArray factoryDisplaysJsAr = jsonObject.getJSONArray("factory displays");
            for (int i = 0; i < factoryDisplaysJsAr.length(); i++) {
              JSONArray factoryDisplayJsAr = factoryDisplaysJsAr.getJSONArray(i);
              for (int j = 0; j < factoryDisplayJsAr.length(); j++) {
                factoryDisplays.getAllDisplays().get(i).
                    add(Tiles.getTileWithName(factoryDisplayJsAr.getString(j)));
              }
            }
            model.informStartNextRound(jsonObject.getInt("current player"), factoryDisplays);
          } else if (jsonObject.get("type").equals("end of game")) {
            JSONArray rankingsJsAr = jsonObject.getJSONArray("rankings");
            LinkedHashMap<Integer, Integer> rankings = new LinkedHashMap<>();
            for (int i = 0; i < rankingsJsAr.length(); i++) {
              JSONObject ranking = rankingsJsAr.getJSONObject(i);
              rankings.put(ranking.getInt("player id"), ranking.getInt("ranking"));
            }
            model.informGameEnd(rankings);
          } else if (jsonObject.get("type").equals("user left room")) {
            model.informUserLeftRoom(jsonObject.getString("nick"));
          } else if (jsonObject.get("type").equals("user left game")) {
            model.informUserLeftGame(jsonObject.getString("nick"));
          } else if (jsonObject.get("type").equals("restart request")) {
            model.informRestartRequest(jsonObject.getString("nick"));
          } else if (jsonObject.get("type").equals("restart rejected")) {
            model.informRestartRequestRejected();
          } else if (jsonObject.get("type").equals("restart")) {
            FactoryDisplay factoryDisplays = new FactoryDisplay(
                model.getUsersAndTheirReadinessState().size());
            JSONArray factoryDisplaysJsAr = jsonObject.getJSONArray("factory displays");
            for (int i = 0; i < factoryDisplaysJsAr.length(); i++) {
              JSONArray factoryDisplayJsAr = factoryDisplaysJsAr.getJSONArray(i);
              for (int j = 0; j < factoryDisplayJsAr.length(); j++) {
                factoryDisplays.getAllDisplays().get(i).
                    add(Tiles.getTileWithName(factoryDisplayJsAr.getString(j)));
              }
            }
            model.informRestartGame(jsonObject.getInt("current player"), factoryDisplays);
          }
				  System.out.println(
					  "Update from server: " + System.lineSeparator() + jsonObject.toString(1));
				} catch (JSONException e) {
				  e.getStackTrace();
				}
			  }
			});
	thread.start();
  }

  /**
   * Stop the network-connection.
   */
  public void stop() {
	try {
	  socket.close();
	} catch (IOException e) {
	  e.printStackTrace();
	}
  }

  // 1. The following methods are made to send requests from the model to the server.
  //    These requests need validation from the server and wait for the corresponding direct
  //    response from the server

  /**
   * Send a login-request to the server.
   *
   * @param loginRequest contains the necessary information for a login request.
   */
  public void sendLoginRequest(LoginRequest loginRequest) throws JSONException, IOException {

    socket = new Socket(loginRequest.getIpAdresse(), PORT);

    reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), UTF_8));

    writer = new OutputStreamWriter(socket.getOutputStream(), UTF_8);


    sendJsonMessageToServer(
        JsonMessage.login(loginRequest.getNickname(), loginRequest.getRoomName()));

	  // Read response from server
	  String line = reader.readLine();
	  JSONObject jsonObject = new JSONObject(line);

    if (jsonObject.get("type").equals("login success")) {
      System.out.println("success");

      LinkedHashMap<String, Boolean> usersReady = new LinkedHashMap<>();

      String roomName = loginRequest.getRoomName();
      String nickname = loginRequest.getNickname();

      JSONArray jsonArray = jsonObject.getJSONArray("logged users");
      for (int i = 0; i < jsonArray.length(); i++) {
        JSONObject userAndState = jsonArray.getJSONObject(i);
        usersReady.put(
            userAndState.getString("nick"), userAndState.getBoolean("is ready for game"));
      }

      model.informLoginSuccess(nickname, roomName, usersReady);

      socketConnected = true;

    } else if (jsonObject.get("type").equals("login failed")) {
      model.informLoginFailed((String) jsonObject.get("cause"));
    }

    //System.out.println(
     //   "Response to login request: " + System.lineSeparator() + jsonObject.toString(1));
  }

  public void sendMoveRequest(PlaceTilesRequest moveRequest) throws IOException {
    sendJsonMessageToServer(JsonMessage.placeTilesRequest(moveRequest.getRowIndex()));
  }


  // 2. The following methods are made to send signals to the server, that don't need validation.
  //    The signals are to be sent from the server to the other players in the corresponding room

  /**
   * Signal that this client is ready to play.
   *
   * @throws JSONException if
   * @throws IOException if
   */
  public void signalReadyToPlay() throws JSONException, IOException {

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("type", "ready for game");

	sendJsonMessageToServer(jsonObject);

  }

  /**
   * Signal that this player is not ready to play.
   *
   * @throws JSONException
   * @throws IOException
   */
  public void signalNotReadyToPlay() throws JSONException, IOException {

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("type", "not ready for game");

	sendJsonMessageToServer(jsonObject);

  }

  public void sendReplyToRestartRequest(boolean agree) throws IOException {
    sendJsonMessageToServer(JsonMessage.replyToRestartRequest(agree));
  }

  /**
   * Signal that this client will restart the game.
   */
  public void sendRestarGameSignal() throws IOException {

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("type", "restart game");

	sendJsonMessageToServer(jsonObject);

  }

  public void sendStopGameSignal() {

	JSONObject jsonObject = new JSONObject();
	jsonObject.put("type", "stop game");

  }

  public void sendLogout() throws JSONException, IOException {

	// Create a message in json-format
	JSONObject jsonObject = new JSONObject();
	jsonObject.put("type", "logout");

	// Send the message to the server
	writer.write(jsonObject + "\n");
	writer.flush();

  }

  public void collectTiles(TilesCollectedSignal tilesCollectedSignal) throws IOException {
    sendJsonMessageToServer(JsonMessage.collectTiles(
        tilesCollectedSignal.getIndexOfCollectPlace(), tilesCollectedSignal.getTileIndex()));
  }

  public void restartGame() throws IOException {
	sendJsonMessageToServer(JsonMessage.restartRequestToServer());

  }
}
