package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.Controller;
import de.lmu.ifi.sosylab.client.controller.GameController;
import de.lmu.ifi.sosylab.client.controller.GameControllerOnline;
import de.lmu.ifi.sosylab.client.model.GameClientModel;
import de.lmu.ifi.sosylab.client.model.GameClientOnlineModel;
import de.lmu.ifi.sosylab.client.model.Model;
import de.lmu.ifi.sosylab.shared.events.LoginEvent;
import de.lmu.ifi.sosylab.shared.events.LoginFailedEvent;
import java.awt.CardLayout;
import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * The game's main frame that contains and controls all sub panels.
 */
public class GameFrame extends JFrame implements PropertyChangeListener {

  private static final String AZUL_TITLE = "Azul";
  private static final String GAMEMODE_SELECTION_CARD = "gamemodeSelection";
  private static final String HOTSEAT_START_CARD = "hotseatStart";
  private static final String LOGIN_CARD = "login";
  private static final String WAITING_ROOM_CARD = "waitingRoom";
  private static final String GAME_CARD = "game";
  private static final String END_CARD = "end";
  private static final String LOGIN_FAILED_TITLE = "Login failed";
  private static final int MINIMUM_FRAME_WIDTH = 650;
  private static final int MINIMUM_FRAME_HEIGHT = 650;
  final CardLayout layout;
  GamePanel gamePanel;
  WaitingRoomPanel waitingRoomPanel;
  GameEndPanel gameEndPanel;
  private Model model;
  private Controller controller;

  /**
   * Initialises the GameFrame.
   */
  public GameFrame() {
    super(AZUL_TITLE);
    setSize(MINIMUM_FRAME_WIDTH, MINIMUM_FRAME_HEIGHT);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setBackground(Color.LIGHT_GRAY);

    layout = new CardLayout();

    createView();
  }

  private void createView() {
    JPanel panel = new JPanel(layout);
    setContentPane(panel);

    JPanel gamemodeSelectionPanel = new GamemodeSelectionPanel(this);
    add(gamemodeSelectionPanel, GAMEMODE_SELECTION_CARD);

    JPanel hotseatStartPanel = new HotseatStartPanel(this);
    add(hotseatStartPanel, HOTSEAT_START_CARD);

    JPanel loginPanel = new LoginPanel(this);
    add(loginPanel, LOGIN_CARD);
  }

  /**
   * Let the model start the game.
   * Initialise and switch to the {@link GamePanel}.
   *
   * @param playerCount the number of players the game should be started with
   * @param usernames   the player's chosen names
   */
  public void handleHotseatStartEvent(int playerCount, ArrayList<String> usernames) {
    GameClientModel model = new GameClientModel();
    GameController controller = new GameController(model);
    this.model = model;
    this.controller = controller;

    // Remove the old game if there is one
    if (gamePanel != null) {
      model.removePropertyChangeListener(gamePanel);
      remove(gamePanel);
    }

    gamePanel = new GamePanel(model, controller, this);
    model.addPropertyChangeListener(gamePanel);

    controller.startGame(playerCount, usernames);

    gamePanel.initialise();
    add(gamePanel, GAME_CARD);

    showGameCard();
  }

  /**
   * Forwards a user's click on the play button to the controller.
   *
   * @param username the player's chosen username
   * @param roomName the name of the room the player wants to connect to
   * @param address  the server-address the user wants to connect to
   */
  public void handleOnlinePlayClickEvent(String username, String roomName, String address) {
    final GameClientOnlineModel model = new GameClientOnlineModel();
    final GameControllerOnline controller = new GameControllerOnline(model);
    this.model = model;
    this.controller = controller;



    if (waitingRoomPanel != null) {
      model.removePropertyChangeListener(waitingRoomPanel);
      remove(waitingRoomPanel);
    }


    waitingRoomPanel = new WaitingRoomPanel(this, model, controller);
    add(waitingRoomPanel, WAITING_ROOM_CARD);
    model.addPropertyChangeListener(waitingRoomPanel);
    model.addPropertyChangeListener(this);

    controller.login(username, roomName, address);
    waitingRoomPanel.setRoomName(model.getRoomName());

    if (this.gamePanel != null) {
      model.removePropertyChangeListener(gamePanel);
      remove(gamePanel);
    }

    gamePanel = new GamePanel(model, controller, this);
    model.addPropertyChangeListener(gamePanel);
    add(gamePanel, GAME_CARD);



  }

  /**
   * Initialises the {@link GamePanel}.
   */
  public void InitialiseGamePanel() {
    gamePanel.initialise();
    gamePanel.revalidate();
  }

  /**
   * Shows the game-mode selection card.
   */
  public void showGamemodeSelectionCard() {
    showCard(GAMEMODE_SELECTION_CARD);
  }

  /**
   * Shows the hotseat-start card.
   */
  public void showHotseatStartCard() {
    showCard(HOTSEAT_START_CARD);
  }

  /**
   * Shows the login card.
   */
  public void showLoginCard() {
    showCard(LOGIN_CARD);
  }

  /**
   * Shows the online mode's waiting-room.
   */
  public void showWaitingRoomCard() {
    showCard(WAITING_ROOM_CARD);
  }

  /**
   * Shows the game card.
   */
  public void showGameCard() {
    showCard(GAME_CARD);
  }

  /**
   * Initialises and shows the end card.
   */
  public void showEndCard(LinkedHashMap<Integer, Integer> ranking) {
    if (gameEndPanel != null) {
      remove(gamePanel);
    }

    gameEndPanel = new GameEndPanel(model, this, ranking);
    add(gameEndPanel, END_CARD);

    showCard(END_CARD);
  }

  private void showCard(String card) {
    layout.show(getContentPane(), card);
  }

  @Override
  public void dispose() {
    super.dispose();

    if (model != null) {
      model.removePropertyChangeListener(gamePanel);
      model.removePropertyChangeListener(waitingRoomPanel);
      model.removePropertyChangeListener(this);
    }

    if (controller != null) {
      controller.stopGame();
    }
  }

  public void setModel(Model model) {
    this.model = model;
  }

  /**
   * Updates the GUI when a user's ready-state has changed.
   */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    SwingUtilities.invokeLater(() -> handleChangeEvent(event));
  }

  private void handleChangeEvent(PropertyChangeEvent event) {
    Object newValue = event.getNewValue();
    if (newValue instanceof LoginEvent) {
      showWaitingRoomCard();
    } else if (newValue instanceof LoginFailedEvent loginFailedEvent) {
      showErrorMessage(loginFailedEvent.getCause());
    }
  }

  private void showErrorMessage(String message) {
    JOptionPane.showMessageDialog(this, message, LOGIN_FAILED_TITLE, JOptionPane.ERROR_MESSAGE);
  }
}