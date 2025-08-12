package de.lmu.ifi.sosylab.client.model;

import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.client.ClientNetworkConnection;
import de.lmu.ifi.sosylab.shared.FactoryDisplay;
import de.lmu.ifi.sosylab.shared.GamePlayers;
import de.lmu.ifi.sosylab.shared.PlayerBoard;
import de.lmu.ifi.sosylab.shared.Tiles;
import de.lmu.ifi.sosylab.shared.events.CreateViewEvent;
import de.lmu.ifi.sosylab.shared.events.FactoryDisplaysChangeEvent;
import de.lmu.ifi.sosylab.shared.events.FloorLineChangeEvent;
import de.lmu.ifi.sosylab.shared.events.GameEndEvent;
import de.lmu.ifi.sosylab.shared.events.GameEvent;
import de.lmu.ifi.sosylab.shared.events.GameTableChangeEvent;
import de.lmu.ifi.sosylab.shared.events.IsNotYourTurnEvent;
import de.lmu.ifi.sosylab.shared.events.LoginEvent;
import de.lmu.ifi.sosylab.shared.events.LoginFailedEvent;
import de.lmu.ifi.sosylab.shared.events.PatternLinesChangeEvent;
import de.lmu.ifi.sosylab.shared.events.PlaceTilesFailEvent;
import de.lmu.ifi.sosylab.shared.events.PlayerTurnFinishedEvent;
import de.lmu.ifi.sosylab.shared.events.RestartEvent;
import de.lmu.ifi.sosylab.shared.events.RestartRequestEvent;
import de.lmu.ifi.sosylab.shared.events.RestartRequestRejectedEvent;
import de.lmu.ifi.sosylab.shared.events.StartGameEvent;
import de.lmu.ifi.sosylab.shared.events.StartNextRoundEvent;
import de.lmu.ifi.sosylab.shared.events.TurnEvent;
import de.lmu.ifi.sosylab.shared.events.UserJoinedEvent;
import de.lmu.ifi.sosylab.shared.events.UserLeftGameEvent;
import de.lmu.ifi.sosylab.shared.events.UserLeftRoomEvent;
import de.lmu.ifi.sosylab.shared.events.UserNotReadyEvent;
import de.lmu.ifi.sosylab.shared.events.UserReadyEvent;
import de.lmu.ifi.sosylab.shared.events.WallChangeEvent;
import de.lmu.ifi.sosylab.shared.requests.LoginRequest;
import de.lmu.ifi.sosylab.shared.requests.PlaceTilesRequest;
import de.lmu.ifi.sosylab.shared.signals.TilesCollectedSignal;
import java.awt.Point;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


/**
 * The online mode's model. Stores the game's data for the GUI to be accesses at any time.
 * It communicates with the server to get and update changes made to the game.
 *
 * @see Model
 */
public class GameClientOnlineModel implements Model {

  private GamePlayers gamePlayers;
  private LinkedHashMap<String, Boolean> usersAndTheirReadinessState;
  private FactoryDisplay factoryDisplays;
  private int currentPlayer;
  private List<Tiles> gameTable;
  private PlayerBoard[] playerBoards;
  private ArrayList<Integer> scores;
  private String nickname;
  private String roomName;
  private int playerIndex;
  private int currentDisplay;
  private List<Tiles> currentCollectedTiles;
  private ClientNetworkConnection connection;
  private final PropertyChangeSupport support;

  /**
   * Construct for the model for the online mode.
   */
  public GameClientOnlineModel() {
	usersAndTheirReadinessState = new LinkedHashMap<>();
	support = new PropertyChangeSupport(this);

	try {
	  connection = new ClientNetworkConnection(this);
	} catch (IOException e) {
	  e.printStackTrace();
	}

  }

  // 0. following methods are to modify die attributs in the online-model.

