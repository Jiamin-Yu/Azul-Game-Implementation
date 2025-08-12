package de.lmu.ifi.sosylab.client.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Contains a title and two buttons below. Each button leads to one of the game-modes.
 */
public class GamemodeSelectionPanel extends JPanel {

  private static final String TITLE = "AZUL";
  private static final String ONLINE = "Online";
  private static final String HOTSEAT = "Hotseat";
  final GameFrame gameFrame;
  private JLabel titleLabel;
  private JButton onlineButton;
  private JButton hotseatButton;

  /**
   * Creates the game-mode selection panel and adds event listeners to it.
   *
   * @param gameFrame to switch the GameFrame's card
   */
  public GamemodeSelectionPanel(GameFrame gameFrame) {
    setLayout(new GridBagLayout());
    this.gameFrame = gameFrame;

    initialiseWidgets();
    addEventListeners();
    createView();
  }

  private void initialiseWidgets() {
    titleLabel = new JLabel(TITLE);

    onlineButton = new JButton(ONLINE);
    hotseatButton = new JButton(HOTSEAT);
  }

  private void addEventListeners() {
    hotseatButton.addActionListener(e -> gameFrame.showHotseatStartCard());
    onlineButton.addActionListener(e -> gameFrame.showLoginCard());
  }

  private void createView() {
    setBackground(gameFrame.getBackground());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(15, 5, 15, 5);

    gbc.gridy = 0;
    gbc.gridx = 0;
    gbc.gridwidth = 2;
    add(titleLabel, gbc);

    gbc.gridy = 1;
    gbc.gridwidth = 1;
    add(hotseatButton, gbc);

    gbc.gridx = 1;
    add(onlineButton, gbc);
  }
}
