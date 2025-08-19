package haunted;

import javax.swing.*;
import java.awt.*;

public class HauntedSplashScreen extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(new Color(10, 10, 40)); 
        g.fillRect(0, 0, getWidth(), getHeight());

        // Title Banner
        g.setColor(Color.RED);
        g.fillRect(200, 100, 400, 80);
        g.setColor(Color.BLACK);
        g.drawRect(200, 100, 400, 80);
        g.setFont(new Font("Monospaced", Font.BOLD, 32));
        g.setColor(new Color(255, 230, 180));
        g.drawString("HAUNTED RADIO", 230, 150);

        // Ghost
        g.setColor(Color.WHITE);
        g.fillOval(100, 200, 80, 80); 
        g.fillRect(110, 280, 60, 60); 
        g.setColor(Color.BLACK);
        g.fillOval(120, 220, 15, 25); 
        g.fillOval(145, 220, 15, 25);

        // Menu Info
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.setColor(new Color(255, 230, 180));
        g.drawString("Click the knob to tune channels...", 200, 250);
    }
}
