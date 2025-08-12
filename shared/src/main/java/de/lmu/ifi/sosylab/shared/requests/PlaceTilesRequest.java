package de.lmu.ifi.sosylab.shared.requests;


import de.lmu.ifi.sosylab.shared.Tiles;

public class PlaceTilesRequest extends Request {

  // the index of the row in pattern lines where the tile will be placed.
  // For floor line will be used -1 and for the displays the numbers 0,...8
  private final int rowIndex;

  public PlaceTilesRequest(int rowIndex) {
	this.rowIndex = rowIndex;
  }

  public int getRowIndex() {
	return rowIndex;
  }
}
