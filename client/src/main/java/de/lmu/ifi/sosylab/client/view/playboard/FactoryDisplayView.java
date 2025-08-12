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
 * Displays a single factory. A slot is empty if only the tile's border is shown.
 */
public class FactoryDisplayView extends JPanel {

  private static final float OPAQUE = 1;
  private final Controller controller;
  private ArrayList<Tiles> factoryDisplay;
  private final ArrayList<Point> tilePositions;
  private final int factoryId;
  private final int tileSize;
  private boolean isClickable;

  /**
   * Creates the FactoryDisplayView with a specific tile size.
   * Calculates the tile's positions and borders based on the tile size.
   *
   * @param tileSize  the tile-size
   * @param model     the game's model
   * @param factoryId the factory's ID
   */
  public FactoryDisplayView(int tileSize, Model model, int factoryId, Controller controller) {
    if (factoryId >= model.getFactoryDisplays().getAllDisplays().size()) {
      throw new AssertionError(
          "factoryID " + factoryId + " doesn't exist in the model, try a lower factoryID");
    }

    factoryDisplay = model.getFactoryDisplays().getAllDisplays().get(factoryId);
    this.tileSize = tileSize;
    this.factoryId = factoryId;
    this.controller = controller;
    isClickable = true;

    int outerBorderSize = calculateOuterBorder(tileSize);
    int innerBorderSize = calculateInnerBorder(tileSize);

    int outerTileStart = outerBorderSize + tileSize + innerBorderSize;

    tilePositions = new ArrayList<>();
    tilePositions.add(new Point(outerBorderSize, outerBorderSize));
    tilePositions.add(new Point(outerTileStart, outerBorderSize));
    tilePositions.add(new Point(outerBorderSize, outerTileStart));
    tilePositions.add(new Point(outerTileStart, outerTileStart));

    int length = 2 * tileSize + innerBorderSize + 2 * outerBorderSize;
    setPreferredSize(new Dimension(length, length));
    configureActionListener();
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;
    setBackground(Color.DARK_GRAY);
    g2D.setColor(Color.BLACK);

    drawOutlines(g2D);
    drawTiles(g2D);
  }

  private void drawOutlines(Graphics2D g2D) {
    for (Point tilePosition : tilePositions) {
      g2D.drawRect(tilePosition.x, tilePosition.y, tileSize, tileSize);
    }
  }

  private void drawTiles(Graphics2D g2D) {
    for (int i = 0; i < factoryDisplay.size(); i++) {
      String imagePath = getFilePath(factoryDisplay.get(i));
      Optional<BufferedImage> image = loadBufferedImage(imagePath, tileSize, tileSize, OPAQUE);

      if (image.isPresent()) {
        g2D.drawImage(image.get(), tilePositions.get(i).x, tilePositions.get(i).y, null);
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
        Point pointClicked = e.getPoint();

        if (isPointInSquare(pointClicked, tilePositions.get(0), tileSize)) {
          controller.collectTilesFromDisplay(factoryId, 0);
        } else if (isPointInSquare(pointClicked, tilePositions.get(1), tileSize)) {
          controller.collectTilesFromDisplay(factoryId, 1);
        } else if (isPointInSquare(pointClicked, tilePositions.get(2), tileSize)) {
          controller.collectTilesFromDisplay(factoryId, 2);
        } else if (isPointInSquare(pointClicked, tilePositions.get(3), tileSize)) {
          controller.collectTilesFromDisplay(factoryId, 3);
        }
      }
    });
  }

  public void setSource(ArrayList<Tiles> factoryDisplay) {
    this.factoryDisplay = factoryDisplay;
  }

  public void setClickable(boolean clickable) {
    isClickable = clickable;
  }
}
