package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.Controller;
import de.lmu.ifi.sosylab.client.model.Model;
import de.lmu.ifi.sosylab.client.view.playboard.FactoryDisplayView;
import de.lmu.ifi.sosylab.client.view.playboard.PlayerBoardView;
import de.lmu.ifi.sosylab.shared.PlayerBoard;
import de.lmu.ifi.sosylab.shared.events.FactoryDisplaysChangeEvent;
import de.lmu.ifi.sosylab.shared.events.FloorLineChangeEvent;
import de.lmu.ifi.sosylab.shared.events.GameEndEvent;
import de.lmu.ifi.sosylab.shared.events.GameTableChangeEvent;
import de.lmu.ifi.sosylab.shared.events.PatternLinesChangeEvent;
import de.lmu.ifi.sosylab.shared.events.PlaceTilesFailEvent;
import de.lmu.ifi.sosylab.shared.events.PlayerTurnFinishedEvent;
import de.lmu.ifi.sosylab.shared.events.RestartRequestEvent;
import de.lmu.ifi.sosylab.shared.events.StartGameEvent;
import de.lmu.ifi.sosylab.shared.events.StartNextRoundEvent;
import de.lmu.ifi.sosylab.shared.events.TurnEvent;
import de.lmu.ifi.sosylab.shared.events.UserLeftGameEvent;
import de.lmu.ifi.sosylab.shared.events.WallChangeEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Displays the game containing {@link FactoryDisplayView}s, {@link PlayerBoardView}s and the
 * {@link GameTable}.
 */
public class GamePanel extends JPanel implements PropertyChangeListener {

  private static final int TILE_SIZE = 15;
  private static final String ERROR_TITLE = "Error";
  private static final String ILLEGAL_MOVE = "Illegal move!";
  private static final String GAME_START_TITLE = "Game Start";
  private static final String TURN_END_TITLE = "Turn end";
  private static final String STOP = "Stop Game";
  private static final String RESTART = "Restart Game";
  private static final String RESTART_TITLE = "Restart?";
  private static final String RESTART_MESSAGE = "A player requests a restart, ok?";
  private static final String USER_LEFT_MESSAGE =
      "The game is stopped because a player left the game.";
  private final Model model;
  private final Controller controller;
  private final GameFrame gameFrame;
  private final ArrayList<FactoryDisplayView> factoryDisplayViews;
  private final ArrayList<PlayerBoardView> playerBoardViews;
  private int factoryDisplayCount;
  private int activePlayer;
  private JLabel infoLabel;
  private JButton stopButton;
  private JButton restartButton;
  private GameTable gameTable;

  /**
   * Constructs the GamePanel.
   *
   * @param model      the game's model
   * @param controller the game's controller
   * @param gameFrame  the GameFrame-object that created this HotseatStartPanel
   */
  GamePanel(Model model, Controller controller, GameFrame gameFrame) {
    setLayout(new BorderLayout());

    this.model = model;
    this.controller = controller;
    this.gameFrame = gameFrame;

    factoryDisplayViews = new ArrayList<>();
    playerBoardViews = new ArrayList<>();
  }

  /**
   * Initialises this GamePanel.
   */
  public void initialise() {
    initialiseWidgets();
    createView();
    addEventListeners();
  }

  private void initialiseWidgets() {
    createPlayerBoardViews();
    createFactoryDisplays();

    gameTable = new GameTable(TILE_SIZE, model, controller);
    stopButton = new JButton(STOP);
    restartButton = new JButton(RESTART);

    infoLabel = new JLabel();
    infoLabel.setForeground(Color.WHITE);
    updateActivePlayerBoardView();
  }

  private void createPlayerBoardViews() {
    int playerCount = model.getPlayerCount();
    ArrayList<String> usernames = model.getGamePlayers().getUsernames();

    for (int i = 0; i < playerCount; i++) {
      PlayerBoardView playerBoardView =
          new PlayerBoardView(usernames.get(i), i, TILE_SIZE, model, controller);
      playerBoardViews.add(playerBoardView);
    }
  }

  private void createFactoryDisplays() {
    factoryDisplayCount = model.getFactoryDisplays().getAllDisplays().size();

    for (int i = 0; i < factoryDisplayCount; i++) {
      FactoryDisplayView fdw = new FactoryDisplayView(TILE_SIZE, model, i, controller);
      factoryDisplayViews.add(fdw);
    }
  }

