package haunted;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class HauntedRadioScreen extends JPanel implements ActionListener {
    private int channel = 0;
    private Timer timer;
    private Random random = new Random();
    private boolean showStatic = true;
    private Radio radio;

    public HauntedRadioScreen() {
        radio = new Radio();

        timer = new Timer(200, this); // refresh
        timer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Knob to change channel
                if (e.getX() > 600 && e.getX() < 650 && e.getY() > 400 && e.getY() < 450) {
                    channel = nextChannel();
                    showStatic = true;
                    repaint();
                }
            }
        });
    }

    private int nextChannel() {
        if (HauntedRadio.isSecretUnlocked() && channel == 5) {
            return 666;
        }
        if (channel == 666) {
            return 0;
        }
        return (channel + 1) % 6; // 0–5
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Background
        g.setColor(new Color(20, 20, 40));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Title
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        g.setColor(new Color(255, 230, 180));
        g.drawString("HAUNTED RADIO", 240, 80);

        // Retro Radio Body
        g.setColor(new Color(80, 30, 30));
        g.fillRoundRect(200, 150, 400, 300, 30, 30);

        // Antennas
        g.setColor(Color.GRAY);
        g.drawLine(300, 150, 250, 50);
        g.drawLine(500, 150, 550, 50);

        // Speaker Grill
        g.setColor(Color.BLACK);
        for (int i = 0; i < 10; i++) {
            g.fillRect(220 + i * 30, 180, 20, 100);
        }

        // Display Screen
        g.setColor(Color.DARK_GRAY);
        g.fillRect(320, 300, 160, 60);
        g.setColor(Color.GREEN);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("CH: " + channel, 340, 340);

        // Static Effect
        if (showStatic) {
            for (int i = 0; i < 300; i++) {
                int x = 320 + random.nextInt(160);
                int y = 300 + random.nextInt(60);
                g.setColor(new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256)));
                g.drawLine(x, y, x, y);
            }
        } else {
            // Broadcast
            String broadcastText = radio.tune(channel);
            String glitched = GlitchEffect.apply(broadcastText);

            int y = 400;
            g.setColor(Color.WHITE);
            for (String line : glitched.split("\n")) {
                g.drawString(line, 100, y);
                y += 20;
            }
        }

        // Knob
        g.setColor(Color.LIGHT_GRAY);
        g.fillOval(600, 400, 50, 50);
        g.setColor(Color.BLACK);
        g.drawString("Tune", 605, 430);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        showStatic = false;
        repaint();
    }
}