  /**
   * Notify the view about new events in the game.
   *
   * @param event the new event
   */
  private void notifyListener(GameEvent event) {
	support.firePropertyChange(event.getName(), null, event);
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener listener) {
	requireNonNull(listener);
	support.addPropertyChangeListener(listener);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener listener) {
	support.removePropertyChangeListener(listener);
  }

  /**
   * Set the network-connection to this connection.
   *
   * @param connection the client-network-connection.
   */
  public void setConnection(ClientNetworkConnection connection) {
	this.connection = connection;
  }


  /**
   * Collect the tiles from a factory display.
   *
   * @param tileIndex    the index of the tile, I.e. the type of tile that will be collected
   * @param displayIndex the index of the factory display from where the tiles are going to
   *                     be collected
   */
  private void collectTilesFromFactoryDisplay(int tileIndex, int displayIndex) {
	  if (currentCollectedTiles.size() != 0) {
		  return;
	  }
	ArrayList<Tiles> selectedFactoryDisplay = factoryDisplays.getAllDisplays().get(displayIndex);

	// these tiles come later in one line in the pattern lines or in the floor line
	Tiles[] removedTiles = factoryDisplays.removeTiles(selectedFactoryDisplay, tileIndex);
	List<Tiles> collectedTiles = new ArrayList<>(Arrays.asList(removedTiles));
	setCurrentCollectedTiles(collectedTiles);

	// these tiles come later in the center
	ArrayList<Tiles> remainingTiles =
		factoryDisplays.remainTiles(selectedFactoryDisplay, removedTiles.length);

	gameTable.addAll(remainingTiles);
	currentDisplay = displayIndex;

	// clear the selected factory display
	factoryDisplays.getAllDisplays().get(displayIndex).clear();

  }

  private void setCurrentCollectedTiles(List<Tiles> currentCollectedTiles) {
	  this.currentCollectedTiles = currentCollectedTiles;
  }

  /**
   * Collect the tiles from the center of the game table.
   *
   * @param tileIndex the index of the tile, I.e. the type of tile that will be collected
   */
  private void collectTilesFromTheCenter(int tileIndex) {
	  if (currentCollectedTiles.size() != 0) {
		  return;
	  }

	Tiles pickedTile = gameTable.get(tileIndex);
	  List<Tiles> collectedTiles = new ArrayList<>();
	  for (Tiles singleTile : gameTable) {
		  if (singleTile == pickedTile) {
			  collectedTiles.add(singleTile);
		  }
	  }
	  setCurrentCollectedTiles(collectedTiles);

	if (gameTable.get(0) == Tiles.START) {

	  gameTable.removeIf(e -> e == Tiles.START || e == pickedTile);

	  playerBoards[currentPlayer].getFloorLine().add(0, Tiles.START);

	} else {

	  gameTable.removeIf(e -> e == pickedTile);
	}

  }

  /**
   * Generate the player boards at the beginning of the game
   */
  private void createPlayerBoards() {
	playerBoards = new PlayerBoard[usersAndTheirReadinessState.size()];
	for (int i = 0; i < usersAndTheirReadinessState.size(); i++) {
	  playerBoards[i] = new PlayerBoard();
	}
  }

  /**
   * Create the center of the game board with the start player marker on it.
   */
  private void createGameTable() {
	gameTable = new ArrayList<>();
	gameTable.add(Tiles.START);
  }

  /**
   * Check if this player has turn.
   *
   * @return true if this player has turn
   */
  private boolean thisPlayerHasTurn() {

	Object[] usernames = usersAndTheirReadinessState.keySet().toArray();
	List<Object> userNames = Arrays.stream(usernames).toList();
	ArrayList<String> users = new ArrayList<>();

	for (Object name : userNames) {
	  users.add((String) name);
	}

	return users.get(playerIndex).equals(users.get(currentPlayer));
  }

