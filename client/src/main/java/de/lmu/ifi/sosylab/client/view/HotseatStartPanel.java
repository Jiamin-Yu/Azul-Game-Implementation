package de.lmu.ifi.sosylab.client.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * For each user a text-field with a label is present to enter his name.
 * Up to four players can be added by clicking the corresponding button.
 * Similarly, user can be removed as long as there are at least two players remaining.
 * Furthermore a "start"- and a "back-" button are present.
 */
public class HotseatStartPanel extends JPanel {

  private int playerCount;
  private static final int TEXTFIELD_COLUMNS = 20;
  private static final int DEFAULT_USER_COUNT = 2;
  private static final int MAX_USER_COUNT = 4;
  private static final String TITLE = "HOTSEAT-MODE";
  private static final String EMPTY_STRING = "";
  private static final String USERNAME_1 = "Username Player 1";
  private static final String USERNAME_2 = "Username Player 2";
  private static final String USERNAME_3 = "Username Player 3";
  private static final String USERNAME_4 = "Username Player 4";
  private static final String ADD_USER = "+";
  private static final String REMOVE_USER = "-";
  private static final String PLAY = "PLAY";
  private static final String BACK = "back";
  private JLabel titleLabel;
  private ArrayList<JLabel> usernameLabels;
  private ArrayList<JTextField> userTextFields;
  private JButton addUserButton;
  private JButton removeUserButton;
  private JButton playButton;
  private JButton backButton;
  private JPanel mainPanel;
  private final GameFrame gameFrame;
  private final ArrayList<String> usernames;

  /**
   * Creates the hotseat-start and adds event listeners to it.
   *
   * @param gameFrame the GameFrame-object that created this HotseatStartPanel
   */
  public HotseatStartPanel(GameFrame gameFrame) {
    playerCount = DEFAULT_USER_COUNT;
    setLayout(new BorderLayout());

    this.gameFrame = gameFrame;
    usernames = new ArrayList<>();
    usernames.addAll(Collections.nCopies(MAX_USER_COUNT, EMPTY_STRING));
    initialiseWidgets();
    addEventListeners();
    createView();
  }

  private void initialiseWidgets() {
    ArrayList<String> usernameLabelText = new ArrayList<>();
    usernameLabels = new ArrayList<>();
    userTextFields = new ArrayList<>();

    usernameLabelText.add(USERNAME_1);
    usernameLabelText.add(USERNAME_2);
    usernameLabelText.add(USERNAME_3);
    usernameLabelText.add(USERNAME_4);

    titleLabel = new JLabel(TITLE);

    for (String username : usernameLabelText) {
      usernameLabels.add(new JLabel(username));
      userTextFields.add(new JTextField(TEXTFIELD_COLUMNS));
    }

    addUserButton = new JButton(ADD_USER);
    removeUserButton = new JButton(REMOVE_USER);
    backButton = new JButton(BACK);
    playButton = new JButton(PLAY);
    playButton.setEnabled(false);
  }

  private void addEventListeners() {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.LINE_START;

    addUserButton.addActionListener(e -> {
      playerCount++;
      updatePlayButton();

      if (playerCount == 3) {
        gbc.gridy = 5;
        mainPanel.add(usernameLabels.get(2), gbc);
        gbc.gridy = 6;
        mainPanel.add(userTextFields.get(2), gbc);
        add(mainPanel, BorderLayout.CENTER);

        revalidate();
        removeUserButton.setEnabled(true);
      } else if (playerCount == 4) {
        gbc.gridy = 7;
        mainPanel.add(usernameLabels.get(3), gbc);
        gbc.gridy = 8;
        mainPanel.add(userTextFields.get(3), gbc);
        add(mainPanel, BorderLayout.CENTER);

        revalidate();
        addUserButton.setEnabled(false);
      }
    });

    removeUserButton.addActionListener(e -> {
      playerCount--;
      updatePlayButton();

      if (playerCount == 2) {
        mainPanel.remove(usernameLabels.get(2));
        mainPanel.remove(userTextFields.get(2));

        removeUserButton.setEnabled(false);
        revalidate();
      } else if (playerCount == 3) {
        mainPanel.remove(usernameLabels.get(3));
        mainPanel.remove(userTextFields.get(3));

        addUserButton.setEnabled(true);
        revalidate();
      }
    });

    addTextFieldListeners();

    playButton.addActionListener(e -> gameFrame.handleHotseatStartEvent(playerCount, usernames));
    backButton.addActionListener(e -> gameFrame.showGamemodeSelectionCard());
  }

  private void addTextFieldListeners() {
    for (int i = 0; i < userTextFields.size(); i++) {
      int finalI = i;
      userTextFields.get(i).getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
          changedUpdate(e);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
          changedUpdate(e);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
          if (userTextFields.get(finalI).getText().length() > 0) {
            playButton.setEnabled(true);
          }

          usernames.set(finalI, userTextFields.get(finalI).getText());
          updatePlayButton();
        }
      });
    }
  }

  private void updatePlayButton() {
    // Checks if there are duplicates in the list
    ArrayList<String> takenUsernameList = new ArrayList<>(usernames.subList(0, playerCount));
    Set<String> takenUsernameSet = new HashSet<>(takenUsernameList);
    playButton.setEnabled(takenUsernameSet.size() >= takenUsernameList.size());

    // Checks if there are any empty textFields
    for (int i = 0; i < playerCount; i++) {
      if (userTextFields.get(i).getText().length() == 0) {
        playButton.setEnabled(false);
      }
    }
  }

  private void createView() {
    // The main panel contains everything except the "back"-button
    mainPanel = new JPanel(new GridBagLayout());
    mainPanel.setBackground(gameFrame.getBackground());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(0, 5, 25, 5);
    gbc.gridwidth = 2;

    mainPanel.add(titleLabel, gbc);

    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.anchor = GridBagConstraints.LINE_START;

    gbc.gridy = 1;
    mainPanel.add(usernameLabels.get(0), gbc);
    gbc.gridy = 2;
    mainPanel.add(userTextFields.get(0), gbc);

    gbc.gridy = 3;
    mainPanel.add(usernameLabels.get(1), gbc);
    gbc.gridy = 4;
    mainPanel.add(userTextFields.get(1), gbc);

    gbc.gridwidth = 1;
    gbc.anchor = GridBagConstraints.LINE_START;

    // Leave four spaces free to insert elements when the addUser-button is clicked
    gbc.gridy = 9;
    mainPanel.add(removeUserButton, gbc);
    removeUserButton.setEnabled(false);

    gbc.anchor = GridBagConstraints.LINE_END;
    gbc.gridx = 1;
    mainPanel.add(addUserButton, gbc);

    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridwidth = 2;

    gbc.gridy = 10;
    gbc.gridx = 0;
    mainPanel.add(playButton, gbc);

    add(mainPanel, BorderLayout.CENTER);

    // The panel containing the back-button
    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    bottomPanel.setBackground(gameFrame.getBackground());
    bottomPanel.add(backButton);

    add(bottomPanel, BorderLayout.SOUTH);
  }
}
