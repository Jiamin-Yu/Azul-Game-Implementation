package de.lmu.ifi.sosylab.client.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Contains a title and text-fields that let the user enter username, lobby and server-address.
 * A "play"- and "back"-button are present.
 */
public class LoginPanel extends JPanel {

  private static final int TEXTFIELD_COLUMNS = 20;
  private static final String TITLE = "ONLINE-PLAY";
  private static final String USERNAME = "Username";
  private static final String LOBBY = "Room-name";
  private static final String ADDRESS = "Server-Address";
  private static final String PLAY = "PLAY";
  private static final String BACK = "back";
  final GameFrame gameFrame;
  private JLabel titleLabel;
  private JLabel usernameLabel;
  private JLabel lobbyLabel;
  private JLabel addressLabel;
  private JTextField usernameTextField;
  private JTextField lobbyTextField;
  private JTextField addressTextField;
  private JButton playButton;
  private JButton backButton;


  /**
   * Creates the login panel and adds event listeners to it.
   *
   * @param gameFrame the GameFrame-object that created this LoginPanel
   */
  public LoginPanel(GameFrame gameFrame) {
    setLayout(new BorderLayout());
    this.gameFrame = gameFrame;

    initialiseWidgets();
    addEventListeners();
    createView();
  }

  private void initialiseWidgets() {
    titleLabel = new JLabel(TITLE);
    usernameLabel = new JLabel(USERNAME);
    lobbyLabel = new JLabel(LOBBY);
    addressLabel = new JLabel(ADDRESS);

    usernameTextField = new JTextField(TEXTFIELD_COLUMNS);
    lobbyTextField = new JTextField(TEXTFIELD_COLUMNS);
    addressTextField = new JTextField(TEXTFIELD_COLUMNS);

    playButton = new JButton(PLAY);
    backButton = new JButton(BACK);
  }

  private void addEventListeners() {
    playButton.addActionListener(e -> {
      String username = usernameTextField.getText();
      String lobby = lobbyTextField.getText();
      String address = addressTextField.getText();

      gameFrame.handleOnlinePlayClickEvent(username, lobby, address);
    });

    backButton.addActionListener(e -> gameFrame.showGamemodeSelectionCard());
  }

  private void createView() {
    // The mainPanel contains title, text-fields and their labels
    JPanel mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBackground(gameFrame.getBackground());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 5, 25, 5);

    mainPanel.add(titleLabel, gbc);

    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.LINE_START;

    gbc.gridy = 1;
    mainPanel.add(usernameLabel, gbc);
    gbc.gridy = 2;
    mainPanel.add(usernameTextField, gbc);

    gbc.gridy = 3;
    mainPanel.add(lobbyLabel, gbc);
    gbc.gridy = 4;
    mainPanel.add(lobbyTextField, gbc);

    gbc.gridy = 5;
    mainPanel.add(addressLabel, gbc);
    gbc.gridy = 6;
    mainPanel.add(addressTextField, gbc);

    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridy = 7;
    mainPanel.add(playButton, gbc);

    add(mainPanel, BorderLayout.CENTER);

    // The bottomPanel contains the back button
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomPanel.setBackground(gameFrame.getBackground());
    bottomPanel.add(backButton);

    add(bottomPanel, BorderLayout.SOUTH);
  }
}