  /**
   * Update the walls, the pattern lines, and the scores of all players.
   * Method is called from the ClientServerConnection to actualize the wall without
   * notifying the view.
   *
   * @param wallsCoordinates the coordinates of the tiles that has to be placed in the wall
   * @param scores           the new scores
   */
  public void updateWallsAndScores(ArrayList<ArrayList<Point>> wallsCoordinates,
								   ArrayList<Integer> scores) {

	// actualize the walls of all players
	for (ArrayList<Point> wallCoordinates : wallsCoordinates) {
	  PlayerBoard playerBoard = playerBoards[wallsCoordinates.indexOf(wallCoordinates)];
	  for (Point point : wallCoordinates) {
		playerBoard.getWall()[point.x][point.y].setIsOnWall();
	  }
	}

	// actualize the pattern lines of all players
	for (PlayerBoard playerboard : playerBoards) {
	  playerboard.actualizePatternLines();
	  playerboard.getFloorLine().clear();
	}

	// actualize scores
	this.scores = scores;

  }

  // 1. following methods are to send signals to the server.
  // I.e. signals that don't need validation from the server but must be sent to the other players

  /**
   * Dispose the current game.
   */
  public void dispose(){
	connection.stop();
  }
  /**
   * Signal that this user left the room.
   *
   * @throws IOException if the client/server communication is interrupted
   */
  public void signalLogout() throws IOException {
	connection.stop();
  }

  /**
   * Signal that this user has stopped the game.
   *
   * @throws IOException if the client/server communication is interrupted
   */
  public void signalStopGame() throws IOException {
	connection.sendStopGameSignal();
  }

  /**
   * Signal that this user is ready to play.
   *
   * @throws IOException if the client/server communication is interrupted
   */
  public void signalReadyToPlay() throws IOException {

	usersAndTheirReadinessState.replace(nickname, true);
	connection.signalReadyToPlay();
  }

  /**
   * Signal that this player is not ready to play.
   *
   * @throws IOException if the client/server communication is interrupted
   */
  public void signalNotReady() throws IOException {

	usersAndTheirReadinessState.replace(nickname, false);
	connection.signalNotReadyToPlay();

  }

  /**
   * Signal the reply to restart request from another user.
   *
   * @param agree The reply to restart request
   * @throws IOException if the client/server communication is interrupted
   */
  public void signalReplyToRestartRequest(Boolean agree) throws IOException {
    connection.sendReplyToRestartRequest(agree);
  }

  /**
   * Signal that this player has just collected tiles.
   *
   * @param tileIndex    the type of the collected tiles
   * @param displayIndex the index of the tile source: -1 for center and 0,...,8 for
   *                     the factory displays
   * @throws IOException if the client/server communication is interrupted
   */
  public void signalTilesCollected(int displayIndex, int tileIndex) throws IOException {

	if (thisPlayerHasTurn()) {

	  collectTilesAndInformTheView(displayIndex, tileIndex);

	  TilesCollectedSignal tilesCollectedSignal = new TilesCollectedSignal(tileIndex, displayIndex);
	  connection.collectTiles(tilesCollectedSignal);

	}

  }

  /**
   * Signal that this user has restarted the game.
   */
  public void restartGame() {
	// resetResources();
	try {
	  connection.restartGame();
	} catch (IOException e) {
	  throw new RuntimeException(e);
	}
  }

  // 2. following methods are made to send requests to the server.
  //    I.e. signals that need validation from the server

  /**
   * Send a login request to the server.
   *
   * @param nickname  the nickname
   * @param roomName  the room name
   * @param ipAddress the ip-addresse
   * @throws IOException if the client/server communication is interrupted
   */
  public void requestLogin(String nickname, String roomName, String ipAddress) throws IOException {

	LoginRequest loginRequest = new LoginRequest(nickname, roomName, ipAddress);
	connection.sendLoginRequest(loginRequest);
	connection.readEventsFromOtherUsers();
  }

