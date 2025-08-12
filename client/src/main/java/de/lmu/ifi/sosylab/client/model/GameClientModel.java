package de.lmu.ifi.sosylab.client.model;


import static java.util.Objects.requireNonNull;

import de.lmu.ifi.sosylab.shared.Bag;
import de.lmu.ifi.sosylab.shared.FactoryDisplay;
import de.lmu.ifi.sosylab.shared.GamePlayers;
import de.lmu.ifi.sosylab.shared.Lid;
import de.lmu.ifi.sosylab.shared.PlayerBoard;
import de.lmu.ifi.sosylab.shared.Tiles;
import de.lmu.ifi.sosylab.shared.WallTile;
import de.lmu.ifi.sosylab.shared.events.FactoryDisplaysChangeEvent;
import de.lmu.ifi.sosylab.shared.events.FloorLineChangeEvent;
import de.lmu.ifi.sosylab.shared.events.GameEndEvent;
import de.lmu.ifi.sosylab.shared.events.GameEvent;
import de.lmu.ifi.sosylab.shared.events.GameTableChangeEvent;
import de.lmu.ifi.sosylab.shared.events.PatternLinesChangeEvent;
import de.lmu.ifi.sosylab.shared.events.PlaceTilesFailEvent;
import de.lmu.ifi.sosylab.shared.events.PlayerTurnFinishedEvent;
import de.lmu.ifi.sosylab.shared.events.StartGameEvent;
import de.lmu.ifi.sosylab.shared.events.StartNextRoundEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * The Hot seat model of the Azul game. Contains methods necessary to change the game's state and
 * checks if they're legal.
 * A {@link java.beans.PropertyChangeEvent} is fired when the GUI needs to be updated.
 * @see Model
 */
public class GameClientModel implements Model {
  private PlayerBoard[] playerBoards;
  private FactoryDisplay factoryDisplays;
  private List<Tiles> gameTable;
  private Bag bag;
  private Lid lid;
  //the total number of players in game
  private int totalNumberOfPlayers;
  private ArrayList<String> usernames;
  private ArrayList<Tiles> currentCollectedTiles;
  private int currentDisplay;
  private GamePlayers gamePlayers;

  //store the scores of all players
  private final ArrayList<Integer> scores;
  private final PropertyChangeSupport support;


  /**
   * Construct the game. Create the game table and the bag of 100 tiles.
   */
  public GameClientModel() {
    //construct the bag of tiles
    this.currentCollectedTiles = new ArrayList<>();
    this.usernames = new ArrayList<>();
    scores = new ArrayList<>();
    support = new PropertyChangeSupport(this);
  }

  /**
   * Set the total number of players according to messages from GUI.
   *
   * @param totalNumberOfPlayers the total number of players received from GUI.
   */
  public void setPlayerNumber(int totalNumberOfPlayers) {
    this.totalNumberOfPlayers = totalNumberOfPlayers;
  }

  public void setFactoryDisplays(FactoryDisplay factoryDisplays) {
    this.factoryDisplays = factoryDisplays;
  }

  public void setPlayerBoards(PlayerBoard[] playerBoards) {
    this.playerBoards = playerBoards;
  }

  public void setUsernames(ArrayList<String> usernames) {
    this.usernames = usernames;
  }

  /**
   * Create game players for the game according to the player numbers received from GUI.
   */
  private void createPlayers() {
    this.gamePlayers = new GamePlayers(totalNumberOfPlayers, usernames);
  }

  /**
   * Create player boards according to the total number of players received from GUI.
   */
  private void createPlayerBoards() {
    this.playerBoards = new PlayerBoard[totalNumberOfPlayers];
    for (int boardIndex = 0; boardIndex < totalNumberOfPlayers; boardIndex++) {
      playerBoards[boardIndex] = new PlayerBoard();
    }
  }

  /**
   * Create factory displays according to the total number of players received from GUI.
   */
  private void createFactoryDisplays() {
    this.factoryDisplays = new FactoryDisplay(totalNumberOfPlayers);
  }

  /**
   * Set the initial scores of all the players to 0.
   */
  private void setInitialScores() {
    for (int playerIndex = 0; playerIndex < totalNumberOfPlayers; playerIndex++) {
      scores.add(0);
    }
  }

