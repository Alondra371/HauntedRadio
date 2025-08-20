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

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Background (dark stone look)
        g2.setColor(new Color(30, 28, 30));
        g2.fillRect(0, 0, getWidth(), getHeight());

        // === 1. Wooden radio body ===
        int bodyX = 150, bodyY = 150, bodyW = 600, bodyH = 300;
        g2.setColor(new Color(110, 75, 47)); // wood brown
        g2.fillRoundRect(bodyX, bodyY, bodyW, bodyH, 20, 20);
        g2.setColor(new Color(63, 42, 24));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(bodyX, bodyY, bodyW, bodyH, 20, 20);

        // === 2. Speaker grille (left side, upper half) ===
        int grilleX = bodyX + 20, grilleY = bodyY + 30, grilleW = 300, grilleH = 120;
        g2.setColor(new Color(96, 66, 37)); // dark inset
        g2.fillRect(grilleX, grilleY, grilleW, grilleH);

        g2.setColor(new Color(217, 199, 166)); // beige slats
        for (int y = grilleY + 10; y < grilleY + grilleH; y += 12) {
            g2.fillRect(grilleX + 5, y, grilleW - 10, 6);
        }

        // === 3. Frequency dial window (right side, upper half) ===
        int dialX = bodyX + 340, dialY = bodyY + 30, dialW = 220, dialH = 120;
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(dialX, dialY, dialW, dialH, 10, 10);
        g2.setColor(new Color(183, 166, 129));
        g2.drawRoundRect(dialX, dialY, dialW, dialH, 10, 10);

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(Color.GREEN);
        g2.drawString("FM   AM   SW", dialX + 15, dialY + 20);

        g2.setColor(Color.RED);
        g2.drawString("88   92   96   100   104   108", dialX + 15, dialY + 50);

        g2.setColor(Color.YELLOW);
        g2.drawString("530  700  900  1000  1200  1600", dialX + 15, dialY + 80);

        // === 4. Control section (bottom strip) ===
        int controlY = bodyY + 180;
        g2.setColor(new Color(70, 50, 30));
        g2.fillRect(bodyX + 20, controlY, bodyW - 40, 80);

        // Small knobs
        g2.setColor(new Color(30, 15, 10));
        g2.fillOval(bodyX + 40, controlY + 10, 40, 40);
        g2.fillOval(bodyX + 100, controlY + 10, 40, 40);
        g2.setColor(new Color(200, 200, 200));
        g2.drawOval(bodyX + 40, controlY + 10, 40, 40);
        g2.drawOval(bodyX + 100, controlY + 10, 40, 40);

        // Large tuning knob (right)
        int knobSize = 70;
        int knobX = bodyX + bodyW - knobSize - 30;
        int knobY = controlY + 5;
        g2.setColor(new Color(30, 15, 10));
        g2.fillOval(knobX, knobY, knobSize, knobSize);
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(knobX, knobY, knobSize, knobSize);
        g2.drawLine(knobX + knobSize/2, knobY + knobSize/2, knobX + knobSize/2, knobY + 15);

        // === 5. Antenna ===
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(bodyX + 30, bodyY, bodyX - 80, bodyY - 100);
    }
}
