package de.lmu.ifi.sosylab.shared;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;


/**
 * Manage the player board.
 */

public class PlayerBoard {
  private final WallTile[][] wall;
  private final Tiles[][] patternLines;
  private final ArrayList<Tiles> floorLine;

  /**
   * Construct the player board.
   */
  public PlayerBoard() {
	//construct the wall
	wall = new WallTile[][] {
		{new WallTile(Tiles.BLUE, false), new WallTile(Tiles.YELLOW, false),
			new WallTile(Tiles.RED, false), new WallTile(Tiles.DARK, false),
			new WallTile(Tiles.GREEN, false)},

		{new WallTile(Tiles.GREEN, false), new WallTile(Tiles.BLUE, false),
			new WallTile(Tiles.YELLOW, false), new WallTile(Tiles.RED, false),
			new WallTile(Tiles.DARK, false)},

		{new WallTile(Tiles.DARK, false), new WallTile(Tiles.GREEN, false),
			new WallTile(Tiles.BLUE, false), new WallTile(Tiles.YELLOW, false),
			new WallTile(Tiles.RED, false)},

		{new WallTile(Tiles.RED, false), new WallTile(Tiles.DARK, false),
			new WallTile(Tiles.GREEN, false), new WallTile(Tiles.BLUE, false),
			new WallTile(Tiles.YELLOW, false)},

		{new WallTile(Tiles.YELLOW, false), new WallTile(Tiles.RED, false),
			new WallTile(Tiles.DARK, false), new WallTile(Tiles.GREEN, false),
			new WallTile(Tiles.BLUE, false)}
	};
	//construct the pattern lines
	patternLines = new Tiles[5][];
	patternLines[0] = new Tiles[1];
	patternLines[1] = new Tiles[2];
	patternLines[2] = new Tiles[3];
	patternLines[3] = new Tiles[4];
	patternLines[4] = new Tiles[5];

	//construct the floor line
	floorLine = new ArrayList<>();
  }

  /**
   * Get the pattern lines of the player board.
   *
   * @return the pattern lines.
   */
  public Tiles[][] getPatternLines() {
	return patternLines;
  }

  /**
   * Get the wall of the player board.
   *
   * @return the wall.
   */
  public WallTile[][] getWall() {
	return wall;
  }

  /**
   * Get the floor line of the player board.
   *
   * @return the floor line.
   */
  public ArrayList<Tiles> getFloorLine() {
	return floorLine;
  }


  /**
   * Actualize the pattern lines before starting the new round
   */
  public void actualizePatternLines() {

	for (Tiles[] row : patternLines) {

	  if (rowInPatternLinesIsFull(row)) {

		Arrays.fill(row, null);
	  }

	}
  }

  /**
   * Check whether the given row in the pattern lines is full of tiles.
   *
   * @param row the row
   * @return true if the row is full of tiles
   */
  private boolean rowInPatternLinesIsFull(Tiles[] row) {

	for (Tiles tile : row) {

	  if (tile == null) {
		return false;
	  }
	}

	return true;
  }

  /**
   * Put a list of tiles in the given row in the pattern lines.
   * If the row is full put the rest of the tiles in the floor line.
   *
   * @param row   the row of the pattern lines
   * @param tiles the list of tiles
   */
  public void placeTilesInPatternLines(int row, ArrayList<Tiles> tiles) {

	// The maximal index of row is row
	for (int i = row; i >= 0; i--) {

	  if (patternLines[row][i] == null && !tiles.isEmpty()) {

		patternLines[row][i] = tiles.get(0);
		tiles.remove(0);
	  }

	}

  }

  /**
   * Put a list of tiles in the floor line.
   *
   * @param tiles the list of tiles
   */
  public void placeTilesInFloorLine(ArrayList<Tiles> tiles) {

	int MAX_CAPACITY_IN_FLOOR_LINE = 7;

	for (Tiles tile : tiles) {

	  if (floorLine.size() < MAX_CAPACITY_IN_FLOOR_LINE) {

		floorLine.add(tile);

	  } else {

		break;
	  }
	}

  }

  private int getNumberOfTilesInPatternLine(int row) {

	int numberOfTilesInRow = 0;

	for (Tiles tile : patternLines[row]) {

	  if (tile != null) {

		numberOfTilesInRow++;
	  }
	}

	return numberOfTilesInRow;
  }
}
