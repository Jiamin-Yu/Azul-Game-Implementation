package de.lmu.ifi.sosylab.shared.signals;

public class TilesCollectedSignal extends Signal{

  private int tileIndex;
  private int indexOfCollectPlace;

  public TilesCollectedSignal(int tileIndex, int displayIndex){
    this.tileIndex = tileIndex;
    this.indexOfCollectPlace = displayIndex;
  }

  public int getIndexOfCollectPlace() {
    return indexOfCollectPlace;
  }

  public int getTileIndex() {
    return tileIndex;
  }
}
