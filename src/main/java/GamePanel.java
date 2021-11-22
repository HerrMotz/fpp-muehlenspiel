import javax.swing.*;
import java.awt.*;

public class GamePanel extends Panel {
    public void paint(Graphics g) {
        for (int i = 0; i < 3; i++) {
            int j = i*100;
            g.drawLine(100 + j, 100 + j, 700 - j, 100 + j);
            g.drawLine(100 + j, 700 - j, 700 - j, 700 - j);

            g.drawLine(100 + j, 100 + j, 100 + j, 700 - j);
            g.drawLine(700 - j, 100 + j, 700 - j, 700 - j);
        }

        g.drawLine(400, 100, 400, 300);
        g.drawLine(100, 400, 300, 400);

        g.drawLine(400, 700, 400, 500);
        g.drawLine(700, 400, 500, 400);
    }


}
