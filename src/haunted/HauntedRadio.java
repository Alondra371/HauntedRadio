package haunted;

import javax.swing.*;
import java.awt.*;

/**
 * Entry point that shows the haunted splash screen first.
 * When the user presses SPACE, the splash closes
 * and the interactive HauntedRadio (with working knobs) opens.
 */
public class HauntedRadio {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            final JFrame win = new JFrame("Ghost Frequency — Splash");
            win.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // Holder to allow stopIntro inside lambda
            final HauntedSplashScreen[] splashHolder = new HauntedSplashScreen[1];

            // Create splash with callback
            splashHolder[0] = new HauntedSplashScreen(() -> {
                // Stop intro audio if needed
                splashHolder[0].stopIntro();
                // Close splash window
                win.dispose();

                // --- Launch interactive haunted radio window ---
                JFrame f = new JFrame("Haunted Radio — Interactive");
                f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                f.setMinimumSize(new Dimension(960, 640));
                f.setLocationRelativeTo(null);

                RadioPanel panel = new RadioPanel();
                ChannelManagerSwing manager = new ChannelManagerSwing(null);

                // Wire knobs/switches
                panel.wireDefaults(manager);

                // Apply initial state
                if (manager.getPlayer() != null) {
                    manager.getPlayer().setVolume(panel.getCurrentVolume0to1());
                }
                if (panel.isPowerOn()) {
                    manager.playChannel(panel.getCurrentChannel());
                }

                f.setContentPane(panel);
                f.setVisible(true);
            });

            win.setContentPane(splashHolder[0]);
            win.pack();
            win.setLocationRelativeTo(null);
            win.setVisible(true);
        });
    }
}