  private void updateActivePlayerBoardView() {
    changeClickabilty();
    changeHighlighting();
  }

  private void changeClickabilty() {
    for (PlayerBoardView playerBoardView : playerBoardViews) {
      playerBoardView.getFloorLine().setClickable(false);
      playerBoardView.getPatternLines().setClickable(false);
    }

    playerBoardViews.get(model.getCurrentPlayer()).getFloorLine().setClickable(true);
    playerBoardViews.get(model.getCurrentPlayer()).getPatternLines().setClickable(true);
  }

  private void changeHighlighting() {
    for (PlayerBoardView playerBoardView : playerBoardViews) {
      playerBoardView.changeBackgroundColor(Color.DARK_GRAY);
    }

    Color highlightingGray = new Color(75, 75, 75);
    playerBoardViews.get(model.getCurrentPlayer()).changeBackgroundColor(highlightingGray);
  }

  private void createView() {
    JPanel factoryDisplayPanel = createFactoryDisplayPanel();
    add(factoryDisplayPanel, BorderLayout.NORTH);

    JPanel mainPanel = createMainPanel();
    add(mainPanel, BorderLayout.CENTER);

    JPanel bottomPanel = createBottomPanel();
    add(bottomPanel, BorderLayout.SOUTH);
  }

  private JPanel createFactoryDisplayPanel() {
    JPanel factoryDisplayPanel = new JPanel(new FlowLayout());
    factoryDisplayPanel.setBackground(Color.DARK_GRAY);

    for (int i = 0; i < factoryDisplayCount; i++) {
      factoryDisplayPanel.add(factoryDisplayViews.get(i));
    }

    return factoryDisplayPanel;
  }

  private JPanel createMainPanel() {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBackground(Color.DARK_GRAY);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);

    gbc.gridx = 1;
    mainPanel.add(playerBoardViews.get(0), gbc);

    gbc.gridx = 2;
    gbc.gridy = 1;
    mainPanel.add(playerBoardViews.get(1), gbc);

    if (playerBoardViews.size() > 2) {
      gbc.gridx = 1;
      gbc.gridy = 2;
      mainPanel.add(playerBoardViews.get(2), gbc);
    }

    if (playerBoardViews.size() > 3) {
      gbc.gridx = 0;
      gbc.gridy = 1;
      mainPanel.add(playerBoardViews.get(3), gbc);
    }

    gbc.gridy = 1;
    gbc.gridx = 1;
    mainPanel.add(gameTable, gbc);

