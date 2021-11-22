import javax.swing.*;

public class GameFrame extends JFrame {
    GamePanel gamePanel = new GamePanel();

    GameFrame() {
        this.setTitle("Mühle für zwei Spieler");
        this.add(gamePanel);
        this.setSize(812, 835);
        this.setVisible(true);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
