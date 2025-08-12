package de.lmu.ifi.sosylab.server;

import de.lmu.ifi.sosylab.shared.Bag;
import de.lmu.ifi.sosylab.shared.FactoryDisplay;
import de.lmu.ifi.sosylab.shared.JsonMessage;
import de.lmu.ifi.sosylab.shared.Lid;
import de.lmu.ifi.sosylab.shared.PlayerBoard;
import de.lmu.ifi.sosylab.shared.Tiles;
import de.lmu.ifi.sosylab.shared.WallTile;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.json.JSONObject;

/**
 * Manage an Azul game in a game room.
 */

public class GameRoom {
  private final String roomName;
  private Phase phase;
  private final List<UserMessageHandler> userMessageHandlers;
  private final List<User> allUsersInGame;
  private List<Integer> scoresOfAllUsers;

  private Bag bag;
  private Lid lid;
  private PlayerBoard[] playerBoards;
  private FactoryDisplay factoryDisplays;
  private List<Tiles> gameTable;
  private List<Tiles> currentCollectedTiles;
  private User currentPlayerInGame;


  /**
   * Construct the game room.
   *
   * @param roomName the name of the room.
   */
  public GameRoom(String roomName) {
    this.roomName = roomName;
    this.phase = Phase.WAITING_FOR_START;

    userMessageHandlers = new ArrayList<>();
    allUsersInGame = new ArrayList<>();
  }

  public void handleUserLeftAndRoomNotEmpty() throws IOException {
    //move other users to waiting room
    for (User singleUser : allUsersInGame) {
      singleUser.setReadyToPlay(false);
    }
    setPhaseToWaitingForStart();
    dispose();
  }

  private void dispose() {
    scoresOfAllUsers = null;
    bag = null;
    lid = null;
    playerBoards = null;
    factoryDisplays = null;
    gameTable = null;
    currentCollectedTiles = null;
    currentPlayerInGame = null;
  }

  public void removeLeftUserFromRoom(String nickname) {
    //remove user's score
    //remove user from room
    for (int playerIndex = 0; playerIndex < getTotalNumberOfPlayers(); playerIndex++) {
      if (Objects.equals(allUsersInGame.get(playerIndex).getNickName(), nickname)) {
        allUsersInGame.remove(playerIndex);
      }
    }
  }
  
  public boolean isGameRoomEmpty() {
    return getTotalNumberOfPlayers() == 0;
  }

  private void setPhaseToWaitingForStart() {
    phase = Phase.WAITING_FOR_START;
  }

  public void setPhaseToOngoingGame() {
    this.phase = Phase.ONGOING_GAME;
  }

  public void startGame() {
    setPhaseToOngoingGame();
    bag = new Bag();
    lid = new Lid();
    createPlayerBoards();
    createFactoryDisplays();
    scoresOfAllUsers = new ArrayList<>();
    setInitialScores();

    gameTable = new ArrayList<>();
    gameTable.add(Tiles.START);
    fillFactoryDisplay();
    setStartGameUser();
    this.currentCollectedTiles = new ArrayList<>();
  }

  public void restartGame() {
    bag = new Bag();
    lid = new Lid();
    createPlayerBoards();
    createFactoryDisplays();
    scoresOfAllUsers.clear();
    setInitialScores();
    gameTable.clear();
    gameTable.add(Tiles.START);
    fillFactoryDisplay();
    setStartGameUser();
    currentCollectedTiles = new ArrayList<>();
  }


  public void collectTilesFromDisplay(int displayIndex, int tileIndex) throws IOException {
    if (currentCollectedTiles.size() != 0) {
      return;
    }
    ArrayList<Tiles> selectedDisplay = factoryDisplays.getAllDisplays().get(displayIndex);
    if (selectedDisplay.size() != 0) {
      Tiles[] collectTiles = factoryDisplays.removeTiles(selectedDisplay, tileIndex);
      //set the current collected tiles of the game
      setCurrentCollectedTiles(collectTiles);
      //store the remaining tiles from the display
      ArrayList<Tiles> discardedTiles = factoryDisplays.remainTiles(
          selectedDisplay, collectTiles.length);
      //clear the remaining tiles from the display
      selectedDisplay.clear();
      if (discardedTiles.size() != 0) {
        gameTable.addAll(discardedTiles);
      }
      broadcast(getCurrentHandler(), JsonMessage.somebodyCollectedTiles(
          getCurrentPlayerInGame().getNickName(),displayIndex,tileIndex));
    }
  }

