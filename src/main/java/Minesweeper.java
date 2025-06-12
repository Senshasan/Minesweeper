import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Minesweeper extends JFrame {
    private static final int ROWS = 16;
    private static final int COLS = 30;
    int MINES = 0;
    private int safelyRevealedMines = 0;

    private void Schwierigkeit () {
        int response = JOptionPane.showOptionDialog(null, null, null, 0, 0, null, options, null);
        if (response == 0) {
            this.MINES = 50;
        }
        if (response == 1) {
            this.MINES = 75;
        }
        if (response == 2) {
            this.MINES = 99;
        }
    }

    private final Tile[][] board = new Tile[ROWS][COLS];
    private boolean firstClick = true;
    private boolean gameOver = false;
    private int tilesRevealed = 0;
    private int flagsPlaced = 0;
    private int Score = 0;
    private final JLabel statusLabel;
    private final JPanel boardPanel;
    private final JLabel timerLabel;
    private Tile hoveredTile = null;
    private Timer gameTimer;
    private int elapsedTime = 0; //in seconds
    String[] options = { "50 Minen", "75 Minen", "99 Minen" };

    public Minesweeper() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Title panel from version 2
        JPanel titlePanel = new JPanel();
        ImageIcon titleImage = new ImageIcon("C:\\Users\\user\\Documents\\GitHub\\Minesweeper\\out\\production\\Minesweeper\\Title1.png");
        JLabel imageLabel = new JLabel(titleImage);
        titlePanel.setBackground(Color.WHITE);
        titlePanel.add(imageLabel);
        add(titlePanel, BorderLayout.NORTH);
        setVisible(true);

        // Status label for score from version 2
        JPanel ScorePanel = new JPanel();
        statusLabel = new JLabel("Score: " + Score + " | Muselmänner: " + (MINES - flagsPlaced));
        statusLabel.setPreferredSize(new Dimension(200, 50));
        ScorePanel.setBackground(Color.WHITE);
        ScorePanel.add(statusLabel);
        add(ScorePanel, BorderLayout.SOUTH);

        //timer Label
        JPanel TimerPanel = new JPanel ();
        timerLabel = new JLabel("Time: " + elapsedTime + "s");
        timerLabel.setPreferredSize(new Dimension(100, 100));
        TimerPanel.setBackground(Color.WHITE);
        TimerPanel.add(timerLabel);
        add (TimerPanel, BorderLayout.LINE_END);


        //timer
        //score
        //ability tracker (points) + power-up UI (secondary panel, vertical label/sidebar??)

        boardPanel = new JPanel(new GridLayout(ROWS, COLS));
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile tile = new Tile(r, c);
                board[r][c] = tile;
                boardPanel.add(tile);
            }
        }
        add(boardPanel, BorderLayout.CENTER);
        Schwierigkeit();

        // Side panels from version 2
        JPanel powerupPanel = new JPanel();
        ImageIcon powerupImage = new ImageIcon("C:\\Users\\user\\Documents\\GitHub\\Minesweeper\\Powerups.png");
        JLabel imageLabel2 = new JLabel(powerupImage);
        powerupPanel.setBackground(Color.WHITE);
        powerupPanel.add(imageLabel2);
        add(powerupPanel, BorderLayout.LINE_START);
        setVisible(true);

        //Mimi, Keyboard Press Scanner.
        //Relatively easy to add new Keybinds for more features like an Options Menu later on
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (gameOver || hoveredTile == null) return;
                //Mimi, Powerup1 Keybind1
                if (e.getKeyCode() == KeyEvent.VK_1) {
                    if (hoveredTile.isFlagged) return;
                    if (firstClick) {
                        placeMines(hoveredTile.row, hoveredTile.col);
                        firstClick = false;
                    }
                    if (!hoveredTile.isRevealed) {
                        safeRevealTile(hoveredTile.row, hoveredTile.col);
                    }
                    requestFocus();
                    //Mimi, Powerup2 Keybind2
                } else if (e.getKeyCode() == KeyEvent.VK_2) {
                    if (hoveredTile.isFlagged) return;
                    if (firstClick) {
                        placeMines(hoveredTile.row, hoveredTile.col);
                        firstClick = false;
                    }
                    if (!hoveredTile.isRevealed) {
                        safeRevealTileX2(hoveredTile.row, hoveredTile.col);
                    }
                    safeRevealAdjacentTiles(hoveredTile.row, hoveredTile.col);
                    requestFocus();
                } else if (e.getKeyCode() == KeyEvent.VK_3) {
                    if (hoveredTile.isFlagged) return;
                    if (firstClick) {
                        placeMines(hoveredTile.row, hoveredTile.col);
                        firstClick = false;
                    }
                    if (!hoveredTile.isRevealed) {
                        safeRevealTileX2(hoveredTile.row, hoveredTile.col);
                    }
                    safeRevealVerticalColumn(hoveredTile.row, hoveredTile.col);
                    requestFocus();

                }
            }
        });

        setFocusable(true);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        requestFocus();
    }
    private void startTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                elapsedTime++;
                timerLabel.setText("Time: " + elapsedTime + "s");
            }
        });
        gameTimer.start();
    }

    public int getScore() {
        return Score;
    }

    private void addScore(int points) {
        this.Score += points;
    }
    private void subtractScore (int points) {
        this.Score-= points;
    }

    private void placeMines(int safeRow, int safeCol) {
        List<Point> positions = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (Math.abs(r - safeRow) <= 1 && Math.abs(c - safeCol) <= 1) continue;
                positions.add(new Point(r, c));
            }
        }
        Collections.shuffle(positions);
        for (int i = 0; i < this.MINES; i++) {
            Point p = positions.get(i);
            board[p.x][p.y].isMine = true;
        }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c].adjacentMines = countAdjacentMines(r, c);
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr, nc = col + dc;
                if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;
                if (board[nr][nc].isMine) count++;
            }
        }
        return count;
    }

    private void revealTile(int row, int col) {
        Tile tile = board[row][col];
        if (tile.isRevealed || tile.isFlagged) return;
        tile.reveal();
        addScore(10);
        statusLabel.setText("Score: " + Score + " | Minen " + (MINES - flagsPlaced));
        tilesRevealed++;
        if (tile.isMine) {
            gameOver(false);
            return;
        }
        if (tile.adjacentMines == 0) {
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = row + dr, nc = col + dc;
                    if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;
                    if (!(dr == 0 && dc == 0)) {
                        revealTile(nr, nc);
                    }
                }
            }
        }
        if (tilesRevealed == ROWS * COLS - this.MINES) {
            gameOver(true);
        }
    }

    //Mimi, Powerup1: Reveal 1 Tile safely
    private void safeRevealTile(int row, int col) {
        Tile tile = board[row][col];
        if (Score < 50) {
            return;
        }

        if (tile.isRevealed || tile.isFlagged) return;
        tile.reveal();
        subtractScore(50);
        statusLabel.setText("Score: " + Score + "| Minen: " + (MINES - flagsPlaced));
        tilesRevealed++;

        if (tile.isMine) {
            safelyRevealedMines++;
            return;
        }

        if (tilesRevealed == ROWS * COLS - this.MINES + safelyRevealedMines) {
            gameOver(true);
        }
    }

    private void safeRevealTileX2(int row, int col) {
        Tile tile = board[row][col];

        if (Score < 450) {
            return;
        }
        if (tile.isRevealed || tile.isFlagged) return;
        tile.reveal();
        tilesRevealed++;

        if (tile.isMine) {
            safelyRevealedMines++;
            return;
        }

        if (tilesRevealed == ROWS * COLS - this.MINES + safelyRevealedMines) {
            gameOver(true);
        }
    }

    private void safeRevealTileX3(int row, int col) {
        Tile tile = board[row][col];

        if (Score < 800) {
            return;
        }
        if (tile.isRevealed || tile.isFlagged) return;
        tile.reveal();
        tilesRevealed++;

        if (tile.isMine) {
            safelyRevealedMines++;
            return;
        }

        if (tilesRevealed == ROWS * COLS - this.MINES + safelyRevealedMines) {
            gameOver(true);
        }
    }

    //Mimi, Powerup2: Reveal a 3x3 Space safely
    private void safeRevealAdjacentTiles(int row, int col) {
        if (Score < 450) {
            return;
        }else {
            subtractScore(450);

            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    int nr = row + dr, nc = col + dc;
                    if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;
                    if (!(dr == 0 && dc == 0)) {
                        safeRevealTileX2(nr, nc);
                        statusLabel.setText("Score: " + Score + " | Minen " + (MINES - flagsPlaced));
                    }
                }
            }
        }

        if (tilesRevealed == ROWS * COLS - this.MINES + safelyRevealedMines) {
            gameOver(true);
        }
    }

    //Mimi, Powerup3: reveal vertical row
    private void safeRevealVerticalColumn(int row, int col) {
        if (Score < 800) {
            return;
        } else {
            subtractScore(0);

            for (int r = 0; r < ROWS; r++) {
                if (r >= 0 && r < ROWS && col >= 0 && col < COLS) {
                    safeRevealTileX3(r, col);
                }
            }
            statusLabel.setText("Score: " + Score + " | Minen: " + (MINES - flagsPlaced));
        }

        if (tilesRevealed == ROWS * COLS - this.MINES + safelyRevealedMines) {
            gameOver(true);
        }
    }

    private void gameOver(boolean win) {
        gameOver = true;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile t = board[r][c];
                if (t.isMine) t.setText("💣");
                t.setEnabled(false);

                if (gameTimer != null) {
                    gameTimer.stop();
                }
            }
        }
        if (win) {
            statusLabel.setText("Gewonnen! \uD83E\uDD73");
            int choice = JOptionPane.showConfirmDialog(null, "Nochmal spielen?",
                    "Gewonnen! \uD83E\uDD73 in in " +elapsedTime+ "Sekunden!" +elapsedTime+ "Sekunden!", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(Minesweeper::new);
            } else if (choice == JOptionPane.NO_OPTION) {
            }
        } else {
            statusLabel.setText("Verloren.... \uD83D\uDE2D \uD83D\uDC80");
            int choice = JOptionPane.showConfirmDialog(null, "Nochmal spielen?",
                    "Verloren.... \uD83D\uDE2D \uD83D\uDC80 in " +elapsedTime+ "Sekunden!", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(Minesweeper::new);
            } else if (choice == JOptionPane.NO_OPTION) {
            }
        }
    }

    private class Tile extends JButton {
        int row;
        int col;
        boolean isMine = false;
        boolean isRevealed = false;
        boolean isFlagged = false;
        int adjacentMines = 0;

        public Tile(int row, int col) {
            this.row = row;
            this.col = col;
            setPreferredSize(new Dimension(25, 25));
            setMargin(new Insets(0,0,0,0));
            setFont(new Font("Monospaced", Font.BOLD, 14));
            setFocusPainted(false);
            setFocusable(false);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (gameOver) return;
                    if (SwingUtilities.isRightMouseButton(e)) {
                        toggleFlag();
                    } else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (isFlagged) return;
                        if (firstClick) {
                            placeMines(row, col);
                            firstClick = false;
                        }
                        if (!isRevealed) {
                            revealTile(row, col);
                            if (firstClick) {
                                placeMines(row,col);
                                startTimer();
                                firstClick = false;
                            }
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    hoveredTile = Tile.this;
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredTile = null;
                }
            });
        }

        public void reveal() {
            if (isRevealed) return;
            isRevealed = true;
            //setEnabled should be set to false but for the life of me I cannot get it work displaying the right colors
            //while its set to false.
            //being able to interact with revealed tiles is bad but being blind to color is worse so well make do for now
            //Lmao this Dude is fucking Colourblind no way

            setEnabled(true);
            if (isMine) {
                setText("💣");
                setBackground(Color.RED);
            } else if (adjacentMines > 0) {
                setText(Integer.toString(adjacentMines));
                setForeground(getColorForNumber(adjacentMines));
                //setDisabledTextColor(Color.RED);        debugging

            } else {
                setText("");
                setBackground(Color.WHITE);
            }
        }

        public void toggleFlag() {
            if (isRevealed) return;
            if (!isFlagged && flagsPlaced >= MINES){
                //QOL: status label/panel swap or whatever display: too many flags/more flags than mines
                return;
            }

            isFlagged = !isFlagged;

            if (isFlagged) {
                setText("🚩");
                flagsPlaced += 1;
            }
            else {
                setText("");
                flagsPlaced -= 1;
            }


            statusLabel.setText("Score: " + Score + " | Minen: " + (MINES - flagsPlaced));
        }

        //red gradients to show increasing danger, can be changed to rbg or whatever, also maybe HexCode colors ?
        private Color getColorForNumber(int n) {
            return switch (n) {
                case 1 -> new Color(255, 0, 0);
                case 2 -> new Color(224, 0, 0);
                case 3 -> new Color(192, 0, 0);
                case 4 -> new Color(160, 0, 0);
                case 5 -> new Color(128, 0, 0);
                case 6 -> new Color(96, 0, 0);
                case 7 -> new Color(64, 0, 0);
                case 8 -> new Color(32, 0, 0);
                default -> Color.BLACK;
            };
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Minesweeper::new);
    }
}

//should amount to the same thing, creating "Minesweeper" in the Swing Thread without externally calling on it in main
//this might be better if we want to add more logic to the run method
//public static void main(String[] args) {
//    SwingUtilities.invokeLater() = (new Runnable() {
//        public void run() {
//            new Minesweeper();
//        }
//    });
//}

//maybe even a complete implementation of the Runnable on class level would make sense
//instead of created a helper object in the Runnable we could implement Runnable from the start and separate UI and
//game features to free the Thread and keep the functionalities safe by separation
