package de.lmu.ifi.sosylab.client.view.playboard;

import static de.lmu.ifi.sosylab.client.view.ViewUtils.calculateInnerBorder;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.calculateOuterBorder;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.getFilePath;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.isPointInSquare;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.loadBufferedImage;

import de.lmu.ifi.sosylab.client.controller.Controller;
import de.lmu.ifi.sosylab.client.model.Model;
import de.lmu.ifi.sosylab.shared.Tiles;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Optional;
import javax.swing.JPanel;

/**
 * Displays a player's pattern-lines. Each row can be clicked to place collected tiles on it.
 */
public class PatternLines extends JPanel {
  private static final float OPAQUE = 1;
  private final Controller controller;
  private final int outerBorderSize;
  private final int innerBorderSize;
  private final int tileSize;
  private Tiles[][] patternLines;
  private boolean isClickable;

  /**
   * Initialises the pattern-lines.
   *
   * @param tileSize   the tile-size
   * @param model      the game's model
   * @param controller the game's controller
   * @param userId     the player's ID
   */
  PatternLines(int tileSize, Model model, Controller controller, int userId) {
    this.tileSize = tileSize;
    this.outerBorderSize = calculateOuterBorder(tileSize);
    this.innerBorderSize = calculateInnerBorder(tileSize);
    this.patternLines = model.getPlayerBoard(userId).getPatternLines();
    this.controller = controller;

    int length = patternLines.length * tileSize + 2 * outerBorderSize
        + (patternLines.length - 1) * innerBorderSize;
    setPreferredSize(new Dimension(length, length));

    setBackground(Color.DARK_GRAY);
    configureActionListener();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;

    for (int i = 0; i < patternLines.length; i++) {
      for (int j = 0; j < patternLines[i].length; j++) {
        Point posTile = new Point();
        posTile.x = outerBorderSize + (j + (4 - i)) * innerBorderSize + (j + (4 - i)) * tileSize;
        posTile.y = outerBorderSize + i * innerBorderSize + i * tileSize;

        // draw outline of a tile
        g2D.drawRect(posTile.x, posTile.y, tileSize, tileSize);

        // draw image of a tile
        if (patternLines[i][j] != null) {
          String imagePath = getFilePath(patternLines[i][j]);
          Optional<BufferedImage> image = loadBufferedImage(imagePath, tileSize, tileSize,
              OPAQUE);

          image.ifPresent(
              bufferedImage -> g2D.drawImage(bufferedImage, posTile.x, posTile.y, null));
        }
      }
    }
  }

  private void configureActionListener() {

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);

        if (!isClickable) {
          return;
        }

        Point clickedPoint = e.getPoint();

        for (int i = 0; i < patternLines.length; i++) {
          for (int j = 0; j < patternLines[i].length; j++) {
            Point posTile = new Point(
                outerBorderSize + (j + (4 - i)) * innerBorderSize + (j + (4 - i)) * tileSize,
                outerBorderSize + i * innerBorderSize + i * tileSize);

            if (isPointInSquare(clickedPoint, posTile, tileSize)) {
              controller.placeTilesToPatternLines(i);
            }
          }
        }
      }
    });
  }

  public void setClickable(boolean clickable) {
    isClickable = clickable;
  }

  public void setSource(Tiles[][] patternLines) {
    this.patternLines = patternLines;
  }
}