  /**
   * Start the game. Create game players, player boards, factory displays,
   * and add starting player marker to the game table. Randomly fill factory displays
   * and set a random starting player.
   * Model notifies GUI that the states of the current game player, the game table,
   * and the factory displays have changed.
   */
  public void startGame() {
    bag = new Bag();
    lid = new Lid();
    createPlayers();
    createPlayerBoards();
    createFactoryDisplays();
    setInitialScores();

    //construct the game table
    gameTable = new ArrayList<>();
    //place the starting player marker to the game table at position 0
    gameTable.add(Tiles.START);
    GameTableChangeEvent gameTableChangeEvent = new GameTableChangeEvent();
    notifyListener(gameTableChangeEvent);
    //randomly fill factory displays
    fillFactoryDisplay();
    //set a random player to start the game and inform GUI which player should start the game
    setCurrentPlayer(gamePlayers.setStartGamePlayer());
    StartGameEvent startGameEvent = new StartGameEvent();
    notifyListener(startGameEvent);
  }

  /**
   * Clears all values before creating a new game.
   */
  public void restartGame() {
    bag = new Bag();
    lid = new Lid();
    createPlayers();
    createPlayerBoards();
    createFactoryDisplays();
    scores.clear();
    setInitialScores();
    gameTable.clear();
    gameTable.add(Tiles.START);
    GameTableChangeEvent gameTableChangeEvent = new GameTableChangeEvent();
    notifyListener(gameTableChangeEvent);
    //randomly fill factory displays
    fillFactoryDisplay();
    //set a random player to start the game and inform GUI which player should start the game
    setCurrentPlayer(gamePlayers.setStartGamePlayer());
    StartGameEvent startGameEvent = new StartGameEvent();
    notifyListener(startGameEvent);
  }

  @Override
  public String getNickname() {
    return "";
  }

  @Override
  public String getRoomName() {
    return "";
  }