  /**
   * Send a request to the server to place tiles either in the pattern lines or in the floor line.
   *
   * @param rowIndex the index of the destination surface. -1 for floor line and 0,...,4 for
   *                 the pattern-lines-row
   * @throws IOException if the client/server communication is interrupted
   */
  public void requestPlaceTiles(int rowIndex) throws IOException {

	if (thisPlayerHasTurn()) {

	  PlaceTilesRequest placeTilesRequest = new PlaceTilesRequest(rowIndex);
	  connection.sendMoveRequest(placeTilesRequest);
	}

  }

  // 3. following methods are to inform the view about updates/new events in the game
  //    and the responses from server to the requests

  /**
   * Inform the view that the login-request of this client failed.
   *
   * @param cause the cause
   */
  public void informLoginFailed(String cause) {
	LoginFailedEvent loginFailedEvent = new LoginFailedEvent(cause);
	notifyListener(loginFailedEvent);
  }

  /**
   * Inform the view that the login-request was success.
   *
   * @param nickname   the name with which the user is logged in
   * @param usersReady a hash map that contains the list of the logged users and whether they are
   *                   ready for the game or not
   */
  public void informLoginSuccess(String nickname, String roomName,
								 LinkedHashMap<String, Boolean> usersReady) {

	this.nickname = nickname;
	this.roomName = roomName;
	this.usersAndTheirReadinessState.putAll(usersReady);
	this.usersAndTheirReadinessState.put(nickname, false);
	playerIndex = usersAndTheirReadinessState.size() - 1;

	LoginEvent loginEvent = new LoginEvent(nickname);
	notifyListener(loginEvent);

  }

  /**
   * Inform the view that a user joined the room.
   *
   * @param nickname the nickname of the new logged user
   */
  public void informUserJoined(String nickname) {

	usersAndTheirReadinessState.put(nickname, false);

	UserJoinedEvent userJoinedEvent = new UserJoinedEvent(nickname);
	notifyListener(userJoinedEvent);
  }

  /**
   * Inform the view that a user left the game.
   *
   * @param nickname the nickname of the player that just left
   */
  public void informUserLeftGame(String nickname) {

	usersAndTheirReadinessState.remove(nickname);

	for (String username : usersAndTheirReadinessState.keySet()) {
	  usersAndTheirReadinessState.replace(username, false);
	}

	UserLeftGameEvent logoutEvent = new UserLeftGameEvent();
	notifyListener(logoutEvent);
  }

  /**
   * Inform the view that a user left the room.
   *
   * @param nickname the nickname of the player that just left
   */
  public void informUserLeftRoom(String nickname) {

	usersAndTheirReadinessState.remove(nickname);

	UserLeftRoomEvent userLeftRoomEvent = new UserLeftRoomEvent();
	notifyListener(userLeftRoomEvent);
  }

  /**
   * Inform the view that the game has ended.
   *
   * @param ranking a hash map the contains the information about the game results.
   */
  public void informGameEnd(LinkedHashMap<Integer, Integer> ranking) {

	GameEndEvent gameEndEvent = new GameEndEvent(ranking);
	notifyListener(gameEndEvent);

  }

  /**
   * Inform the view, the current player has changed.
   *
   * @param playerIndex the index of the new current player
   */
  public void informTurn(int playerIndex) {

	currentPlayer = playerIndex;

	if (thisPlayerHasTurn()) {
	  TurnEvent turnEvent = new TurnEvent();
	  notifyListener(turnEvent);
	}
	else {
	  IsNotYourTurnEvent turnEvent = new IsNotYourTurnEvent();
	  notifyListener(turnEvent);
	}

//	else if (newValue instanceof IsNotYourTurnEvent){
//	  updateActivePlayerBoardView();
//
//	  String nextPlayerName = model.getNickname();
//	  String message = nextPlayerName + ", wait for your turn...";
//	  infoLabel.setText(message);
//	}

  }