  public void collectTilesFromGameTable(int tileIndex) throws IOException {
    if (currentCollectedTiles.size() != 0) {
      return;
    }
    //check whether game table is empty or only has the starting player marker
    //if so, GUI de-activate the game table automatically
    if (gameTable.size() == 0 || (gameTable.size() == 1 && gameTable.get(0) == Tiles.START)) {
      //do nothing because the game table is either empty or has just the starting player marker
      //and therefore the player cannot collect tiles from table
    } else {
      if (gameTable.get(0) == Tiles.START) {
        PlayerBoard currentBoard = playerBoards[getIndexOfCurrentPlayerInGame()];
        List<Tiles> floorLine = currentBoard.getFloorLine();
        floorLine.add(0, Tiles.START);
      }
      Tiles tileType = gameTable.get(tileIndex);
      int countTiles = 0;
      for (int tileId = 0; tileId < gameTable.size(); tileId++) {
        if (gameTable.get(tileId) == tileType) {
          countTiles = countTiles + 1;
        }
      }
      gameTable.removeIf(n -> (n == tileType));
      Tiles[] collectTiles = new Tiles[countTiles];
      Arrays.fill(collectTiles, tileType);
      //set the current collected tiles of the game
      setCurrentCollectedTiles(collectTiles);
      if (gameTable.size() > 0) {
        if (gameTable.get(0) == Tiles.START) {
          gameTable.remove(0);
        }
      }
    }
    broadcast(getCurrentHandler(), JsonMessage.somebodyCollectedTiles(
        getCurrentPlayerInGame().getNickName(), -1, tileIndex));
  }

  private UserMessageHandler getCurrentHandler() {
    UserMessageHandler handlerForCurrentPlayer =
        userMessageHandlers.get(getIndexOfCurrentPlayerInGame());
    return handlerForCurrentPlayer;
  }

  public void placeTilesToPatternLine(int row) throws IOException {
    PlayerBoard currentBoard = playerBoards[getIndexOfCurrentPlayerInGame()];
    //do nothing if no tiles have been selected yet
    if (currentCollectedTiles.size() == 0) {
      return;
    }
    Tiles tileType = currentCollectedTiles.get(0);
    int tileCount = currentCollectedTiles.size();
    Tiles[][] patternLines = currentBoard.getPatternLines();
    WallTile[][] wall = currentBoard.getWall();

    //check whether place tiles is possible in this pattern lines
    //if yes, place tiles, if no, place tiles to floor line
    if (checkAvailableLines(tileType, patternLines, wall)) {
      //if there are still available lines, check wall rule
      if (!checkWallRule(row, wall, tileType)) {
        //if wall rule is defied, send invalid move to current player
        handleCommunicationWhenInvalidMove();
        return;
      }
      //wall rule is not defied, continuing checking other rules
      //check pattern lines, if the line is empty, place tiles, deal with excess tiles
      if (patternLines[row][row] == null) {
        placeTilesToEmptyLine(row, tileCount, tileType, patternLines);
      } else {
        //if the line has the same type of tiles, place tiles and deal with excess tiles
        if (patternLines[row][row] == tileType) {
          //if the line is full, send invalid move
          if (patternLines[row][0] != null) {
            handleCommunicationWhenInvalidMove();
            return;
          }
          placeTilesToLineWithSameTiles(row, tileCount, tileType, patternLines);
        } else {
          //if the line has a different type of tiles, cannot play tiles and send invalid move
          placeTilesToLineWithDifferentTiles(row, tileType, patternLines);
          return;
        }
      }
    } else {
      handleNoAvailablePatternLines();
      handleCommunicationsWhenValidMove(-1, new ArrayList<>(), currentCollectedTiles);
      //change to next player and send turn json message after valid move
      clearCurrentCollectedTiles();
    }
    if (isRoundFinished()) {
      moveTilesToWall();
      //move tiles to wall (send updated rows of all players' walls and all players' scores to model
    } else {
      setNextPlayerInGame();
      handleCommunicationWhenPlayerChanged();
    }
  }

  public void placeTilesToFloorLine() throws IOException {
    if (currentCollectedTiles.size() == 0) {
      return;
    }
    PlayerBoard currentBoard = playerBoards[getIndexOfCurrentPlayerInGame()];
    currentBoard.getFloorLine().addAll(currentCollectedTiles);
    handleCommunicationsWhenValidMove(-1, new ArrayList<>(), currentCollectedTiles);
    clearCurrentCollectedTiles();

    if (isRoundFinished()) {
      moveTilesToWall();
    } else {
      //if a round is not yet finished, change to next player
      setNextPlayerInGame();
      handleCommunicationWhenPlayerChanged();
    }
  }

  public boolean isRoundFinished() {
    boolean isRoundFinished = true;
    //check whether there are tiles on displays and on game table
    if (gameTable.size() != 0) {
      //check game table
      return false;
    } else {
      //check factory displays
      ArrayList<ArrayList<Tiles>> allDisplays = factoryDisplays.getAllDisplays();
      for (int displayIndex = 0; displayIndex < allDisplays.size(); displayIndex++) {
        ArrayList<Tiles> singleDisplay = allDisplays.get(displayIndex);
        if (singleDisplay.size() != 0) {
          return false;
        }
      }
    }
    return isRoundFinished;
  }

