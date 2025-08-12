package de.lmu.ifi.sosylab.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.model.GameClientModel;
import de.lmu.ifi.sosylab.shared.PlayerBoard;
import de.lmu.ifi.sosylab.shared.Tiles;
import de.lmu.ifi.sosylab.shared.WallTile;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Test functionalities of the class GameClientModel.
 */
public class GameClientModelTest {

  static final int MAX_NUMBER_OF_PLAYERS = 4;
  static final int SIZE_OF_WALL = 5;
  static final int MIN_NUMBER_OF_PLAYERS = 2;

  @Test
  public void testInitializationStatusOfGame() {

    for (int i = MIN_NUMBER_OF_PLAYERS; i <= MAX_NUMBER_OF_PLAYERS; i++) {

      // setUp
      GameClientModel model = new GameClientModel();
      GameController controller = new GameController(model);
      ArrayList<String> usernames = new ArrayList<>();
      for (int j = 0; j < i; j++) {
        usernames.add("player" + j);
      }
      controller.startGame(i, usernames);

      // Test

      // test if the number of players is i
      assertEquals(model.getPlayerCount(), i);

      // test if the names of all players are right and in the right order
      model.getGamePlayers().getUsernames().forEach(entry -> assertEquals(
          entry, "player" + model.getGamePlayers().getUsernames().indexOf(entry)));

      // test if there are ((2 * i) + 1) factory displays
      assertEquals(model.getFactoryDisplays().getAllDisplays().size(), 2 * i + 1);

      // test if every factory has 4 tiles
      model.getFactoryDisplays().getAllDisplays()
          .forEach(entry -> assertEquals(entry.size(), 4));

      // test if gameTable only has the starting player marker
      assertEquals(model.getGameTable().size(), 1);
      assertEquals(model.getGameTable().get(0), Tiles.START);

      // test if scores of all players are all 0
      model.getScores().forEach(entry -> assertEquals(entry, 0));

      // test if all walls are correctly initialized
      testInitializationStatusOfAllWalls(model);
    }
  }

  void testInitializationStatusOfAllWalls(GameClientModel model) {
    // set up
    Map<Point, Tiles> modelOfWallAtTheBeginning = new HashMap<>();
    Tiles[] tilesInFirstRowOfWall = {Tiles.BLUE, Tiles.YELLOW, Tiles.RED, Tiles.DARK, Tiles.GREEN};
    for (int h = 0; h < SIZE_OF_WALL; h++) {
      for (int g = 0; g < SIZE_OF_WALL; g++) {
        modelOfWallAtTheBeginning.put(new Point(h, g), tilesInFirstRowOfWall[(g - h + 5) % 5]);
      }
    }

    // test
    for (int k = 0; k < model.getPlayerCount(); k++) {
      WallTile[][] wall = model.getPlayerBoard(k).getWall();
      for (int h = 0; h < SIZE_OF_WALL; h++) {
        for (int g = 0; g < SIZE_OF_WALL; g++) {
          assertEquals(wall[h][g].getTile(), modelOfWallAtTheBeginning.get(new Point(h, g)));
          assertFalse(wall[h][g].getIsOnWall());
        }
      }
    }
  }

  @Test
  void testCalculatorOfScores() {
    // set up (2 players)
    GameClientModel model = new GameClientModel();
    GameController controller = new GameController(model);
    ArrayList<String> usernames = new ArrayList<>();
    usernames.add("player0");
    usernames.add("player1");
    controller.startGame(2, usernames);

    // set up pattern lines and floor line of player0
    model.getPlayerBoard(0).getPatternLines()[0][0] = Tiles.RED;
    model.getPlayerBoard(0).getPatternLines()[1][0] = Tiles.BLUE;
    model.getPlayerBoard(0).getPatternLines()[1][1] = Tiles.BLUE;
    model.getPlayerBoard(0).getFloorLine().add(Tiles.START);

    // add tiles on wall
    model.tilesToWall();

    // test
    assertEquals(model.getScores().get(0), 1);

    // modify pattern lines and floor line of player 0
    model.getPlayerBoard(0).getPatternLines()[0][0] = Tiles.YELLOW;
    model.getPlayerBoard(0).getPatternLines()[1][0] = Tiles.RED;
    model.getPlayerBoard(0).getPatternLines()[1][1] = Tiles.RED;
    model.getPlayerBoard(0).getPatternLines()[2][0] = Tiles.BLUE;
    model.getPlayerBoard(0).getPatternLines()[2][1] = Tiles.BLUE;
    model.getPlayerBoard(0).getPatternLines()[2][2] = Tiles.BLUE;
    model.getPlayerBoard(0).getFloorLine().add(Tiles.GREEN);

    // add tiles on wall
    model.tilesToWall();

    // test
    assertEquals(model.getScores().get(0), 6);

    // modify pattern lines and floor line of player 0
    model.getPlayerBoard(0).getPatternLines()[1][0] = Tiles.YELLOW;
    model.getPlayerBoard(0).getPatternLines()[1][1] = Tiles.YELLOW;
    model.getPlayerBoard(0).getFloorLine().add(Tiles.DARK);
    model.getPlayerBoard(0).getFloorLine().add(Tiles.DARK);
    model.getPlayerBoard(0).getFloorLine().add(Tiles.DARK);
    model.getPlayerBoard(0).getFloorLine().add(Tiles.DARK);

    // add tiles on wall
    model.tilesToWall();

    // test
    assertEquals(model.getScores().get(0), 6);
  }