  /**
   * Inform the view that has benn a change in the center of the play board.
   */
  public void collectTilesAndInformTheView(int indexOfCollectPlace, int tileIndex) {

	if (indexOfCollectPlace == -1) {

	  collectTilesFromTheCenter(tileIndex);

	  // notify the View
	  GameTableChangeEvent gameTableChangeEvent = new GameTableChangeEvent();
	  notifyListener(gameTableChangeEvent);

	  FloorLineChangeEvent floorLineChangeEvent = new FloorLineChangeEvent();
	  notifyListener(floorLineChangeEvent);

	} else {

	  collectTilesFromFactoryDisplay(tileIndex, indexOfCollectPlace);

	  // notify the View
	  GameTableChangeEvent gameTableChangeEvent = new GameTableChangeEvent();
	  notifyListener(gameTableChangeEvent);

	  FactoryDisplaysChangeEvent factoryDisplaysChangeEvent = new FactoryDisplaysChangeEvent();
	  notifyListener(factoryDisplaysChangeEvent);

	}

  }

  /**
   * Inform the view that has benn a change in the pattern lines and/or in the floor line.
   *
   * @param placeLocationIndex      the index of the row: -1 for floor line und 0,...,4 for
   *                                the rows in pattern lines
   * @param tilesAddedInPatternLine the list of tiles to be placed in the pattern lines
   * @param tilesAddedInFloorLine   the list of tiles to be placed in the floor line
   */
  public void informChangeInPatternLines(int placeLocationIndex,
										 ArrayList<Tiles> tilesAddedInPatternLine,
										 ArrayList<Tiles> tilesAddedInFloorLine) {
	  currentCollectedTiles.clear();
	  System.out.println("XXXXXXXXonline model inform change pattern line" + currentCollectedTiles);

	if (placeLocationIndex == -1) {

	  playerBoards[currentPlayer].placeTilesInFloorLine(tilesAddedInFloorLine);

	} else {

	  playerBoards[currentPlayer].placeTilesInPatternLines(placeLocationIndex,
		  tilesAddedInPatternLine);
	  playerBoards[currentPlayer].placeTilesInFloorLine(tilesAddedInFloorLine);

	  PatternLinesChangeEvent patternLinesChangeEvent = new PatternLinesChangeEvent();
	  notifyListener(patternLinesChangeEvent);

	}

	FloorLineChangeEvent floorLineChangeEvent = new FloorLineChangeEvent();
	notifyListener(floorLineChangeEvent);

  }


  /**
   * Inform the view that has been a change in the wall.
   *
   * @param wallsCoordinates the coordinates of the tiles that has to be placed in the wall
   * @param scores           the new scores
   */
  public void informChangeInWall(ArrayList<ArrayList<Point>> wallsCoordinates,
								 ArrayList<Integer> scores) {

	updateWallsAndScores(wallsCoordinates, scores);

	WallChangeEvent wallChangeEvent = new WallChangeEvent();
	notifyListener(wallChangeEvent);

  }

  /**
   * Inform the view that the tile can not be placed in the requested place.
   */
  public void informInvalidMove() {
	PlaceTilesFailEvent placeTilesFailEvent = new PlaceTilesFailEvent();
	notifyListener(placeTilesFailEvent);
  }

  /**
   * Inform the view that the turn of another player has just finished.
   */
  public void informPlayerTurnFinished() {
	PlayerTurnFinishedEvent playerTurnFinishedEvent = new PlayerTurnFinishedEvent();
	notifyListener(playerTurnFinishedEvent);
  }

  /**
   * Inform the view that this player is ready to play.
   *
   * @param nickname the nickname of the player
   */
  public void informUserReady(String nickname) {
	usersAndTheirReadinessState.replace(nickname, true);
	UserReadyEvent userReadyEvent = new UserReadyEvent();
	notifyListener(userReadyEvent);
  }

  /**
   * Inform the view that this player is not ready to play.
   *
   * @param nickname the nickname of the player
   */
  public void informUserNotReady(String nickname) {
	usersAndTheirReadinessState.replace(nickname, false);
	UserNotReadyEvent userNotReadyEvent = new UserNotReadyEvent();
	notifyListener(userNotReadyEvent);
  }

