package haunted;

import javax.swing.*;
import java.awt.*;

public class GhostUnlockSplash extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // Ghostly Message
        g.setColor(Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        g.drawString("☠ CHANNEL 666 UNLOCKED ☠", 140, 100);

        // Big ghost figure
        g.setColor(Color.WHITE);
        g.fillOval(320, 150, 160, 160); // Head
        g.fillRect(340, 300, 120, 150); // Body
        g.fillOval(300, 400, 60, 80);
        g.fillOval(460, 400, 60, 80);

        // Eyes & mouth
        g.setColor(Color.BLACK);
        g.fillOval(360, 190, 25, 40);
        g.fillOval(415, 190, 25, 40);
        g.fillOval(380, 250, 50, 30);

        // Text at bottom
        g.setColor(new Color(0, 255, 100));
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("The radio shrieks with unholy static...", 200, 500);
    }

    public static void showSplash(JFrame parent) {
        JFrame splash = new JFrame("Ghostly Unlock");
        splash.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        splash.setSize(800, 600);
        splash.setLocationRelativeTo(null);
        splash.add(new GhostUnlockSplash());
        splash.setVisible(true);

        // Close splash after 4 seconds, return to radio
        new javax.swing.Timer(4000, e -> {
            splash.dispose();
            parent.setVisible(true); // re-show radio game
        }).start();
    }
}
