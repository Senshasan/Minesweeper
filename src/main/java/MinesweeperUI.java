import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MinesweeperUI extends JFrame {
    private final JLabel statusLabel;
    private final JLabel timerLabel;
    private final JPanel boardPanel;
    private MinesweeperLogic game;

    public MinesweeperUI() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title panel
        JPanel titlePanel = new JPanel();
        ImageIcon titleImage = new ImageIcon(getClass().getResource("Title1.png"));
        JLabel imageLabel = new JLabel(titleImage);
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(imageLabel);
        add(titlePanel, BorderLayout.NORTH);

        // Score panel
        JPanel scorePanel = new JPanel();
        statusLabel = new JLabel("Score: 0 | Minen: 0");
        statusLabel.setPreferredSize(new Dimension(200, 50));
        scorePanel.setBackground(Color.WHITE);
        scorePanel.add(statusLabel);
        add(scorePanel, BorderLayout.SOUTH);

        // Timer panel
        JPanel timerPanel = new JPanel();
        timerLabel = new JLabel("Time: 0s");
        timerLabel.setPreferredSize(new Dimension(100, 100));
        timerPanel.setBackground(Color.WHITE);
        timerPanel.add(timerLabel);
        add(timerPanel, BorderLayout.LINE_END);

        // Board panel
        boardPanel = new JPanel(new GridLayout(16, 30));
        add(boardPanel, BorderLayout.CENTER);

        // Powerup panel
        JPanel powerupPanel = new JPanel();
        ImageIcon powerupImage = new ImageIcon(getClass().getResource("Powerups.png"));
        JLabel imageLabel2 = new JLabel(powerupImage);
        powerupPanel.setBackground(Color.WHITE);
        powerupPanel.add(imageLabel2);
        add(powerupPanel, BorderLayout.LINE_START);

        // Initialize game
        game = new MinesweeperLogic(this);
        initializeBoard();

        // Add key listener
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (game.isGameOver()) return;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1 -> game.handlePowerup(1); // Single tile reveal
                    case KeyEvent.VK_2 -> game.handlePowerup(2); // 3x3 grid reveal
                    case KeyEvent.VK_3 -> game.handlePowerup(3); // Column reveal
                }
            }
        });

        setFocusable(true);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void initializeBoard() {
        boardPanel.removeAll();
        Tile[][] board = game.getBoard();
        for (int r = 0; r < board.length; r++) {
            for (int c = 0; c < board[0].length; c++) {
                boardPanel.add(board[r][c]);
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    public void updateStatusLabel() {
        statusLabel.setText("Score: " + game.getScore() + " | Minen: " + (game.getMines() - game.getFlagsPlaced() - game.getSafelyRevealedMines()));
    }

    public void updateTimerLabel() {
        int minutes = game.getRemainingTime() / 60;
        int seconds = game.getRemainingTime() % 60;
        timerLabel.setText(String.format("Time: %02d:%02d", minutes, seconds));
    }

    public void restartGame() {
        game = new MinesweeperLogic(this);
        initializeBoard();
        updateStatusLabel();
        updateTimerLabel();
    }
}