  /**
   * Create/set the start attributs of the game according to the information the server sent.
   *
   * @param currentPlayer   the player that has to play
   * @param factoryDisplays the factory displays
   */
  public void startGame(int currentPlayer, FactoryDisplay factoryDisplays) {

	gamePlayers = new GamePlayers(usersAndTheirReadinessState.size(),
		new ArrayList<>(usersAndTheirReadinessState.keySet()));

	createPlayerBoards();
	createGameTable();

	this.factoryDisplays = factoryDisplays;
	this.currentPlayer = currentPlayer;
	this.scores = new ArrayList<>(Collections.nCopies(usersAndTheirReadinessState.size(), 0));
	this.currentCollectedTiles = new ArrayList<>();

	CreateViewEvent createViewEvent = new CreateViewEvent();
	notifyListener(createViewEvent);

	StartGameEvent startGameEvent = new StartGameEvent();
	notifyListener(startGameEvent);

  }

  /**
   * Inform the view that the next round will start now.
   */
  public void informStartNextRound(int currentPlayer, FactoryDisplay newFactoryDisplays) {

	gameTable.add(0, Tiles.START);

	for (int i = 0; i < factoryDisplays.getAllDisplays().size(); i++) {
	  factoryDisplays.getAllDisplays().get(i).addAll(newFactoryDisplays.getAllDisplays().get(i));
	}

	this.currentPlayer = currentPlayer;

	StartNextRoundEvent startNextRoundEvent = new StartNextRoundEvent();
	notifyListener(startNextRoundEvent);

  }

  public void informRestartRequest(String nickname) {
    notifyListener(new RestartRequestEvent(nickname));
  }
  public void informRestartRequestRejected() {
    notifyListener(new RestartRequestRejectedEvent());
  }

  public void informRestartGame(int currentPlayer, FactoryDisplay factoryDisplays) {
    gamePlayers = new GamePlayers(usersAndTheirReadinessState.size(),
        new ArrayList<>(usersAndTheirReadinessState.keySet()));

    createPlayerBoards();
    createGameTable();

    this.factoryDisplays = factoryDisplays;
    this.currentPlayer = currentPlayer;
    this.scores = new ArrayList<>(Collections.nCopies(usersAndTheirReadinessState.size(), 0));

    notifyListener(new RestartEvent());
    notifyListener(new StartGameEvent());
  }

  // the GUI uses the following methods to obtain information about the actual state of the game:

  /**
   * get the users and their readiness to play
   *
   * @return a hash map tha contains a list of the users in the room and whether they are
   * ready to play
   */
  public LinkedHashMap<String, Boolean> getUsersAndTheirReadinessState() {
	return usersAndTheirReadinessState;
  }

  @Override
  public int getPlayerCount() {
	return usersAndTheirReadinessState.size();
  }

  @Override
  public GamePlayers getGamePlayers() {
	return gamePlayers;
  }

  @Override
  public FactoryDisplay getFactoryDisplays() {
	return factoryDisplays;
  }

  @Override
  public int getCurrentPlayer() {
	return currentPlayer;
  }

  @Override
  public int getCurrentDisplay() {
	return currentDisplay;
  }

  @Override
  public List<Tiles> getGameTable() {
	return gameTable;
  }

  @Override
  public PlayerBoard getPlayerBoard(int player) {
	return playerBoards[player];
  }

  @Override
  public ArrayList<Integer> getScores() {
	return scores;
  }

  /**
   * Get the nickname of this user.
   *
   * @return the nickname
   */
  @Override
  public String getNickname() {
	return nickname;
  }

  /**
   * Get the name of the room in which this player is logged.
   *
   * @return the name of the room
   */
  @Override
  public String getRoomName() {
	return roomName;
  }

}
