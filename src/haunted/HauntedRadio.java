package haunted;

import javax.swing.*;

public class HauntedRadio {
    private static boolean secretUnlocked = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Haunted Radio");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);

            // Splash screen with callback
            HauntedSplashScreen splash = new HauntedSplashScreen(() -> {
                frame.dispose();
                launchRadioGame();
            });

            frame.add(splash);
            frame.setVisible(true);

            splash.requestFocusInWindow();
            new Timer(1000 / 60, e -> splash.repaint()).start(); // repaint loop
        });
    }

    public static void launchRadioGame() {
        HauntedRadioScreen radioScreen = new HauntedRadioScreen();
        radioScreen.setVisible(true);
    }

    public static void unlockSecret() {
        secretUnlocked = true;
    }

    public static boolean isSecretUnlocked() {
        return secretUnlocked;
    }
}
