import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MinesweeperLogic {
    private static final int ROWS = 16;
    private static final int COLS = 30;
    private static final int INITIAL_TIME = 600; // 10 minutes in seconds
    private static final int POWERUP_COST_SINGLE = 50;
    private static final int POWERUP_COST_GRID = 450;
    private static final int POWERUP_COST_COLUMN = 800;
    private int mines = 0;
    private int safelyRevealedMines = 0;
    private final Tile[][] board;
    private boolean firstClick = true;
    private boolean gameOver = false;
    private int tilesRevealed = 0;
    private int flagsPlaced = 0;
    private int score = 0;
    private Tile hoveredTile = null;
    private Timer gameTimer;
    private int remainingTime = INITIAL_TIME;
    private int elapsedTime = 0;
    private final String[] options = { "50 Minen", "75 Minen", "99 Minen" };
    private MinesweeperUI ui;

    public MinesweeperLogic(MinesweeperUI ui) {
        this.ui = ui;
        this.board = new Tile[ROWS][COLS];
        initializeBoard();
        selectDifficulty();
    }

    private void initializeBoard() {
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = new Tile(r, c, this);
            }
        }
    }

    private void selectDifficulty() {
        int response = JOptionPane.showOptionDialog(null, null, null, 0, 0, null, options, null);
        if (response == 0) {
            this.mines = 50;
        } else if (response == 1) {
            this.mines = 75;
        } else if (response == 2) {
            this.mines = 99;
        }
    }

    public void handleTileClick(int row, int col) {
        if (firstClick) {
            placeMines(row, col);
            firstClick = false;
            startTimer();
        }
        if (!board[row][col].isRevealed()) {
            revealTile(row, col);
        }
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
        for (int i = 0; i < this.mines; i++) {
            Point p = positions.get(i);
            board[p.x][p.y].setMine(true);
        }
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c].setAdjacentMines(countAdjacentMines(r, c));
            }
        }
    }

    private int countAdjacentMines(int row, int col) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr, nc = col + dc;
                if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;
                if (board[nr][nc].isMine()) count++;
            }
        }
        return count;
    }

    private void revealTile(int row, int col) {
        Tile tile = board[row][col];
        if (tile.isRevealed() || tile.isFlagged()) return;
        tile.reveal();
        addScore(10);
        ui.updateStatusLabel();
        tilesRevealed++;

        if (tile.isMine()) {
            gameOver(false);
            return;
        }

        if (tile.getAdjacentMines() == 0) {
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

        if (tilesRevealed == ROWS * COLS - this.mines) {
            gameOver(true);
        }
    }

    private void gameOver(boolean win) {
        gameOver = true;
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                Tile t = board[r][c];
                if (t.isMine()) t.setText("💣");
                t.setEnabled(false);
            }
        }

        if (gameTimer != null) {
            gameTimer.stop();
        }

        String message;
        if (win) {
            message = "Gewonnen! \uD83E\uDD73 in " + elapsedTime + " Sekunden!";
        } else {
            message = "Verloren.... \uD83D\uDE2D \uD83D\uDC80 in " + elapsedTime + " Sekunden!";
        }

        int choice = JOptionPane.showConfirmDialog(null, "Nochmal spielen?", message, JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            ui.restartGame();
        } else if (choice == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
    }

    private void startTimer() {
        gameTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                remainingTime--;
                elapsedTime++;
                ui.updateTimerLabel();
                if (remainingTime <= 0) {
                    gameTimer.stop();
                    gameOver(false);
                }
            }
        });
        gameTimer.start();
    }

    public void handlePowerup(int powerupType) {
        if (gameOver || hoveredTile == null) return;

        switch (powerupType) {
            case 1 -> safeRevealTile(hoveredTile.getRow(), hoveredTile.getCol());
            case 2 -> safeRevealAdjacentTiles(hoveredTile.getRow(), hoveredTile.getCol());
            case 3 -> safeRevealVerticalColumn(hoveredTile.getRow(), hoveredTile.getCol());
        }
    }

    private void safeRevealTile(int row, int col) {
        if (score < POWERUP_COST_SINGLE) return;

        Tile tile = board[row][col];
        if (tile.isRevealed() || tile.isFlagged()) return;

        tile.reveal();
        subtractScore(POWERUP_COST_SINGLE);
        tilesRevealed++;

        if (tile.isMine()) {
            safelyRevealedMines++;
        }

        updateStatusLabel();
        checkWinCondition();
    }

    private void safeRevealAdjacentTiles(int row, int col) {
        if (score < POWERUP_COST_GRID) return;

        subtractScore(POWERUP_COST_GRID);

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = row + dr, nc = col + dc;
                if (nr < 0 || nr >= ROWS || nc < 0 || nc >= COLS) continue;

                Tile tile = board[nr][nc];
                if (!tile.isRevealed() && !tile.isFlagged()) {
                    tile.reveal();
                    tilesRevealed++;
                    if (tile.isMine()) {
                        safelyRevealedMines++;
                    }
                }
            }
        }

        updateStatusLabel();
        checkWinCondition();
    }

    private void safeRevealVerticalColumn(int row, int col) {
        if (score < POWERUP_COST_COLUMN) return;

        subtractScore(POWERUP_COST_COLUMN);

        for (int r = 0; r < ROWS; r++) {
            Tile tile = board[r][col];
            if (!tile.isRevealed() && !tile.isFlagged()) {
                tile.reveal();
                tilesRevealed++;
                if (tile.isMine()) {
                    safelyRevealedMines++;
                }
            }
        }

        updateStatusLabel();
        checkWinCondition();
    }

    private void checkWinCondition() {
        if (tilesRevealed == ROWS * COLS - mines + safelyRevealedMines) {
            gameOver(true);
        }
    }

    // Getters and setters
    public boolean isGameOver() { return gameOver; }
    public int getMines() { return mines; }
    public int getFlagsPlaced() { return flagsPlaced; }
    public void incrementFlagsPlaced() { flagsPlaced++; }
    public void decrementFlagsPlaced() { flagsPlaced--; }
    public void setHoveredTile(Tile tile) { this.hoveredTile = tile; }
    public Tile[][] getBoard() { return board; }
    public int getScore() { return score; }
    public void addScore(int points) { this.score += points; }
    public void subtractScore(int points) { this.score -= points; }
    public int getElapsedTime() { return elapsedTime; }
    public int getRemainingTime() { return remainingTime; }
    public void updateStatusLabel() { ui.updateStatusLabel(); }
    public int getSafelyRevealedMines() { return safelyRevealedMines; }
}