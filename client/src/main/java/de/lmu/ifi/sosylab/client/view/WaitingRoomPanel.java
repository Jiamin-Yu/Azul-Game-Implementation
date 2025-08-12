package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.controller.GameControllerOnline;
import de.lmu.ifi.sosylab.client.model.GameClientOnlineModel;
import de.lmu.ifi.sosylab.shared.events.CreateViewEvent;
import de.lmu.ifi.sosylab.shared.events.LoginEvent;
import de.lmu.ifi.sosylab.shared.events.UserJoinedEvent;
import de.lmu.ifi.sosylab.shared.events.UserLeftRoomEvent;
import de.lmu.ifi.sosylab.shared.events.UserNotReadyEvent;
import de.lmu.ifi.sosylab.shared.events.UserReadyEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Displays a title and a player list with a "ready"-button.
 * Player's that are ready are shown in green, while player's that aren't are shown in red.
 * At the bottom right a "back"-button is located.
 */
public class WaitingRoomPanel extends JPanel implements PropertyChangeListener {

  private static final int MAX_PLAYER_COUNT = 4;
  private static final String TITLE = "ROOM: ";
  private static final String NO_PLAYER = "-";
  private static final String READY = "READY";
  private static final String NOT_READY = "NOT READY";
  private static final String BACK = "back";
  private static final String ERROR_TITLE = "Error!";
  private static final String ERROR_MESSAGE = "Operation failed";
  private final GameFrame gameFrame;
  private final GameClientOnlineModel gameClientOnlineModel;
  private final GameControllerOnline gameControllerOnline;
  private final ArrayList<JLabel> usernameLabels;
  private boolean ready;
  private JLabel titleLabel;
  private JButton readyButton;
  private JButton backButton;

  /**
   * Creates a new WaitingRoomPanel.
   *
   * @param gameFrame the GameFrame-object that created this HotseatStartPanel
   */
  public WaitingRoomPanel(GameFrame gameFrame, GameClientOnlineModel model,
                          GameControllerOnline controller) {
    setLayout(new BorderLayout());
    this.gameFrame = gameFrame;
    this.gameClientOnlineModel = model;
    this.gameControllerOnline = controller;
    ready = false;

    usernameLabels = new ArrayList<>();

    initialiseWidgets();
    createView();
    addEventListeners();
  }

  private void initialiseWidgets() {
    titleLabel = new JLabel();

    for (int i = 0; i < MAX_PLAYER_COUNT; i++) {
      usernameLabels.add(new JLabel());
    }

    readyButton = new JButton(READY);
    backButton = new JButton(BACK);
  }

  private void addEventListeners() {
    readyButton.addActionListener(e -> handleReadyClickEvent());

    backButton.addActionListener(e -> leaveRoom());
  }

  private void leaveRoom() {
    try {
      gameControllerOnline.leaveRoom();
      gameFrame.showLoginCard();
    } catch (IOException e) {
      showErrorMessage();
    }
  }

  private void handleReadyClickEvent() {
    try {
      if (ready) {
        readyButton.setText(READY);
        gameControllerOnline.signalNotReady();
      } else {
        readyButton.setText(NOT_READY);
        gameControllerOnline.signalReadyToPlay();
      }
      ready = !ready;
    } catch (IOException e) {
      showErrorMessage();
    }
  }

  private void createView() {
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBackground(gameFrame.getBackground());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 5, 25, 5);

    mainPanel.add(titleLabel, gbc);

    gbc.insets = new Insets(5, 5, 5, 5);

    gbc.gridy = 1;
    updateUserDisplay();
    for (JLabel usernameLabel : usernameLabels) {
      mainPanel.add(usernameLabel, gbc);
      gbc.gridy++;
    }

    mainPanel.add(readyButton, gbc);

    add(mainPanel, BorderLayout.CENTER);

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomPanel.setBackground(gameFrame.getBackground());
    bottomPanel.add(backButton);

    add(bottomPanel, BorderLayout.SOUTH);
  }

  /**
   * Updates the GUI when a user's ready-state has changed.
   *
   * @param event the event that was fired by the model
   */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    SwingUtilities.invokeLater(() -> handleChangeEvent(event));
  }

  private void handleChangeEvent(PropertyChangeEvent event) {
    Object newValue = event.getNewValue();
    if (newValue instanceof UserJoinedEvent
        || newValue instanceof UserLeftRoomEvent
        || newValue instanceof LoginEvent
        || newValue instanceof UserReadyEvent
        || newValue instanceof UserNotReadyEvent) {
      updateUserDisplay();
    }else if (newValue instanceof CreateViewEvent) {
      gameFrame.InitialiseGamePanel();
      gameFrame.showGameCard();
    }
  }

  private void updateUserDisplay() {
    HashMap<String, Boolean> users = gameClientOnlineModel.getUsersAndTheirReadinessState();

    int i = 0;
    for (Map.Entry<String, Boolean> entry : users.entrySet()) {
      if (entry.getValue()) {
        usernameLabels.get(i).setForeground(Color.GREEN);
      } else {
        usernameLabels.get(i).setForeground(Color.RED);
      }

      usernameLabels.get(i).setText(entry.getKey());
      i++;
    }

    usernameLabels.get(i).setForeground(Color.BLACK);
    while (i < MAX_PLAYER_COUNT) {
      usernameLabels.get(i).setText(NO_PLAYER);
      i++;
    }
  }

  private void showErrorMessage() {
    JOptionPane.showMessageDialog(this, ERROR_MESSAGE, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Resets button and ready-state to not ready.
   */
  public void resetReadyState() {
    readyButton.setText(READY);
    ready = false;
  }

  public void setRoomName(String roomName) {
    titleLabel.setText(TITLE + roomName);
  }
}
