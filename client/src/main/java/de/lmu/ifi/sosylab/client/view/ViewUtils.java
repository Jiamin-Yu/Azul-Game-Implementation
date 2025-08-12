package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.shared.Tiles;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import javax.imageio.ImageIO;

/**
 * Contains static methods used for the view.
 */
public final class ViewUtils {

  private static final String RED_PATH = "square-red.png";
  private static final String BLUE_PATH = "circle-blue.png";
  private static final String GREEN_PATH = "diamond-green.png";
  private static final String YELLOW_PATH = "sparkle-yellow.png";
  private static final String DARK_PATH = "triangle-purple.png";
  private static final String ONE_PATH = "star-orange.png";

  private ViewUtils() {
    // Class is final to prevent extension
    // Constructor is private to prevent instantiation
  }

  /**
   * A {@link BufferedImage} is returned if a file exists at the given path.
   * If the loading of the image fails the optional is empty.
   * The image's dimensions equals the file's original width and length.
   *
   * @param path the image's path
   * @return an optional containing the image in case of success
   */
  public static Optional<BufferedImage> loadBufferedImage(String path) {
    try {

      URL url = ViewUtils.class.getClassLoader().getResource(path);

      if (url == null) {
        return Optional.empty();
      }

      try (InputStream stream = url.openStream()) {
        return Optional.of(ImageIO.read(stream));
      }
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }

  /**
   * A {@link BufferedImage} is returned if a file exists at the given path.
   * If the loading of the image fails the optional is empty.
   * The image's dimensions equals the specified parameters.
   *
   * @param path    the image's path
   * @param width   the width the image will be loaded with, expects a value greater than zero
   * @param height  the height the image will be loaded with, expects a value greater than zero
   * @param opacity the opacity the image will be loaded with, expects a value between zero and one
   * @return an optional containing the image in case of success
   */
  public static Optional<BufferedImage> loadBufferedImage(String path, int width, int height,
                                                          float opacity) {
    Optional<BufferedImage> originalImage = loadBufferedImage(path);

    if (originalImage.isEmpty() || width <= 0 || height <= 0 || opacity < 0 || opacity > 1) {
      return Optional.empty();
    }

    // Most of the following lines are copied from the ResourceLoader.java file provided on Uni2Work
    BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = resizedImage.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OUT, opacity));
    g2.drawImage(originalImage.get(), 0, 0, width, height, null);
    g2.dispose();

    return Optional.of(resizedImage);
  }

  /**
   * Returns the file path of a given tile.
   *
   * @param tile the tile whose path is returned
   * @return the file path to the tile's image as String
   */
  public static String getFilePath(Tiles tile) {
    return switch (tile) {
      case RED -> RED_PATH;
      case BLUE -> BLUE_PATH;
      case GREEN -> GREEN_PATH;
      case YELLOW -> YELLOW_PATH;
      case DARK -> DARK_PATH;
      case START -> ONE_PATH;
    };
  }

  /**
   * Returns a boolean providing information about if a point is inside a square.
   *
   * @param point        the point that could be inside the square
   * @param topLeftPoint the square's top left point
   * @param squareSize   the square's size
   * @return true if the point is inside the square, false if it is not
   */
  public static boolean isPointInSquare(Point point, Point topLeftPoint, int squareSize) {
    Point bottomRightPoint = new Point(topLeftPoint.x + squareSize, topLeftPoint.y + squareSize);

    return point.x > topLeftPoint.x && point.y > topLeftPoint.y && point.x < bottomRightPoint.x
        && point.y < bottomRightPoint.y;
  }

  /**
   * Calculates the outer border's size as a fixed fraction of the tile's size.
   *
   * @param tileSize the tile's size the border size depends on
   * @return the outer border's size
   */
  public static int calculateOuterBorder(int tileSize) {
    return tileSize / 4;
  }

  /**
   * Calculates the inner border's size as a fixed fraction of the tile's size.
   *
   * @param tileSize the tile's size the border size depends on
   * @return the inner border's size
   */
  public static int calculateInnerBorder(int tileSize) {
    return tileSize / 8;
  }
}
