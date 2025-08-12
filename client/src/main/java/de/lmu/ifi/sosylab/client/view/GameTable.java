package de.lmu.ifi.sosylab.client.view;

import static de.lmu.ifi.sosylab.client.view.ViewUtils.calculateInnerBorder;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.calculateOuterBorder;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.isPointInSquare;

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
import java.util.List;
import java.util.Optional;
import javax.swing.JPanel;


/**
 * Displays Azul's "center of the table"-element, containing tiles that are not chosen by a player.
 */
public class GameTable extends JPanel {
  private static final float OPAQUE = 1;
  private static final int MAX_NUMBER_OF_ROWS = 5;
  private static final int MAX_NUMBER_OF_COLUMNS = 6;
  private final int outerBorderSize;
  private final int innerBorderSize;
  private final int tileSize;
  private final Controller controller;
  private List<Tiles> gameTable;
  private boolean isClickable;

  /**
   * Sets the table's preferred size and adds action listeners to the panel.
   *
   * @param tileSize   the tile-size
   * @param model      the game's model
   * @param controller the game's controller
   */
  public GameTable(int tileSize, Model model, Controller controller) {
    this.tileSize = tileSize;
    this.outerBorderSize = calculateOuterBorder(tileSize);
    this.innerBorderSize = calculateInnerBorder(tileSize);
    this.gameTable = model.getGameTable();
    this.controller = controller;

    int length = MAX_NUMBER_OF_COLUMNS * tileSize + 2 * outerBorderSize
        + (MAX_NUMBER_OF_COLUMNS - 1) * innerBorderSize;
    int height = MAX_NUMBER_OF_ROWS * tileSize + 2 * outerBorderSize
        + (MAX_NUMBER_OF_ROWS - 1) * innerBorderSize;
    setPreferredSize(new Dimension(length, height));
    configureActionListener();

    isClickable = true;
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;
    setBackground(Color.DARK_GRAY);

    for (int i = 0; i < gameTable.size(); i++) {
      String imagePath = ViewUtils.getFilePath(gameTable.get(i));
      Optional<BufferedImage> image = ViewUtils.loadBufferedImage(imagePath, tileSize, tileSize,
          OPAQUE);
      if (image.isPresent()) {
        g2D.drawImage(image.get(),
            outerBorderSize + (i % MAX_NUMBER_OF_COLUMNS) * innerBorderSize
                + (i % MAX_NUMBER_OF_COLUMNS) * tileSize,
            outerBorderSize + (i / MAX_NUMBER_OF_COLUMNS) * innerBorderSize
                + (i / MAX_NUMBER_OF_COLUMNS) * tileSize, null);
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

        for (int i = 0; i < gameTable.size(); i++) {
          Point posTile = new Point();
          posTile.x = outerBorderSize + (i % MAX_NUMBER_OF_COLUMNS) * innerBorderSize
              + (i % MAX_NUMBER_OF_COLUMNS) * tileSize;
          posTile.y = outerBorderSize + (i / MAX_NUMBER_OF_COLUMNS) * innerBorderSize
              + (i / MAX_NUMBER_OF_COLUMNS) * tileSize;

          if (isPointInSquare(clickedPoint, posTile, tileSize)) {
            if (gameTable.get(i) != Tiles.START) {
              controller.collectTilesFromTable(i);
            }
          }
        }
      }
    });
  }

  public void setSource(List<Tiles> gameTable) {
    this.gameTable = gameTable;
  }

  public void setClickable(boolean clickable) {
    isClickable = clickable;
  }
}
