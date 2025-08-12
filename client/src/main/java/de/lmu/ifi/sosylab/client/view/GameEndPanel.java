package de.lmu.ifi.sosylab.client.view;

import de.lmu.ifi.sosylab.client.model.Model;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Displays the game's results.
 */
public class GameEndPanel extends JPanel {

  private static final String TITLE = "RANKING";
  private static final String ROW_TEXT = "%d. %s (%d)";
  private static final String CONFIRM = "ok";
  private final Model model;
  private final GameFrame gameFrame;
  private final LinkedHashMap<Integer, Integer> ranking;
  private JLabel titleLabel;
  private ArrayList<JLabel> rankingElementLabels;
  private JButton confirmButton;

  GameEndPanel(Model model, GameFrame gameFrame, LinkedHashMap<Integer, Integer> ranking) {
    setLayout(new GridBagLayout());
    this.model = model;
    this.gameFrame = gameFrame;
    this.ranking = ranking;

    initialiseWidgets();
    createView();
    addEventListeners();
  }

  private void initialiseWidgets() {
    rankingElementLabels = new ArrayList<>();

    titleLabel = new JLabel(TITLE);
    confirmButton = new JButton(CONFIRM);

    ArrayList<String> rankingTextRows = createRankingTextRows();

    for (String rankingTextRow : rankingTextRows) {
      rankingElementLabels.add(new JLabel(rankingTextRow));
    }
  }

  private void createView() {
    setBackground(gameFrame.getBackground());
    GridBagConstraints gbc = new GridBagConstraints();

    gbc.insets = new Insets(0, 5, 25, 5);

    add(titleLabel, gbc);

    gbc.anchor = GridBagConstraints.LINE_START;
    gbc.insets = new Insets(5, 5, 5, 5);
    for (int i = 0; i < rankingElementLabels.size(); i++) {
      gbc.gridy = i + 1;
      add(rankingElementLabels.get(i), gbc);
    }

    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridy++;
    add(confirmButton, gbc);
  }

  private void addEventListeners() {
    confirmButton.addActionListener(e -> gameFrame.showGamemodeSelectionCard());
  }

  private ArrayList<String> createRankingTextRows() {
    ArrayList<String> rankingTextRows = new ArrayList<>();

    for (Map.Entry<Integer, Integer> entry : ranking.entrySet()) {
      int userId = entry.getKey();
      int score = model.getScores().get(userId);
      String username = model.getGamePlayers().getUsernames().get(userId);

      String rankingTextRow = String.format(ROW_TEXT, entry.getValue(), username, score);
      rankingTextRows.add(rankingTextRow);
    }

    return rankingTextRows;
  }


}
