package de.lmu.ifi.sosylab.client.view.playboard;

import static de.lmu.ifi.sosylab.client.view.ViewUtils.calculateInnerBorder;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.calculateOuterBorder;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.getFilePath;
import static de.lmu.ifi.sosylab.client.view.ViewUtils.loadBufferedImage;

import de.lmu.ifi.sosylab.client.model.Model;
import de.lmu.ifi.sosylab.shared.WallTile;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Optional;
import javax.swing.JPanel;

/**
 * Displays Azul's wall, where tiles form a square to inform the player about their current
 * progress. Placed tiles are fully opaque, while tiles that have not been played are transparent.
 */
public class Wall extends JPanel {

  private static final float OPAQUE = 1;
  private static final float TRANSPARENT = 0.2f;
  private WallTile[][] wall;
  private final int outerBorderSize;
  private final int innerBorderSize;
  private int tileSize;

  /**
   * Creates the wall-panel with a specified tile-size. Panel- and border-size depend on the
   * tile-size.
   *
   * @param tileSize the tile-size
   * @param model    the game's model
   */
  Wall(int tileSize, Model model, int userId) {
    wall = model.getPlayerBoard(userId).getWall();

    this.tileSize = tileSize;
    outerBorderSize = calculateOuterBorder(tileSize);
    innerBorderSize = calculateInnerBorder(tileSize);

    updatePreferredSize();
  }

  private void updatePreferredSize() {
    int length = wall.length * tileSize + 2 * outerBorderSize + (wall.length - 1) * innerBorderSize;
    setPreferredSize(new Dimension(length, length));

    setBackground(Color.DARK_GRAY);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2D = (Graphics2D) g;

    for (int i = 0; i < wall.length; i++) {
      for (int j = 0; j < wall[i].length; j++) {

        float opacity = TRANSPARENT;
        if (wall[i][j].getIsOnWall()) {
          opacity = OPAQUE;
        }

        String imagePath = getFilePath(wall[i][j].getTile());
        Optional<BufferedImage> image = loadBufferedImage(imagePath, tileSize, tileSize, opacity);

        if (image.isPresent()) {
          g2D.drawImage(image.get(), outerBorderSize + j * innerBorderSize + j * tileSize,
              outerBorderSize + i * innerBorderSize + i * tileSize, null);
        }
      }
    }
  }

  public void setSource(WallTile[][] wall) {
    this.wall = wall;
  }

  /**
   * Changes the elements size and its tile-size.
   *
   * @param tileSize the tile-size
   */
  public void setTileSize(int tileSize) {
    this.tileSize = tileSize;
    updatePreferredSize();
  }
}