  @Test
  void testPickTilesFromFactoryDisplay() {
    // set up (2 players)
    GameClientModel model = new GameClientModel();
    GameController controller = new GameController(model);
    ArrayList<String> usernames = new ArrayList<>();
    usernames.add("player0");
    usernames.add("player1");
    controller.startGame(2, usernames);

    // pick the first tile from the first factory display
    ArrayList<Tiles> remainedTiles = model.getFactoryDisplays().getAllDisplays().get(0);
    Tiles removedTileType = remainedTiles.get(0);
    model.tilesFromDisplay(0, 0);
    remainedTiles.removeIf(entry -> entry == removedTileType);

    // test
    assertTrue(model.getFactoryDisplays().getAllDisplays().get(0).isEmpty());
    assertTrue(model.getGameTable().containsAll(remainedTiles));
  }

  @Test
  void testPickTilesFromGameTable() {
    // set up (2 players)
    GameClientModel model = new GameClientModel();
    GameController controller = new GameController(model);
    ArrayList<String> usernames = new ArrayList<>();
    usernames.add("player0");
    usernames.add("player1");
    controller.startGame(2, usernames);

    // add tiles to the game table
    model.getGameTable().add(Tiles.GREEN);
    model.getGameTable().add(Tiles.BLUE);
    model.getGameTable().add(Tiles.DARK);
    model.getGameTable().add(Tiles.RED);

    // pick the blue tile from the game table
    model.tilesFromTable(2);
    ArrayList<Tiles> remainedTiles = new ArrayList<>();
    remainedTiles.add(Tiles.GREEN);
    remainedTiles.add(Tiles.DARK);
    remainedTiles.add(Tiles.RED);

    // test
    model.getGameTable().forEach(entry ->
        assertSame(entry, remainedTiles.get(model.getGameTable().indexOf(entry))));
  }

  @Test
  void testPlaceTilesToEmptyLine() {
    //set up
    GameClientModel model = new GameClientModel();
    model.setPlayerNumber(2);
    model.startGame();

    int currentPlayer = 0;
    PlayerBoard playerBoard = model.getPlayerBoard(currentPlayer);
    Tiles[][] patternLines = playerBoard.getPatternLines();
    int row = 2;
    int tileCount = 1;
    Tiles tileType = Tiles.RED;

    model.placeTilesToEmptyLine(row, tileCount, tileType, patternLines);

    //test equal
    patternLines = playerBoard.getPatternLines();
    assertEquals(Tiles.RED, patternLines[2][2],
        "place tiles to an empty pattern line should work");
  }

  @Test
  void testPlaceTilesToPatternLines() {
    //set up
    int row = 2;
    Tiles[] collectedTiles = new Tiles[] {Tiles.RED};

    GameClientModel model = new GameClientModel();
    model.setPlayerNumber(2);
    model.startGame();
    model.setCurrentCollectedTiles(collectedTiles);

    //test when the line is empty
    model.placeTilesToPatternLines(row);
    Tiles[][] patternLines = model.getPlayerBoard(model.getCurrentPlayer()).getPatternLines();
    assertEquals(Tiles.RED, patternLines[2][2],
        "place tiles to an empty pattern line should work");

    //test when the line has the same tile type
    model.setCurrentCollectedTiles(collectedTiles);
    model.placeTilesToPatternLines(row);
    patternLines = model.getPlayerBoard(model.getCurrentPlayer()).getPatternLines();
    assertEquals(Tiles.RED, patternLines[2][1],
        "place tiles to a pattern line with the same tile type should work");

    //test when the line has a different tile type
    collectedTiles = new Tiles[] {Tiles.BLUE};
    model.setCurrentCollectedTiles(collectedTiles);
    model.placeTilesToPatternLines(row);
    patternLines = model.getPlayerBoard(model.getCurrentPlayer()).getPatternLines();
    assertNull(patternLines[2][0],
        "place tiles to a pattern line with a different tile type should work");

    //test move tiles to wall
    //make the line full
    collectedTiles = new Tiles[] {Tiles.RED};
    model.setCurrentCollectedTiles(collectedTiles);
    model.placeTilesToPatternLines(row);
    //move tiles to wall
    model.tilesToWall();
    WallTile[][] wall = model.getPlayerBoard(model.getCurrentPlayer()).getWall();
    //check wall has the tile
    assertEquals(Tiles.RED, wall[2][4].getTile(), "move tiles to wall should work");
  }

  @Test
  void testTilesToFloorLine() {
    //set up
    GameClientModel model = new GameClientModel();
    model.setPlayerNumber(2);
    model.startGame();
    ArrayList<Tiles> tiles = new ArrayList<>();
    tiles.add(Tiles.RED);
    //test floor line
    model.tilesToFloorLine(tiles);
    ArrayList<Tiles> floorLine = model.getPlayerBoard(model.getCurrentPlayer()).getFloorLine();
    assertEquals(Tiles.RED, floorLine.get(0), "move tiles to wall should work");
  }

}