  public void moveTilesToWall() throws IOException {
    //store the calculated gain points of all players
    ArrayList<Integer> allGainPoints =
        new ArrayList<>(Collections.nCopies(getTotalNumberOfPlayers(), 0));
    List<List<Point>> updatedPartsOfWallsForAllPlayer = new ArrayList<>();
    for (int boardIndex = 0; boardIndex < getTotalNumberOfPlayers(); boardIndex++) {
      List<Point> updatedPartsOfWallForSinglePlayer = new ArrayList<>();
      PlayerBoard currentBoard = playerBoards[boardIndex];
      Tiles[][] patternLines = currentBoard.getPatternLines();
      WallTile[][] wall = currentBoard.getWall();
      for (int row = 0; row < 5; row++) {
        //move a tile to wall if the pattern line is completed
        if (patternLines[row][0] != null) {
          Tiles tileType = patternLines[row][0];
          //add a tile to wall
          for (int col = 0; col < wall[row].length; col++) {
            if (wall[row][col].getTile() == tileType) {
              wall[row][col].setIsOnWall();
              Point addedTilesOnWall = new Point(row, col);
              updatedPartsOfWallForSinglePlayer.add(addedTilesOnWall);
              allGainPoints.set(boardIndex,
                  allGainPoints.get(boardIndex) + calculateSingleTileGainPoints(wall, row, col));
            }
          }
          //discard remaining tiles and add them to bag
          int discardTilesCount = patternLines[row].length - 1;
          List<Tiles> discardTiles = new ArrayList<>();
          for (int tileIndex = 0; tileIndex < discardTilesCount; tileIndex++) {
            discardTiles.add(tileType);
          }
          discardTilesToLid(discardTiles);
          //clear the whole pattern line
          Arrays.fill(patternLines[row], null);
        }
      }
      updatedPartsOfWallsForAllPlayer.add(updatedPartsOfWallForSinglePlayer);
    }
    handleCheckEndGame(allGainPoints, updatedPartsOfWallsForAllPlayer);
  }



  private void handleCheckEndGame(ArrayList<Integer> allGainPoints, List<List<Point>> updatedPartsOfWallsForAllPlayer) throws IOException {
    //check whether the game is finished
    if (!checkEndGame()) {
      //if not yet finished, calculate scores
      //set the scoring of the game to the current calculated scores
      scoringAfterEachRound(allGainPoints);
      broadcastToAll(JsonMessage.updateWallsAndScores(updatedPartsOfWallsForAllPlayer, scoresOfAllUsers));
      //clear floor line: move tiles in floor line to lid (except the starting player marker)
      clearFloorLine();
      startNextRound();
    } else {
      scoringAfterEachRound(allGainPoints);
      //end game scoring
      endGameScoring();
      broadcastToAll(JsonMessage.updateWallsAndScores(updatedPartsOfWallsForAllPlayer, scoresOfAllUsers));
      LinkedHashMap<Integer, Integer> ranking = calculateRanking();
      broadcastToAll(JsonMessage.endOfGame(ranking));
    }
  }



  private LinkedHashMap<Integer, Integer> calculateRanking() {
    HashMap<Integer, Integer> playersWithScores = new HashMap<>();
    for (int playerIndex = 0; playerIndex < getTotalNumberOfPlayers(); playerIndex++) {
      playersWithScores.put(playerIndex, scoresOfAllUsers.get(playerIndex));
    }
    LinkedHashMap<Integer, Integer> ranking = new LinkedHashMap<>();
    HashMap<Integer, Integer> playersWithAscendingRanking = new HashMap<>();
    Set<Integer> setOfNoDuplicatedScores = new HashSet<>();
    Collections.sort(scoresOfAllUsers);
    setOfNoDuplicatedScores.add(scoresOfAllUsers.get(0));
    for (Integer score : scoresOfAllUsers) {
      if (!Objects.equals(score, scoresOfAllUsers.get(0))) {
        //run through the list of scores to check whether there are duplicates
        if (!setOfNoDuplicatedScores.add(score)) {
          //there are duplicated scores in the list of scores
          //handle duplicates by comparing completed columns
          ranking = handleTiesBeforeCalculatingRows();
          break;
        }
      }
      //there are no duplicated scores in the list of scores
      ranking = calculateRankingWhenNoDuplicates(playersWithScores, playersWithAscendingRanking);
    }
    return ranking;
  }



  private LinkedHashMap<Integer, Integer> calculateRankingWhenNoDuplicates(
      HashMap<Integer, Integer> playersWithScores,
      HashMap<Integer, Integer> playersWithAscendingRanking) {
    LinkedHashMap<Integer, Integer> ranking = new LinkedHashMap<>();
    for (int scoreIndex = 0; scoreIndex < getTotalNumberOfPlayers(); scoreIndex++) {
      int rank = getTotalNumberOfPlayers() - scoreIndex;
      for (Map.Entry<Integer, Integer> entry : playersWithScores.entrySet()) {
        if (Objects.equals(entry.getValue(), scoresOfAllUsers.get(scoreIndex))) {
          playersWithAscendingRanking.put(entry.getKey(), rank);
          break;
        }
      }
    }
    for (int playerIndex = 0; playerIndex < getTotalNumberOfPlayers(); playerIndex++) {
      for (Map.Entry<Integer, Integer> entry : playersWithAscendingRanking.entrySet()) {
        if (Objects.equals(entry.getKey(), playerIndex)) {
          ranking.put(playerIndex, entry.getValue());
          break;
        }
      }
    }
    return ranking;
  }



