package de.lmu.ifi.sosylab.shared;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Manage the players in game and control player order.
 */
public class GamePlayers {
  //the total number of players in game
  private final int playerNumber;
  private final int[] gamePlayers;
  private int currentPlayer;
  private final ArrayList<String> usernames;

  /**
   * Construct game players.
   *
   * @param playerNumber the number of players in game.
   */
  public GamePlayers(int playerNumber, ArrayList<String> usernames) {
    this.playerNumber = playerNumber;
    //initialise a list of game players according to the total player number in game
    gamePlayers = new int[playerNumber];
    for (int index = 0; index < playerNumber; index++) {
      gamePlayers[index] = index;
    }
    this.usernames = new ArrayList<>(usernames);
  }

  /**
   * Get all the game players.
   *
   * @return an array of all the game players.
   */
  public int[] getGamePlayers() {
    return gamePlayers;
  }

  /**
   * Change the current player to the next game player.
   *
   */
  public void changeCurrentPlayer() {
    currentPlayer = ++currentPlayer % playerNumber;
  }

  /**
   * Set the current player at the start of the whole game and at the new round.
   *
   * @param currentPlayer the current player that should be set.
   */
  public void setCurrentPlayer(int currentPlayer) {
    this.currentPlayer = currentPlayer;
  }

  /**
   * Get the current player in game.
   *
   * @return the current player in game.
   */
  public int getCurrentPlayer() {
    return currentPlayer;
  }

  /**
   * Set a random player to start the game.
   *
   * @return the random player who starts the game.
   */
  public int setStartGamePlayer() {
    //random player starts the whole game
    int randomInt = ThreadLocalRandom.current().nextInt(playerNumber);
    return gamePlayers[randomInt];
  }

  public ArrayList<String> getUsernames() {
    return usernames;
  }

}