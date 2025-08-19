package haunted;

import javax.swing.*;

public class HauntedRadio {
    private static boolean secretUnlocked = false;

    public static void main(String[] args) {
        // Show Swing Splash First
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Haunted Radio");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.add(new HauntedSplashScreen());
            frame.setVisible(true);

            // Auto-close after 4 seconds and start HauntedRadioScreen
            new javax.swing.Timer(4000, e -> {
                frame.dispose();
                launchRadioGame();
            }).start();
        });
    }

    public static void launchRadioGame() {
        JFrame gameFrame = new JFrame("Haunted Radio");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(800, 600);
        gameFrame.setLocationRelativeTo(null);

        HauntedRadioScreen panel = new HauntedRadioScreen();
        gameFrame.add(panel);
        gameFrame.setVisible(true);
    }

    public static void unlockSecret() {
        secretUnlocked = true;
    }

    public static boolean isSecretUnlocked() {
        return secretUnlocked;
    }
}