  private LinkedHashMap<Integer, Integer> handleTiesBeforeCalculatingRows() {
    LinkedHashMap<Integer, Integer> ranking = new LinkedHashMap<>();
    HashMap<Integer, Integer> playersWithAscendingRanking = new HashMap<>();
    ArrayList<Integer> completedRowsOfAllPlayers = calculateCompletedRowsOnWalls();
    //add the score of the player and her completed rows together
    //if there is still a tie, players with the same scores are ranked the same
    ArrayList<Integer> scorePlusCompletedRows = new ArrayList<>();
    for (int playerIndex = 0; playerIndex < getTotalNumberOfPlayers(); playerIndex++) {
      scorePlusCompletedRows.add(
          scoresOfAllUsers.get(playerIndex) + completedRowsOfAllPlayers.get(playerIndex));
    }
    //store the score (plus completed rows) and the corresponding players together
    HashMap<Integer, Integer> playersWithScoresAndRows = new HashMap<>();
    for (int playerIndex = 0; playerIndex < getTotalNumberOfPlayers(); playerIndex++) {
      playersWithScoresAndRows.put(playerIndex, scorePlusCompletedRows.get(playerIndex));
    }
    //sort the scores (plus completed rows) in ascending order
    Collections.sort(scorePlusCompletedRows);
    Set<Integer> setOfNoDuplicates = new HashSet<>();
    setOfNoDuplicates.add(scorePlusCompletedRows.get(0));
    for (Integer scorePlusRows : scorePlusCompletedRows) {
      if (!Objects.equals(scorePlusRows, scorePlusCompletedRows.get(0))) {
        if (!setOfNoDuplicates.add(scorePlusRows)) {
          //there are still duplicates after adding completed rows
          ranking = handlesTiesAfterCalculatingRows(playersWithScoresAndRows);
          break;
        }
      }
      //there are no duplicates after adding scores and completed column numbers together
      ranking = calculateRankingWhenNoDuplicates(playersWithScoresAndRows, playersWithAscendingRanking);
    }
    return ranking;
  }



