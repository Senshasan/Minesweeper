import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Tile extends JButton {
    private int row;
    private int col;
    private boolean isMine = false;
    private boolean isRevealed = false;
    private boolean isFlagged = false;
    private int adjacentMines = 0;
    private MinesweeperLogic game;

    public Tile(int row, int col, MinesweeperLogic game) {
        this.row = row;
        this.col = col;
        this.game = game;
        setPreferredSize(new Dimension(25, 25));
        setMargin(new Insets(0,0,0,0));
        setFont(new Font("Monospaced", Font.BOLD, 14));
        setFocusPainted(false);
        setFocusable(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (game.isGameOver()) return;
                if (SwingUtilities.isRightMouseButton(e)) {
                    toggleFlag();
                } else if (SwingUtilities.isLeftMouseButton(e)) {
                    if (isFlagged) return;
                    game.handleTileClick(row, col);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                game.setHoveredTile(Tile.this);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                game.setHoveredTile(null);
            }
        });
    }

    public void reveal() {
        if (isRevealed) return;
        isRevealed = true;
        setEnabled(true);

        if (isMine) {
            setText("💣");
            setBackground(Color.RED);
        } else if (adjacentMines > 0) {
            setText(Integer.toString(adjacentMines));
            setForeground(getColorForNumber(adjacentMines));
        } else {
            setText("");
            setBackground(Color.WHITE);
        }
    }

    public void toggleFlag() {
        if (isRevealed) return;
        if (!isFlagged && game.getFlagsPlaced() >= game.getMines()) {
            return;
        }

        isFlagged = !isFlagged;
        if (isFlagged) {
            setText("🚩");
            game.incrementFlagsPlaced();
        } else {
            setText("");
            game.decrementFlagsPlaced();
        }
        game.updateStatusLabel();
    }

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

    // Getters and setters
    public boolean isMine() { return isMine; }
    public void setMine(boolean mine) { isMine = mine; }
    public boolean isRevealed() { return isRevealed; }
    public boolean isFlagged() { return isFlagged; }
    public int getAdjacentMines() { return adjacentMines; }
    public void setAdjacentMines(int adjacentMines) { this.adjacentMines = adjacentMines; }
    public int getRow() { return row; }
    public int getCol() { return col; }
}