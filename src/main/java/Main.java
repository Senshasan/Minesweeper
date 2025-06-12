import javax.swing.SwingUtilities;

public class Main {
    private static void startGame() {
        MinesweeperUI game = new MinesweeperUI();
        game.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::startGame);
    }
}