  private ArrayList<Integer> calculateCompletedRowsOnWalls() {
    //store the completed row numbers
    ArrayList<Integer> completedRowsOfAllPlayers = new ArrayList<>();
    for (int playerIndex = 0; playerIndex < getTotalNumberOfPlayers(); playerIndex++) {
      PlayerBoard boardOfSinglePlayer = playerBoards[playerIndex];
      WallTile[][] wallOfSinglePlayer = boardOfSinglePlayer.getWall();
      //calculate the completed rows on wall
      int completedRowsOfSinglePlayer = 0;
      int countTilesOnRow = 0;
      for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
        //run through the row and calculate the tiles on the row
        for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
          if (!wallOfSinglePlayer[rowIndex][columnIndex].getIsOnWall()) {
            break;
          }
          countTilesOnRow = countTilesOnRow + 1;
        }
        //if there are 5 tiles on the row, add one completed row
        if (countTilesOnRow == 5) {
          completedRowsOfSinglePlayer = completedRowsOfSinglePlayer + 1;
        }
      }
      //store the completed row numbers
      completedRowsOfAllPlayers.add(completedRowsOfSinglePlayer);
    }
    return completedRowsOfAllPlayers;
  }




  private LinkedHashMap<Integer, Integer> handlesTiesAfterCalculatingRows(
      HashMap<Integer, Integer> playersWithScoresAndRows) {
    LinkedHashMap<Integer, Integer> ranking = new LinkedHashMap<>();
    for (int rank = 1; rank < getTotalNumberOfPlayers(); rank++){
      if (!playersWithScoresAndRows.isEmpty()) {
        int maxScore = 0;
        for (int playerIndex : playersWithScoresAndRows.keySet()) {
          if (maxScore < playersWithScoresAndRows.get(playerIndex)) {
            maxScore = playersWithScoresAndRows.get(playerIndex);
          }
        }
        ArrayList<Integer> remainPlayers = new ArrayList<>(playersWithScoresAndRows.keySet());
        Collections.sort(remainPlayers);
        for (int remainPlayerIndex : remainPlayers) {
          if (playersWithScoresAndRows.get(remainPlayerIndex) == maxScore) {
            playersWithScoresAndRows.remove(remainPlayerIndex);
            ranking.put(remainPlayerIndex, rank);
          }
        }
      }
    }
    return ranking;
  }




  private void startNextRound() throws IOException {
    //refill factory display
    fillFactoryDisplay();
    //start with player who has the starting player marker, send the index of this player to GUI
    //so that GUI deactivates the player boards of other players
    for (int boardIndex = 0; boardIndex < getTotalNumberOfPlayers(); boardIndex++) {
      PlayerBoard currentBoard = playerBoards[boardIndex];
      ArrayList<Tiles> floorLine = currentBoard.getFloorLine();
      if (floorLine.size() > 0) {
        if (floorLine.get(0) == Tiles.START) {
          setFirstPlayerInNewRound(boardIndex);
          floorLine.clear();
        }
      }
    }
    //the starting player places the starting player marker (in floor line) to the game table
    gameTable.add(0, Tiles.START);
    int indexOfStartNewRoundPlayer = getIndexOfCurrentPlayerInGame();
    broadcastToAll(JsonMessage.startNextRound(getAllFactoryDisplays(), indexOfStartNewRoundPlayer));
    //todo start next round event is kept by model (repaint wall, pattern lines, center, inform player who's turn)
  }


  private void clearFloorLine() {
    //move tiles (except the starting player marker) to lid
    for (int boardIndex = 0; boardIndex < getTotalNumberOfPlayers(); boardIndex++) {
      PlayerBoard currentBoard = playerBoards[boardIndex];
      ArrayList<Tiles> floorLine = currentBoard.getFloorLine();
      if (floorLine.size() > 0) {
        List<Tiles> discardTiles = new ArrayList<>();
        for (Tiles tile : floorLine) {
          if (tile != Tiles.START) {
            discardTiles.add(tile);
          }
        }
        discardTilesToLid(discardTiles);
        floorLine.removeAll(discardTiles);
        //(finished) (Diego) inform all users about floor line change
        //(all tiles except starting player marker are gone to lid)
      }
    }
  }

  private void scoringAfterEachRound(ArrayList<Integer> allGainPoints) {
    for (int boardIndex = 0; boardIndex < getTotalNumberOfPlayers(); boardIndex++) {
      PlayerBoard currentBoard = playerBoards[boardIndex];
      ArrayList<Tiles> floorLine = currentBoard.getFloorLine();
      //calculate the minus points for a single player
      int minusPoints = calculateSinglePlayerMinusPoints(floorLine.size());
      //get the gain points for a single player
      int gainPoints = allGainPoints.get(boardIndex);
      //calculate the score for a single player
      if (scoresOfAllUsers.get(boardIndex) + gainPoints - minusPoints > 0) {
        scoresOfAllUsers.set(boardIndex, scoresOfAllUsers.get(boardIndex) + gainPoints - minusPoints);
      } else {
        scoresOfAllUsers.set(boardIndex, 0);
      }
    }
  }

  private int calculateSinglePlayerMinusPoints(int floorSize) {
    int minus;
    //set the maximum size of the floor line to be 7
    if (floorSize > 7) {
      floorSize = 7;
    }
    switch (floorSize) {
      case 1:
        minus = 1;
        break;
      case 2:
        minus = 2;
        break;
      case 3:
        minus = 4;
        break;
      case 4:
        minus = 6;
        break;
      case 5:
        minus = 8;
        break;
      case 6:
        minus = 11;
        break;
      case 7:
        minus = 14;
        break;
      default:
        minus = 0;
    }
    return minus;
  }

  private void endGameScoring() {
    for (int boardIndex = 0; boardIndex < getTotalNumberOfPlayers(); boardIndex++) {
      PlayerBoard currentBoard = playerBoards[boardIndex];
      WallTile[][] wall = currentBoard.getWall();
      int columnBonus = colBonus(wall);
      int rowBonus = rowBonus(wall);
      int colorBonus = colorBonus(wall);
      scoresOfAllUsers.set(boardIndex, columnBonus + rowBonus + colorBonus + scoresOfAllUsers.get(boardIndex));
    }
  }

  private int colBonus(WallTile[][] wall) {
    int bonus = 0;
    for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
      boolean isFull = true;
      for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
        if (!wall[rowIndex][columnIndex].getIsOnWall()) {
          isFull = false;
          break;
        }
      }
      if (isFull) {
        bonus += 7;
      }
    }
    return bonus;
  }

  private int rowBonus(WallTile[][] wall) {
    int bonus = 0;
    for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
      boolean isFull = true;
      for (int columnIndex = 0; columnIndex< 5; columnIndex++) {
        if (!wall[rowIndex][columnIndex].getIsOnWall()) {
          isFull = false;
          break;
        }
      }
      if (isFull) {
        bonus += 2;
      }
    }
    return bonus;
  }

  private int colorBonus(WallTile[][] wall) {
    int bonus = 0;
    // check for each color whether there are 5 tiles of the color on the wall
    for (Tiles tile : Tiles.values()) {
      int colorCount = 0;
      for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
        for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
          if (wall[rowIndex][columnIndex].getIsOnWall() && wall[rowIndex][columnIndex].getTile() == tile) {
            colorCount++;
          }
        }
      }
      if (colorCount == 5) {
        bonus += bonus;
      }
    }
    return bonus;
  }

  private boolean checkEndGame() {
    for (int boardIndex = 0; boardIndex < getTotalNumberOfPlayers(); boardIndex++) {
      PlayerBoard currentBoard = playerBoards[boardIndex];
      WallTile[][] wall = currentBoard.getWall();

      for (int row = 0; row < 5; row++) {
        int tileCount = 0;
        for (int column = 0; column < 5; column++) {
          if (wall[row][column].getIsOnWall()) {
            tileCount = tileCount + 1;
          }
        }
        if (tileCount == 5) {
          return true;
        }
      }
    }
    return false;
  }

  private void discardTilesToLid(List<Tiles> discardTiles) {
    lid.discardTilesToLid(discardTiles);
  }

  private int calculateSingleTileGainPoints(WallTile[][] wall, int row, int column) {
    int gainPoints = 0;
    //count the tiles to the left of this tile
    int tilesOnLeft = 0;
    for (int columnIndex = column - 1; columnIndex >= 0; columnIndex--) {
      if (!wall[row][columnIndex].getIsOnWall()) {
        break;
      }
      tilesOnLeft = tilesOnLeft + 1;
    }
    //count the tiles to the right of this tile
    int tilesOnRight = 0;
    for (int columnIndex = column + 1; columnIndex < 5; columnIndex++) {
      if (!wall[row][columnIndex].getIsOnWall()) {
        break;
      }
      tilesOnRight = tilesOnRight + 1;
    }
    //count the tiles above this tile
    int tilesAbove = 0;
    for (int rowIndex = row - 1; rowIndex >= 0; rowIndex--) {
      if (!wall[rowIndex][column].getIsOnWall()) {
        break;
      }
      tilesAbove = tilesAbove + 1;
    }
    //count the tiles below this tile
    int tilesBelow = 0;
    for (int rowIndex = row + 1; rowIndex < 5; rowIndex++) {
      if (!wall[rowIndex][column].getIsOnWall()) {
        break;
      }
      tilesBelow = tilesBelow + 1;
    }
    // if there are linked tiles vertically
    if (tilesAbove + tilesBelow > 0) {
      gainPoints += 1 + tilesAbove + tilesBelow;
    }
    // if there are linked tiles horizontally
    if (tilesOnLeft + tilesOnRight > 0) {
      gainPoints += 1 + tilesOnRight + tilesOnLeft;
    }
    // if there are no linked tiles vertically and horizontally
    if (tilesAbove + tilesBelow + tilesOnLeft + tilesOnRight == 0) {
      gainPoints = 1;
    }
    return gainPoints;
  }


  private void discardTilesToFloorLine(List<Tiles> excessTiles) {
    PlayerBoard currentBoard = playerBoards[getIndexOfCurrentPlayerInGame()];
    List<Tiles> floorLine = currentBoard.getFloorLine();
    floorLine.addAll(excessTiles);

    //FloorLineChangeEvent floorLineChangeEvent = new FloorLineChangeEvent();
    //notifyListener(floorLineChangeEvent);
  }

  private boolean checkAvailableLines(Tiles tileType, Tiles[][] patternLines, WallTile[][] wall) {
    //check the first positions of all pattern lines
    for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
      //if the line is an empty line, break the loop and check the next condition
      if (patternLines[rowIndex][patternLines[rowIndex].length - 1] == null) {
        break;
      }
      //if it is not an empty line, check the tile type on the line
      //if it has the same tile type, and it is not full, return true
      if (patternLines[rowIndex][patternLines[rowIndex].length - 1] == tileType
          && patternLines[rowIndex][0] == null) {
        return true;
      }
      //if it's not that it has the same tile type and is not full, continue checking other lines
      //until finish checking the whole pattern lines
    }
    //find one empty line where its wall doesn't have the same tile type
    //if there is such a line, break the loop and return true
    //if there is no such a line, check whether there are lines that have the same tile type and is
    //not full, if yes, return true, otherwise return false
    for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
      if (patternLines[rowIndex][patternLines[rowIndex].length - 1] == null) {
        //if it is an empty line, check wall
        if (Arrays.stream(wall[rowIndex]).anyMatch(
            entry -> entry.getIsOnWall() && entry.getTile() == tileType)) {
          continue;
        }
        return true;
      }
    }
    //check whether there are lines that have the same tile type and is not full
    for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
      if (patternLines[rowIndex][patternLines[rowIndex].length - 1] == tileType
          && patternLines[rowIndex][0] == null) {
        return true;
      }
    }
    return false;
  }

  private boolean checkWallRule(int row, WallTile[][] wall, Tiles tileType) {
    //return false if place tiles fail, send invalid move
    //if there are still available lines, check the status of the wall
    //check whether the corresponding line of the wall has the same tile
    //if so, cannot place tile
    for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
      if (wall[row][columnIndex].getIsOnWall()) {
        if (wall[row][columnIndex].getTile() == tileType) {
          return false;
        }
      }
    }
    return true;
  }

  private void placeTilesToLineWithSameTiles(int row, int tileCount, Tiles tileType,
                                             Tiles[][] patternLines) throws IOException {
    if (patternLines[row][row] == tileType) {
      int existedTiles = 0;
      for (int column = 0; column < patternLines[row].length; column++) {
        if (patternLines[row][column] != null) {
          existedTiles = existedTiles + 1;
        }
      }
      int emptyBlocks = patternLines[row].length - existedTiles;
      if (emptyBlocks >= tileCount) {
        for (int column = row - existedTiles; column > row - existedTiles - tileCount; column--) {
          patternLines[row][column] = tileType;
        }
        //no excess tiles
        handleCommunicationsWhenValidMove(row, currentCollectedTiles, new ArrayList<>());
      } else {
        for (int column = row - existedTiles; column >= 0; column--) {
          patternLines[row][column] = tileType;
        }
        //deal with excess tiles
        int excessTilesCount = tileCount - emptyBlocks;
        List<Tiles> excessTiles = new ArrayList<>(excessTilesCount);
        for (int tileIndex = 0; tileIndex < excessTilesCount; tileIndex++) {
          excessTiles.add(tileType);
        }
        discardTilesToFloorLine(excessTiles);
        //remove excess tiles from current collected tiles
        System.out.println("current collected tiles before removing: " + currentCollectedTiles);
        int tilesPlacedToPatternLineCount = emptyBlocks;
        ArrayList<Tiles> tilesPlacedToPatternLine = new ArrayList<>();
        for (int tileIndex = 0; tileIndex < tilesPlacedToPatternLineCount; tileIndex++) {
          tilesPlacedToPatternLine.add(tileType);
        }
        System.out.println("after removing: " + tilesPlacedToPatternLine);
        handleCommunicationsWhenValidMove(row, tilesPlacedToPatternLine, excessTiles);
      }
      clearCurrentCollectedTiles();
    }
  }

  private void placeTilesToEmptyLine(int row, int tileCount, Tiles tileType,
                                    Tiles[][] patternLines) throws IOException {
    int lineLength = patternLines[row].length;
    if (tileCount <= lineLength) {
      if (patternLines[row][row] == null) {
        for (int index = 0; index < tileCount; index++) {
          patternLines[row][lineLength - (tileCount - index)] = tileType;
        }
        handleCommunicationsWhenValidMove(row, currentCollectedTiles, new ArrayList<>());
      }
    } else {
      List<Tiles> tilesPlacedToPatternLine = new ArrayList<>(lineLength);
      if (patternLines[row][row] == null) {
        for (int index = 0; index < lineLength; index++) {
          patternLines[row][index] = tileType;
          tilesPlacedToPatternLine.add(tileType);
        }
      }
      //deal with excess tiles
      int excessTilesCount = tileCount - lineLength;
      List<Tiles> excessTiles = new ArrayList<>(excessTilesCount);
      for (int tileIndex = 0; tileIndex < excessTilesCount; tileIndex++) {
        excessTiles.add(tileType);
      }
      discardTilesToFloorLine(excessTiles);
      handleCommunicationsWhenValidMove(row, tilesPlacedToPatternLine, excessTiles);
    }
    clearCurrentCollectedTiles();
  }

  private void placeTilesToLineWithDifferentTiles(int row, Tiles tileType, Tiles[][] patternLines)
      throws IOException {
    if (patternLines[row][row] != tileType) {
      handleCommunicationWhenInvalidMove();
    }
  }

  private void handleNoAvailablePatternLines() {
    //if place tiles is not possible in the whole pattern lines, place tiles to floor line
    //and clear current tiles
    int tileCount = currentCollectedTiles.size();
    Tiles tileType = currentCollectedTiles.get(0);
    ArrayList<Tiles> discardedTiles = new ArrayList<>();
    for (int count = 0; count < tileCount; count++) {
      discardedTiles.add(tileType);
    }
    discardTilesToFloorLine(discardedTiles);
  }

  private void handleCommunicationWhenInvalidMove() throws IOException {
    //send invalid move to current player
    getCurrentHandler().send(JsonMessage.invalidMove());
  }

  private void handleCommunicationsWhenValidMove(int placeLocation, List<Tiles> tilesToPatternLine,
                                                 List<Tiles> tilesToFloorLine) throws IOException {
    //send valid move to current player
    getCurrentHandler().send(
        JsonMessage.validMove(placeLocation, tilesToPatternLine, tilesToFloorLine));
    //broadcast to other users that somebody placed tiles
    User currentPlayer = getCurrentPlayerInGame();
    String nicknameOfCurrentPlayer = currentPlayer.getNickName();
    broadcast(getCurrentHandler(), JsonMessage.somebodyPlacedTiles(
        nicknameOfCurrentPlayer, placeLocation, tilesToPatternLine, tilesToFloorLine));
  }

  private void handleCommunicationWhenPlayerChanged() throws IOException {
    //broadcast player changed to all users (with turn json message)
    int currentPlayer = getIndexOfCurrentPlayerInGame();
    broadcastToAll(JsonMessage.turn(currentPlayer));
  }

  private void setCurrentCollectedTiles(Tiles[] collectedTiles) {
    Collections.addAll(currentCollectedTiles, collectedTiles);
  }

  public List<Tiles> getCurrentCollectedTiles() {
    return currentCollectedTiles;
  }

  public void clearCurrentCollectedTiles() {
    currentCollectedTiles.clear();
  }

  private void fillFactoryDisplay() {
    int displaysNumber = factoryDisplays.getAllDisplays().size();
    for (int displayIndex = 0; displayIndex < displaysNumber; displayIndex++) {
      for (int tileIndex = 0; tileIndex < 4; tileIndex++) {
        ArrayList<Tiles> singleDisplay = factoryDisplays.getAllDisplays().get(displayIndex);
        if (bag.getBagSize() != 0) {
          singleDisplay.add(bag.getRandomTile());
        } else {
          if (lid.getLidSize() > 0) {
            singleDisplay.add(lid.getRandomTileFromLid());
          }
        }
      }
    }
  }

  private void setStartGameUser() {
    //random player starts the whole game
    int totalNumberOfPlayers = getTotalNumberOfPlayers();
    int randomInt = ThreadLocalRandom.current().nextInt(totalNumberOfPlayers);
    currentPlayerInGame = allUsersInGame.get(randomInt);
  }

  private void setNextPlayerInGame() {
    int indexOfCurrentPlayerInGame = getIndexOfCurrentPlayerInGame();
    int totalNumberOfPlayers = getTotalNumberOfPlayers();
    int indexOfNextPlayerInGame = ++indexOfCurrentPlayerInGame % totalNumberOfPlayers;

    currentPlayerInGame = allUsersInGame.get(indexOfNextPlayerInGame);
  }

  private void setFirstPlayerInNewRound(int indexOfFirstPlayerInRound) {
    currentPlayerInGame = allUsersInGame.get(indexOfFirstPlayerInRound);
  }


  public User getCurrentPlayerInGame() {
    return currentPlayerInGame;
  }

  public int getIndexOfCurrentPlayerInGame() {
    return allUsersInGame.indexOf(currentPlayerInGame);
  }

  private void createPlayerBoards() {
    int totalNumberOfPlayers = getTotalNumberOfPlayers();
    this.playerBoards = new PlayerBoard[totalNumberOfPlayers];
    for (int boardIndex = 0; boardIndex < totalNumberOfPlayers; boardIndex++) {
      playerBoards[boardIndex] = new PlayerBoard();
    }
  }

  private void createFactoryDisplays() {
    int totalNumberOfPlayers = getTotalNumberOfPlayers();
    this.factoryDisplays = new FactoryDisplay(totalNumberOfPlayers);
  }

  public FactoryDisplay getAllFactoryDisplays() {
    return factoryDisplays;
  }

  private int getTotalNumberOfPlayers() {
    return allUsersInGame.size();
  }

  private void setInitialScores() {
    int totalNumberOfPlayers = getTotalNumberOfPlayers();
    for (int playerIndex = 0; playerIndex < totalNumberOfPlayers; playerIndex++) {
      scoresOfAllUsers.add(0);
    }
  }

  public String getRoomName() {
    return roomName;
  }

  public Phase getPhase() {
    return phase;
  }

  public List<User> getAllUsersInGame() {
    return allUsersInGame;
  }

  public boolean isRoomFull() {
    return allUsersInGame.size() == 4;
  }

  public boolean isNicknameAvailable(String nickname) {
    for (User user : allUsersInGame) {
      if (Objects.equals(user.getNickName(), nickname)) {
        return false;
      }
    }
    return true;
  }

  public void addUser(User user) {
    allUsersInGame.add(user);
  }

  public void addUserMessageHandler(UserMessageHandler userMessageHandler) {
    userMessageHandlers.add(userMessageHandler);
  }

  public void removeUserMessageHandler(UserMessageHandler userMessageHandler) {
    userMessageHandlers.remove(userMessageHandler);
  }

  public boolean areAllUsersReady() {
    //check whether all users are ready for game, if so, return true
    if (allUsersInGame.size() < 2) {
      return false;
    }
    for (User user : allUsersInGame) {
      if (!user.isReadyToPlay()) {
        return false;
      }
    }
    return true;
  }

  /**
   * Method for a single user to broadcast a message to all the other connected users in the same
   * game room.
   *
   * @param sender The user from whom the message originates
   * @param message The message as JSONObject that is to be broadcast.
   * @throws IOException Thrown when failing to access the input- or output-stream.
   */
  public void broadcast(UserMessageHandler sender, JSONObject message) throws IOException {
    synchronized (userMessageHandlers) {
      for (UserMessageHandler handler : userMessageHandlers) {
        if (handler != sender) {
          handler.send(message);
        }
      }
    }
  }

  /**
   * Method for a game room to broadcast a message to all users in the game room.
   *
   * @param message The message as JSONObject that is to be broadcast.
   * @throws IOException Thrown when failing to access the input- or output-stream.
   */
  public void broadcastToAll(JSONObject message) throws IOException {
    synchronized (userMessageHandlers) {
      for (UserMessageHandler handler : userMessageHandlers) {
        handler.send(message);
      }
    }
  }

}