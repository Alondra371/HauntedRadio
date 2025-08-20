package haunted;

import javax.swing.*;
import java.awt.*;

public class HauntedRadioScreen extends JFrame {
    public HauntedRadioScreen() {
        setTitle("Haunted Radio (Look Only)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);

        add(new RadioPanel());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HauntedRadioScreen frame = new HauntedRadioScreen();
            frame.setVisible(true);
        });
    }
}

class RadioPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Smooth drawing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(new Color(25, 20, 20));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // 1. Wooden radio body
        g2.setPaint(new GradientPaint(0, 0,
                new Color(130, 80, 30), // top brown
                0, getHeight(),
                new Color(70, 40, 15))); // bottom brown
        g2.fillRoundRect(50, 50, getWidth() - 100, getHeight() - 100, 50, 50);

        // 2. Speaker grille
        int grilleX = 200, grilleY = 120, grilleW = 500, grilleH = 300;
        g2.setColor(new Color(40, 40, 40));
        g2.fillRoundRect(grilleX, grilleY, grilleW, grilleH, 30, 30);

        // Mesh dots
        g2.setColor(new Color(90, 90, 90));
        for (int x = grilleX + 10; x < grilleX + grilleW - 10; x += 15) {
            for (int y = grilleY + 10; y < grilleY + grilleH - 10; y += 15) {
                g2.fillOval(x, y, 5, 5);
            }
        }

        // 3. Frequency dial window
        int dialX = 200, dialY = 450, dialW = 500, dialH = 60;
        g2.setColor(Color.BLACK);
        g2.fillRect(dialX, dialY, dialW, dialH);

        g2.setColor(Color.GREEN);
        g2.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2.drawString("600   700   800   900   1000   1080", dialX + 20, dialY + 35);

        // Glass reflection effect
        g2.setPaint(new GradientPaint(dialX, dialY,
                new Color(255, 255, 255, 60),
                dialX, dialY + dialH,
                new Color(0, 0, 0, 0)));
        g2.fillRect(dialX, dialY, dialW, dialH);

        // 4. Knobs (static)
        g2.setColor(new Color(60, 60, 60));
        g2.fillOval(120, 500, 80, 80); // volume knob
        g2.fillOval(680, 500, 80, 80); // tuning knob

        g2.setColor(Color.LIGHT_GRAY);
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(120, 500, 80, 80);
        g2.drawOval(680, 500, 80, 80);

        // Knob indicators
        g2.drawLine(160, 540, 160, 510); // left knob tick
        g2.drawLine(720, 540, 750, 540); // right knob tick

        // 5. Power button (static)
        g2.setColor(new Color(180, 30, 30));
        g2.fillRoundRect(400, 540, 100, 40, 15, 15);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("POWER", 425, 565);
    }
}
