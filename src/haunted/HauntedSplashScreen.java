package haunted;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.GeneralPath;
import java.util.Random;

public class HauntedSplashScreen extends JPanel {
    private Random random = new Random();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- Background: VHS grainy purple ---
        g.setColor(new Color(20, 0, 40));
        g.fillRect(0, 0, getWidth(), getHeight());

        // VHS static speckles
        for (int i = 0; i < 300; i++) {
            int x = random.nextInt(getWidth());
            int y = random.nextInt(getHeight());
            g.setColor(new Color(0, random.nextInt(50), 0, 100));
            g.fillRect(x, y, 2, 2);
        }

        // --- Ghosts in background ---
        drawGhost(g2d, 150, 250, 1.0);     // left
        drawGhost(g2d, 650, 250, 1.0);     // right
        drawGhost(g2d, getWidth() / 2, 320, 1.2); // bottom center
        drawGhost(g2d, 250, 100, 0.8);     // top left
        drawGhost(g2d, 550, 100, 0.8);     // top right

        // --- Title: slimey glowing text (drawn AFTER ghosts, so it's on top) ---
        String title = "GHOST FREQUENCY";
        g.setFont(new Font("Monospaced", Font.BOLD, 55)); // big font

        FontMetrics fm = g.getFontMetrics();
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        int titleY = 200;

        // Glow effect
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(0, 255, 100, 40));
            g.drawString(title, titleX - i, titleY - i);
        }

        // Main slime text
        g.setColor(new Color(0, 255, 0));
        g.drawString(title, titleX, titleY);


        // --- Prompt: flickering retro text ---
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.setColor(random.nextBoolean() ? new Color(0, 255, 0) : new Color(0, 200, 0));
        String prompt = "Press START to tune in...";
        int promptX = (getWidth() - g.getFontMetrics().stringWidth(prompt)) / 2;
        g.drawString(prompt, promptX, 420);
    }

    /** Helper: draw a cute ghost at (x,y) with size scale */
    private void drawGhost(Graphics2D g2d, int x, int y, double scale) {
        int w = (int) (100 * scale);
        int h = (int) (100 * scale);

        // Body with curves
        GeneralPath ghost = new GeneralPath();
        ghost.moveTo(x, y - h / 2);
        ghost.curveTo(x - w / 2, y - h / 3, x - w / 2, y + h / 3, x - w / 4, y + h / 2);
        ghost.curveTo(x, y + h, x + w / 4, y + h / 2, x + w / 2, y + h / 3);
        ghost.curveTo(x + w / 2, y - h / 3, x, y - h / 2, x, y - h / 2);
        ghost.closePath();

        g2d.setColor(Color.WHITE);
        g2d.fill(ghost);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(3));
        g2d.draw(ghost);

        // Eyes
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x - (int)(0.15 * w), y - (int)(0.2 * h), (int)(0.15 * w), (int)(0.25 * h));
        g2d.fillOval(x + (int)(0.05 * w), y - (int)(0.2 * h), (int)(0.15 * w), (int)(0.25 * h));

        // Eye highlights
        g2d.setColor(Color.WHITE);
        g2d.fillOval(x - (int)(0.1 * w), y - (int)(0.15 * h), (int)(0.05 * w), (int)(0.08 * h));
        g2d.fillOval(x + (int)(0.1 * w), y - (int)(0.15 * h), (int)(0.05 * w), (int)(0.08 * h));
    }

    // Quick test launcher
    public static void main(String[] args) {
        JFrame frame = new JFrame("Haunted Splash");
        HauntedSplashScreen panel = new HauntedSplashScreen();
        frame.add(panel);
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        new Timer(200, e -> panel.repaint()).start();
    }
}

