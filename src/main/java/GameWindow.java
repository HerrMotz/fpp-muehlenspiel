import javax.swing.*;
import java.awt.*;

public class GameWindow extends Panel {
    public void paint(Graphics g) {
        int height = getSize().height;
        int width = getSize().width;

        for (int i = 0; i < 300; i+=100) {
            g.drawLine(100 + i, 100 + i, 700 - i, 100 + i);
            g.drawLine(100 + i, 700 - i, 700 - i, 700 - i);

            g.drawLine(100 + i, 100 + i, 100 + i, 700 - i);
            g.drawLine(700 - i, 100 + i, 700 - i, 700 - i);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mühle für zwei Spieler");
        frame.add(new GameWindow());
        frame.setSize(812, 835);
        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