    return mainPanel;
  }

  private JPanel createBottomPanel() {
    JPanel bottomPanel = new JPanel(new GridBagLayout());
    bottomPanel.setBackground(Color.DARK_GRAY);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.weightx = 1;
    gbc.insets = new Insets(5, 5, 5, 5);

    gbc.anchor = GridBagConstraints.WEST;
    bottomPanel.add(infoLabel, gbc);

    gbc.anchor = GridBagConstraints.EAST;
    gbc.weightx = 0;
    bottomPanel.add(stopButton, gbc);
    bottomPanel.add(restartButton, gbc);

    return bottomPanel;
  }

  private void addEventListeners() {
    stopButton.addActionListener(e -> {
      controller.stopGame();
      gameFrame.showGamemodeSelectionCard();
    });

    restartButton.addActionListener(e -> {
      controller.restart();

      updateActivePlayerBoardView();
      resetAllSources();
      repaintEverything();
    });
  }

  private void resetAllSources() {
    gameTable.setSource(model.getGameTable());

    for (int i = 0; i < factoryDisplayViews.size(); i++) {
      factoryDisplayViews.get(i).setSource(model.getFactoryDisplays().getAllDisplays().get(i));
    }

    for (int i = 0; i < playerBoardViews.size(); i++) {
      PlayerBoardView playerBoardView = playerBoardViews.get(i);
      PlayerBoard playerBoard = model.getPlayerBoard(i);

      playerBoardView.getPatternLines().setSource(playerBoard.getPatternLines());
      playerBoardView.getFloorLine().setSource(playerBoard.getFloorLine());
      playerBoardView.getWall().setSource(playerBoard.getWall());
    }
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    SwingUtilities.invokeLater(() -> handleChangeEvent(event));
  }

  /**
   * The observable (= model) has just published that its state has changed. The GUI needs to be
   * updated accordingly.
   *
   * @param event the event that was fired by the model
   */
  private void handleChangeEvent(PropertyChangeEvent event) {
    int playerId = model.getCurrentPlayer();

    Object newValue = event.getNewValue();
    if (newValue instanceof FloorLineChangeEvent) {
      playerBoardViews.get(activePlayer).getFloorLine().repaint();
    } else if (newValue instanceof GameTableChangeEvent) {
      activePlayer = playerId;
      gameTable.repaint();
    } else if (newValue instanceof PatternLinesChangeEvent) {
      playerBoardViews.get(activePlayer).getPatternLines().repaint();
    } else if (newValue instanceof PlaceTilesFailEvent) {
      showErrorMessage();
    } else if (newValue instanceof WallChangeEvent) {
      playerBoardViews.get(playerId).getWall().repaint();
    } else if (newValue instanceof StartNextRoundEvent) {
      repaintEverything();
      updateActivePlayerBoardView();

      String nextPlayerName = model.getGamePlayers().getUsernames().get(playerId);
      String message = nextPlayerName + ", it's your turn now!";
      infoLabel.setText(message);
      showPlainMessage(TURN_END_TITLE, message);
    } else if (newValue instanceof StartGameEvent) {
      String startPlayerName = model.getGamePlayers().getUsernames().get(playerId);
      infoLabel.setText(startPlayerName + ", you were chosen to start the game, congratulations!");
      showPlainMessage(GAME_START_TITLE,
          startPlayerName + ", you were chosen to start the game, congratulations!");
    } else if (newValue instanceof PlayerTurnFinishedEvent) {
      updateActivePlayerBoardView();

      String nextPlayerName = model.getGamePlayers().getUsernames().get(playerId);
      String message = nextPlayerName + ", it's your turn now!";
      infoLabel.setText(message);
      showPlainMessage(TURN_END_TITLE, message);
    } else if (newValue instanceof TurnEvent) {
      updateActivePlayerBoardView();

      String nextPlayerName = model.getGamePlayers().getUsernames().get(playerId);
      String message = nextPlayerName + ", it's your turn now!";
      infoLabel.setText(message);
    } else if (newValue instanceof FactoryDisplaysChangeEvent) {

      for (FactoryDisplayView factoryDisplayView : factoryDisplayViews) {
        factoryDisplayView.repaint();
      }

      factoryDisplayViews.get(model.getCurrentDisplay()).repaint();
      gameTable.repaint();
    } else if (newValue instanceof GameEndEvent gameEndEvent) {
      gameFrame.showEndCard(gameEndEvent.getRanking());
    } else if (newValue instanceof RestartRequestEvent) {
      System.out.println("RestartRequestEvent received");
      showRequestRestart();
    } else if (newValue instanceof UserLeftGameEvent) {
      JOptionPane.showMessageDialog(this, USER_LEFT_MESSAGE);
      gameFrame.waitingRoomPanel.resetReadyState();
      gameFrame.showWaitingRoomCard();
    }
  }

  private void showErrorMessage() {
    JOptionPane.showMessageDialog(this, ILLEGAL_MOVE, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
  }

  private void showPlainMessage(String title, String message) {
    JOptionPane.showMessageDialog(this, message, title, JOptionPane.PLAIN_MESSAGE);
  }

  private void showRequestRestart() {
    int answer = JOptionPane.showConfirmDialog(this, RESTART_TITLE, RESTART_MESSAGE,
        JOptionPane.YES_NO_OPTION);
    if (answer == JOptionPane.YES_OPTION) {
      System.out.println("yes");
      controller.replyRestartRequest(true);
    } else {
      System.out.println("no");
      controller.replyRestartRequest(true);

    }
  }

  private void repaintEverything() {
    gameTable.repaint();

    for (FactoryDisplayView factoryDisplayView : factoryDisplayViews) {
      factoryDisplayView.repaint();
    }

    for (PlayerBoardView playerBoardView : playerBoardViews) {
      playerBoardView.getPatternLines().repaint();
      playerBoardView.getWall().repaint();
      playerBoardView.getFloorLine().repaint();
      playerBoardView.updateScore();
    }
  }
}
