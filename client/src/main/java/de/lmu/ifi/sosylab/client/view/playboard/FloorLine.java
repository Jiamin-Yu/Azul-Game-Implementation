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
import java.util.ArrayList;
import java.util.Optional;
import javax.swing.JPanel;

/**
 * Displays a player's floor-line, consisting of seven slots for tiles and a label above each tile,
 * providing information about how much points a player loses if said tile is placed there.
 */
public class FloorLine extends JPanel {

  private static final int MINUS_ONE_SLOT_COUNT = 2;
  private static final int MINUS_TWO_SLOT_COUNT = 3;
  private static final String PENALTY_LABEL_MINUS_ONE = "-1";
  private static final String PENALTY_LABEL_MINUS_TWO = "-2";
  private static final String PENALTY_LABEL_MINUS_THREE = "-3";
  private static final float OPAQUE = 1;
  private static final int SLOT_COUNT = 7;
  private static final int LABEL_LENGTH = 4;
  private final Controller controller;
  private final int outerBorderSize;
  private final int innerBorderSize;
  private final int tileSize;
  private final int penaltyPointLabelHeight;
  private ArrayList<Tiles> floorLine;
  private boolean isClickable;

  /**
   * Initialises the floor-line.
   *
   * @param tileSize the tile-size
   * @param model    the game's model
   * @param userId   the player's ID
   */
  FloorLine(int tileSize, Model model, Controller controller, int userId) {

    floorLine = model.getPlayerBoard(userId).getFloorLine();

    this.tileSize = tileSize;
    this.controller = controller;
    penaltyPointLabelHeight = tileSize / 2;
    outerBorderSize = calculateOuterBorder(tileSize);
    innerBorderSize = calculateInnerBorder(tileSize);

    int width = SLOT_COUNT * tileSize + (SLOT_COUNT - 1) * innerBorderSize + 2 * outerBorderSize;
    int height = outerBorderSize * 2 + penaltyPointLabelHeight + innerBorderSize + tileSize;
    setPreferredSize(new Dimension(width, height));

    setBackground(Color.DARK_GRAY);
    configureActionListeners();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;

    drawSlots(g2D);
    drawTiles(g2D);
  }

  private void drawSlots(Graphics2D g2D) {
    for (int i = 0; i < SLOT_COUNT; i++) {
      String penaltyLabel;
      if (i < MINUS_ONE_SLOT_COUNT) {
        penaltyLabel = PENALTY_LABEL_MINUS_ONE;
      } else if (i < MINUS_ONE_SLOT_COUNT + MINUS_TWO_SLOT_COUNT) {
        penaltyLabel = PENALTY_LABEL_MINUS_TWO;
      } else {
        penaltyLabel = PENALTY_LABEL_MINUS_THREE;
      }

      int startPositionX = outerBorderSize + i * tileSize + i * innerBorderSize;
      int startPositionY = outerBorderSize;

      g2D.setColor(Color.BLACK);
      g2D.fillRect(startPositionX, startPositionY, tileSize, penaltyPointLabelHeight);

      g2D.drawRect(startPositionX, startPositionY + penaltyPointLabelHeight + innerBorderSize,
          tileSize, tileSize);

      g2D.setColor(Color.WHITE);
      g2D.drawString(penaltyLabel, startPositionX + tileSize / 2 - LABEL_LENGTH,
          startPositionY + penaltyPointLabelHeight / 2 + LABEL_LENGTH);
    }
  }

  private void drawTiles(Graphics2D g2D) {
    for (int i = 0; i < floorLine.size(); i++) {
      if (i < SLOT_COUNT) {
        String imagePath = getFilePath(floorLine.get(i));
        Optional<BufferedImage> image = loadBufferedImage(imagePath, tileSize, tileSize, OPAQUE);

        if (image.isPresent()) {
          g2D.drawImage(image.get(), outerBorderSize + i * tileSize + i * innerBorderSize,
              outerBorderSize + penaltyPointLabelHeight + innerBorderSize, null);
        }
      }
    }
  }

  private void configureActionListeners() {
    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);

        if (!isClickable) {
          return;
        }

        Point pointClicked = e.getPoint();

        for (int i = 0; i < SLOT_COUNT; i++) {
          Point posTile = new Point();
          posTile.x = outerBorderSize + i * tileSize + i * innerBorderSize;
          posTile.y = outerBorderSize + penaltyPointLabelHeight + innerBorderSize;

          if (isPointInSquare(pointClicked, posTile, tileSize)) {
            controller.placeTilesToFloorLine();
            controller.placeTilesToFloorLine();
          }
        }
      }
    });
  }

  public void setClickable(boolean clickable) {
    isClickable = clickable;
  }

  public void setSource(ArrayList<Tiles> floorLine) {
    this.floorLine = floorLine;
  }
}