  /**
   * Fill each factory display with 4 random tiles from the bag.
   */
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
    FactoryDisplaysChangeEvent factoryDisplaysChangeEvent = new FactoryDisplaysChangeEvent();
    notifyListener(factoryDisplaysChangeEvent);
  }

  /**
   * Set a random player to start the game,
   * and set the player who has the starting player marker to start a new round.
   *
   * @param startPlayer the current player that starts the game.
   */
  private void setCurrentPlayer(int startPlayer) {
    gamePlayers.setCurrentPlayer(startPlayer);
  }

  /**
   * Set the current collected tiles of the game.
   *
   * @param collectedTiles the collected tiles of the player.
   */
  public void setCurrentCollectedTiles(Tiles[] collectedTiles) {
    currentCollectedTiles.addAll(Arrays.asList(collectedTiles));
  }

  /**
   * Clear the current collected tiles if one player finishing placing tiles to pattern lines.
   */
  private void clearCurrentCollectedTiles() {
    currentCollectedTiles.clear();
  }

  /**
   * Collect tiles from the chosen factory display and discard remaining tiles to table.
   * The model notifies the listener that the state of the factory displays and of the game table
   * has changed.
   *
   * @param displayIndex the chosen factory display.
   * @param tileIndex the index of (one of) the chosen tile(s).
   */
  public void tilesFromDisplay(int displayIndex, int tileIndex) {
    if (currentCollectedTiles.size() != 0) {
      return;
    }
    this.currentDisplay = displayIndex;
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
      //notify GUI about factory display change
      FactoryDisplaysChangeEvent factoryDisplaysChangeEvent = new FactoryDisplaysChangeEvent();
      notifyListener(factoryDisplaysChangeEvent);
      if (discardedTiles.size() != 0) {
        gameTable.addAll(discardedTiles);
        GameTableChangeEvent gameTableChangeEvent = new GameTableChangeEvent();
        notifyListener(gameTableChangeEvent);
      }
    }
  }

  /**
   * Get the current chosen factory display in the game.
   *
   * @return the current chosen factory display.
   */
  public int getCurrentDisplay() {
    return currentDisplay;
  }

  /**
   * Collect tiles from the middle of the game table.
   *
   * @param tileIndex the index of (one of) the chosen tile(s).
   */
  public void tilesFromTable(int tileIndex) {
    if (currentCollectedTiles.size() != 0) {
      return;
    }
    //check whether game table is empty or only has the starting player marker
    //if so, notify GUI that the player cannot collect tiles from the table
    if (gameTable.size() == 0 || (gameTable.size() == 1 && gameTable.get(0) == Tiles.START)) {
      //do nothing because the game table is either empty or has just the starting player marker
      //and therefore the player cannot collect tiles from table
    } else {
      //collect starting player marker if the first
      //and place it to the first position of the floor line
      if (gameTable.get(0) == Tiles.START) {
        PlayerBoard currentBoard = playerBoards[gamePlayers.getCurrentPlayer()];
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

      GameTableChangeEvent gameTableChangeEvent = new GameTableChangeEvent();
      notifyListener(gameTableChangeEvent);
      FloorLineChangeEvent floorLineChangeEvent = new FloorLineChangeEvent();
      notifyListener(floorLineChangeEvent);
    }
  }

  /**
   * Place the collected tiles to the pattern lines, deal with excess tiles,
   * and change current player to the next player.
   *
   * @param row the chosen row of the pattern lines.
   */
  public void placeTilesToPatternLines(int row) {
    PlayerBoard currentBoard = playerBoards[gamePlayers.getCurrentPlayer()];
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
      //if there are still available lines, check the status of the wall
      //check whether the corresponding line of the wall has the same tile
      //if so, cannot place tile
      for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
        if (wall[row][columnIndex].getIsOnWall()) {
          if (wall[row][columnIndex].getTile() == tileType) {
            PlaceTilesFailEvent placeTilesFailEvent = new PlaceTilesFailEvent();
            notifyListener(placeTilesFailEvent);
            return;
          }
        }
      }
      //if the corresponding row of the wall doesn't have the same tile type,
      //check the status of pattern lines

      //if the line is empty, place tiles and deal with excess tiles
      if (patternLines[row][row] == null) {
        placeTilesToEmptyLine(row, tileCount, tileType, patternLines);
      } else {
        //if the line has the same type of tiles,
        //place tiles to empty blocks and deal with excess tiles
        if (patternLines[row][row] == tileType) {
          //check whether the line is full, if full, inform GUI by sending PlaceTilesFailEvent
          if (patternLines[row][0] != null) {
            PlaceTilesFailEvent placeTilesFailEvent = new PlaceTilesFailEvent();
            notifyListener(placeTilesFailEvent);
            return;
          }
          placeTilesToLineWithSameTiles(row, tileCount, tileType, patternLines);
        } else {
          //if the line has a different type of tiles, cannot play tiles and notify GUI
          placeTilesToLineWithDifferentTiles(row, tileType, patternLines);
          return;
        }

      }
    } else {
      //if place tiles is not possible in the whole pattern lines, place tiles to floor line
      //and clear current tiles
      ArrayList<Tiles> discardedTiles = new ArrayList<>();
      for (int count = 0; count < tileCount; count++) {
        discardedTiles.add(tileType);
      }
      tilesToFloorLine(discardedTiles);
      clearCurrentCollectedTiles();

    }

    //check whether a round is finished
    if (isRoundFinished()) {
      //if a round is finished, move tiles to wall
      tilesToWall();
    } else {
      gamePlayers.changeCurrentPlayer();
      PlayerTurnFinishedEvent playerTurnFinishedEvent = new PlayerTurnFinishedEvent();
      notifyListener(playerTurnFinishedEvent);
    }

  }

  /**
   * The player can add tiles to the floor line after collecting tiles.
   */
  public void placeTilesToFloorLine() {
    if (currentCollectedTiles.size() == 0) {
      return;
    }
    PlayerBoard currentBoard = playerBoards[gamePlayers.getCurrentPlayer()];
    currentBoard.getFloorLine().addAll(currentCollectedTiles);

    FloorLineChangeEvent floorLineChangeEvent = new FloorLineChangeEvent();
    notifyListener(floorLineChangeEvent);
    clearCurrentCollectedTiles();

    //check whether a round is finished
    if (isRoundFinished()) {
      //if a round is finished, move tiles to wall
      tilesToWall();
    } else {
      gamePlayers.changeCurrentPlayer();
      PlayerTurnFinishedEvent playerTurnFinishedEvent = new PlayerTurnFinishedEvent();
      notifyListener(playerTurnFinishedEvent);
    }

  }


  /**
   * Check whether it is possible to place tiles according to the status of the whole pattern line.
   *
   * @param tileType the type of the places that need to be placed.
   * @param patternLines the current pattern lines.
   * @return true if it is possible to place tiles, false if not.
   */
  private boolean checkAvailableLines(Tiles tileType, Tiles[][] patternLines, WallTile[][] wall) {
    //check the first positions of the pattern lines
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

  /**
   * Place tiles to an empty pattern line.
   *
   * @param row the row of the empty pattern line.
   * @param tileCount the count of the collected tiles.
   * @param tileType the type of the collected tiles.
   * @param patternLines the pattern lines where the tiles are placed.
   */
  public void placeTilesToEmptyLine(int row, int tileCount, Tiles tileType,
                                    Tiles[][] patternLines) {
    int lineLength = patternLines[row].length;

    if (tileCount <= lineLength) {
      if (patternLines[row][row] == null) {
        for (int index = 0; index < tileCount; index++) {
          patternLines[row][lineLength - (tileCount - index)] = tileType;
        }
        PatternLinesChangeEvent patternLinesChangeEvent = new PatternLinesChangeEvent();
        notifyListener(patternLinesChangeEvent);
      }

    } else {
      if (patternLines[row][row] == null) {
        for (int index = 0; index < lineLength; index++) {
          patternLines[row][index] = tileType;
        }
        PatternLinesChangeEvent patternLinesChangeEvent = new PatternLinesChangeEvent();
        notifyListener(patternLinesChangeEvent);
      }
      //deal with excess tiles
      int excessTilesCount = tileCount - lineLength;
      List<Tiles> excessTiles = new ArrayList<>(excessTilesCount);
      for (int tileIndex = 0; tileIndex < excessTilesCount; tileIndex++) {
        excessTiles.add(tileType);
      }
      tilesToFloorLine(excessTiles);
    }
    clearCurrentCollectedTiles();
  }

  /**
   * Place the collected tiles to a pattern line with the same tile types.
   *
   * @param row the row of the pattern line.
   * @param tileCount the number of the collected tiles.
   * @param tileType the type of the collected tiles.
   * @param patternLines the pattern lines where the tiles are placed.
   */
  private void placeTilesToLineWithSameTiles(int row, int tileCount, Tiles tileType,
                                            Tiles[][] patternLines) {
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
        tilesToFloorLine(excessTiles);
      }
      PatternLinesChangeEvent patternLinesChangeEvent = new PatternLinesChangeEvent();
      notifyListener(patternLinesChangeEvent);
      clearCurrentCollectedTiles();
    }

  }

  /**
   * Place tiles to the pattern line with a different tile type.
   *
   * @param row the row of the pattern line.
   * @param tileType the type of the collected tiles.
   * @param patternLines the pattern lines where the tiles are placed.
   */
  private void placeTilesToLineWithDifferentTiles(int row, Tiles tileType, Tiles[][] patternLines) {
    if (patternLines[row][row] != tileType) {
      PlaceTilesFailEvent placeTilesFailEvent = new PlaceTilesFailEvent();
      notifyListener(placeTilesFailEvent);
    }
  }

  /**
   * Game controller requests model to change the current player.
   */
  public void changeCurrentPlayer() {
    gamePlayers.changeCurrentPlayer();
  }

  /**
   * Move excess tiles to the floor line.
   *
   * @param excessTiles the excess tiles.
   */
  public void tilesToFloorLine(List<Tiles> excessTiles) {
    PlayerBoard currentBoard = playerBoards[gamePlayers.getCurrentPlayer()];
    List<Tiles> floorLine = currentBoard.getFloorLine();
    floorLine.addAll(excessTiles);

    FloorLineChangeEvent floorLineChangeEvent = new FloorLineChangeEvent();
    notifyListener(floorLineChangeEvent);
  }

  /**
   * Check whether a round is finished.
   *
   * @return true if a round is finished, false if not.
   */
  private boolean isRoundFinished() {
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

  /**
   * Move tiles to wall when a round is finished, and discard remaining tiles.
   * The model updates the pattern lines and the wall accordingly, and then informs the listener.
   *
   */
  public void tilesToWall() {
    //store the calculated gain points of all players
    ArrayList<Integer> allGainPoints =
        new ArrayList<>(Collections.nCopies(totalNumberOfPlayers, 0));
    for (int boardIndex = 0; boardIndex < totalNumberOfPlayers; boardIndex++) {
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
              allGainPoints.set(boardIndex,
                  allGainPoints.get(boardIndex) + calculateSingleTileGainPoints(wall, row, col));
            }
          }
          //discard remaining tiles and add them to lid
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
    }
    //check whether the game is finished
    if (!checkEndGame()) {
      //calculate scores
      //set the scoring of the game to the current calculated scores
      scoring(allGainPoints);
      //clear floor line: move tiles in floor line to lid (except the starting player marker)
      clearFloorLine();
      //start new round
      startNextRound();
    } else {
      scoring(allGainPoints);
      //end game scoring
      endGameScoring();
      //notify GUI that game is ended, and sent the ranking to GUI
      LinkedHashMap<Integer, Integer> ranking = calculateRanking();
      GameEndEvent gameEndEvent = new GameEndEvent(ranking);
      notifyListener(gameEndEvent);
    }
  }

  /**
   * Clear the floor line (except for starting player marker).
   */
  private void clearFloorLine() {
    //move tiles (except the starting player marker) to lid
    for (int boardIndex = 0; boardIndex < totalNumberOfPlayers; boardIndex++) {
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
        //notify GUI about floor line change
        FloorLineChangeEvent floorLineChangeEvent = new FloorLineChangeEvent();
        notifyListener(floorLineChangeEvent);
      }
    }
  }

  /**
   * Collect all discarded tiles after wall-tiling and add them to the lid.
   *
   * @param discardTiles the discarded tiles.
   */
  private void discardTilesToLid(List<Tiles> discardTiles) {
    lid.discardTilesToLid(discardTiles);
  }

  /**
   * Starts a new round. Randomly refill the factory displays
   * and start with the player who has the starting player marker. Change the floor line
   * of the starting player and the game table accordingly.
   */
  private void startNextRound() {
    //refill factory display
    fillFactoryDisplay();
    //start with player who has the starting player marker, notify GUI
    for (int boardIndex = 0; boardIndex < totalNumberOfPlayers; boardIndex++) {

      PlayerBoard currentBoard = playerBoards[boardIndex];
      ArrayList<Tiles> floorLine = currentBoard.getFloorLine();
      if (floorLine.size() > 0) {
        if (floorLine.get(0) == Tiles.START) {
          setCurrentPlayer(boardIndex);
          floorLine.clear();
        }
      }
    }
    //the starting player places the starting player marker (in floor line) to the game table
    gameTable.add(0, Tiles.START);
    StartNextRoundEvent startNextRoundEvent = new StartNextRoundEvent();
    notifyListener(startNextRoundEvent);
  }

  /**
   * Checks whether the end game condition is reached.
   *
   * @return true if end game condition is reached, false if not.
   */
  private boolean checkEndGame() {
    for (int boardIndex = 0; boardIndex < totalNumberOfPlayers; boardIndex++) {
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

  /**
   * Scoring after each round.
   * Calculate the score of each player using gain points and minus points.
   *
   * @param allGainPoints the gain points of all users in the phase wall tiling
   */
  private void scoring(ArrayList<Integer> allGainPoints) {
    for (int boardIndex = 0; boardIndex < totalNumberOfPlayers; boardIndex++) {
      PlayerBoard currentBoard = playerBoards[boardIndex];
      ArrayList<Tiles> floorLine = currentBoard.getFloorLine();
      //calculate the minus points for a single player
      int minusPoints = calculateSinglePlayerMinusPoints(floorLine.size());
      //get the gain points for a single player
      int gainPoints = allGainPoints.get(boardIndex);
      //calculate the score for a single player
      if (scores.get(boardIndex) + gainPoints - minusPoints > 0) {
        scores.set(boardIndex, scores.get(boardIndex) + gainPoints - minusPoints);
      } else {
        scores.set(boardIndex, 0);
      }
    }
  }


  /**
   * Calculate gain points for a single tile.
   *
   * @param row of tile row of the tile.
   * @param column of tile column of the tile.
   * @return gain points for this tile.
   */
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


  /**
   * Scoring after game end.
   */
  private void endGameScoring() {
    for (int boardIndex = 0; boardIndex < totalNumberOfPlayers; boardIndex++) {
      PlayerBoard currentBoard = playerBoards[boardIndex];
      WallTile[][] wall = currentBoard.getWall();
      int columnBonus = colBonus(wall);
      int rowBonus = rowBonus(wall);
      int colorBonus = colorBonus(wall);
      scores.set(boardIndex, columnBonus + rowBonus + colorBonus + scores.get(boardIndex));
    }
  }

  /**
   * Calculate columns bonus.
   * Completed columns on the walls: +7 for each column.
   */
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

  /**
   * Calculate rows bonus.
   * Completed rows on the walls: +2 for each row.
   */
  private int rowBonus(WallTile[][] wall) {
    int bonus = 0;
    for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
      boolean isFull = true;
      for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
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

  /**
   * Calculate colors bonus.
   * Completed colors on the walls: +10 for each color
   */
  private int colorBonus(WallTile[][] wall) {
    int bonus = 0;
    // check for each color whether there are 5 tiles of the color on the wall
    for (Tiles tile : Tiles.values()) {
      int colorCount = 0;
      for (int rowIndex = 0; rowIndex < 5; rowIndex++) {
        for (int columnIndex = 0; columnIndex < 5; columnIndex++) {
          if (wall[rowIndex][columnIndex].getIsOnWall()
              && wall[rowIndex][columnIndex].getTile() == tile) {
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

  private LinkedHashMap<Integer, Integer> calculateRanking() {
    HashMap<Integer, Integer> playersWithScores = new HashMap<>();
    for (int playerIndex = 0; playerIndex < totalNumberOfPlayers; playerIndex++) {
      playersWithScores.put(playerIndex, scores.get(playerIndex));
    }
    LinkedHashMap<Integer, Integer> ranking = new LinkedHashMap<>();
    HashMap<Integer, Integer> playersWithAscendingRanking = new HashMap<>();
    Set<Integer> setOfNoDuplicatedScores = new HashSet<>();
    Collections.sort(scores);
    setOfNoDuplicatedScores.add(scores.get(0));
    for (Integer score : scores) {
      if (!Objects.equals(score, scores.get(0))) {
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
    for (int scoreIndex = 0; scoreIndex < totalNumberOfPlayers; scoreIndex++) {
      int rank = totalNumberOfPlayers - scoreIndex;
      for (Entry<Integer, Integer> entry : playersWithScores.entrySet()) {
        if (Objects.equals(entry.getValue(), scores.get(scoreIndex))) {
          playersWithAscendingRanking.put(entry.getKey(), rank);
          break;
        }
      }
    }
    for (int playerIndex = 0; playerIndex < totalNumberOfPlayers; playerIndex++) {
      for (Entry<Integer, Integer> entry : playersWithAscendingRanking.entrySet()) {
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
    for (int playerIndex = 0; playerIndex < totalNumberOfPlayers; playerIndex++) {
      scorePlusCompletedRows.add(
          scores.get(playerIndex) + completedRowsOfAllPlayers.get(playerIndex));
    }
    //store the score (plus completed rows) and the corresponding players together
    HashMap<Integer, Integer> playersWithScoresAndRows = new HashMap<>();
    for (int playerIndex = 0; playerIndex < totalNumberOfPlayers; playerIndex++) {
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
    for (int playerIndex = 0; playerIndex < totalNumberOfPlayers; playerIndex++) {
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
    for (int rank = 1; rank < totalNumberOfPlayers; rank++){
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


  /**
   * Calculate minus points for each player.
   *
   * @param  floorSize the size of the player's floor line.
   * @return minusPoint the minus points of the player.
   */
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

  /**
   * Get the player board of the player.
   *
   * @param player the player
   * @return the player board.
   */
  public PlayerBoard getPlayerBoard(int player) {
    return playerBoards[player];
  }

  /**
   * Get all factory displays.
   *
   * @return all factory displays.
   */
  public FactoryDisplay getFactoryDisplays() {
    return factoryDisplays;
  }

  /**
   * Get the game table.
   *
   * @return the game table.
   */
  public List<Tiles> getGameTable() {
    return gameTable;
  }

  /**
   * Get a list of scores of each player.
   *
   * @return the list of scores of each player.
   */
  public ArrayList<Integer> getScores() {
    return scores;
  }

  /**
   * Get the current player in game.
   *
   * @return the current player.
   */
  public int getCurrentPlayer() {
    return gamePlayers.getCurrentPlayer();
  }

  /**
   * Get the count of the total number of players in game.
   *
   * @return the count of the total number of players in game.
   */
  public int getPlayerCount() {
    return gamePlayers.getGamePlayers().length;
  }

  /**
   * Get all the players in game.
   *
   * @return all the players in game.
   */
  public GamePlayers getGamePlayers() {
    return gamePlayers;
  }

  /**
   * Add a {@link PropertyChangeListener} to the model to notify subscribed listener about
   * any changes that are published by the model.
   *
   * @param listener the view that subscribes itself to the model.
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    requireNonNull(listener);
    support.addPropertyChangeListener(listener);
  }

  /**
   * Remove the subscribed listener from the model. The listener will not get notified about
   * any changes that are published by the model.
   *
   * @param listener the view that is to be unsubscribed from the model.
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {

    support.removePropertyChangeListener(listener);
  }

  /**
   * Notify the subscribed listener that the state of the model has changed. The model fires
   * a specific {@link GameEvent} so that the subscribed listener knows
   * what has been changed in the model.
   *
   * @param event the event that is fired by the model.
   */
  private void notifyListener(GameEvent event) {
    support.firePropertyChange(event.getName(), null, event);
  }
}
