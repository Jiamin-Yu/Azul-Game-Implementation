package de.lmu.ifi.sosylab.client.view.playboard;

import de.lmu.ifi.sosylab.client.controller.Controller;
import de.lmu.ifi.sosylab.client.model.Model;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Contains a player's {@link PatternLines}, {@link Wall}, {@link FloorLine}, a label for the
 * player's name and one for their score.
 */
public class PlayerBoardView extends JPanel {

  private static final String SCORE_LABEL = "Score: ";
  private final Model model;
  private final Controller controller;
  private final int userId;
  private final String username;
  private final int tileSize;
  private JLabel usernameLabel;
  private JLabel userScoreLabel;
  private PatternLines patternLines;
  private Wall wall;
  private FloorLine floorLine;

  /**
   * Creates the PlayerBoardsView for a user with a specified username and tile size.
   *
   * @param username the player's username
   * @param userId   the player's ID
   * @param tileSize the tile-size
   * @param model    the game's model
   */
  public PlayerBoardView(String username, int userId, int tileSize, Model model,
                         Controller controller) {
    setBackground(Color.DARK_GRAY);
    setLayout(new GridBagLayout());

    this.username = username;
    this.userId = userId;
    this.tileSize = tileSize;
    this.model = model;
    this.controller = controller;

    initialiseWidgets();
    createView();
  }

  private void initialiseWidgets() {
    usernameLabel = new JLabel(username);
    usernameLabel.setForeground(Color.WHITE);
    userScoreLabel = new JLabel(SCORE_LABEL + model.getScores().get(userId));
    userScoreLabel.setForeground(Color.WHITE);

    patternLines = new PatternLines(tileSize, model, controller, userId);
    wall = new Wall(tileSize, model, userId);
    floorLine = new FloorLine(tileSize, model, controller, userId);
  }

  private void createView() {
    GridBagConstraints gbc = new GridBagConstraints();

    add(patternLines, gbc);

    gbc.gridx = 1;
    add(wall, gbc);

    gbc.gridy = 1;
    gbc.gridx = 0;
    gbc.gridheight = 2;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.LINE_START;
    add(floorLine, gbc);

    gbc.gridx = 1;
    gbc.gridheight = 1;
    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.EAST;
    add(usernameLabel, gbc);

    gbc.gridy = 2;
    add(userScoreLabel, gbc);
  }

  /**
   * Changes the background color of the PlayerBoardView and it's sub-elements ({@link Wall},
   * {@link PatternLines} and {@link FloorLine}).
   *
   * @param color the color that is set
   */
  public void changeBackgroundColor(Color color) {
    patternLines.setBackground(color);
    floorLine.setBackground(color);
    wall.setBackground(color);
    setBackground(color);
  }

  /**
   * Update the score.
   */
  public void updateScore() {
    userScoreLabel.setText(SCORE_LABEL + model.getScores().get(userId));
  }

  public PatternLines getPatternLines() {
    return patternLines;
  }

  public FloorLine getFloorLine() {
    return floorLine;
  }

  public Wall getWall() {
    return wall;
  }
}
