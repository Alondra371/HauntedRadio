package haunted;


import javax.swing.*;


/**
 * Small bootstrap that shows the splash first and then swaps to the radio window.
 * Using SwingUtilities.invokeLater ensures we touch Swing on the EDT (Event Dispatch Thread).
 */
public class HauntedLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame win = new JFrame("Ghost Frequency — Splash");
            win.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);


// When the user presses SPACE/ENTER on the splash, open the radio frame
            HauntedSplashScreen splash = new HauntedSplashScreen(() -> {
// Dispose the splash frame and open the interactive radio UI
                SwingUtilities.invokeLater(() -> {
                    win.dispose();
                    new HauntedRadioScreen().setVisible(true);
                });
            });


            win.setContentPane(splash);
            win.pack();
            win.setLocationRelativeTo(null); // center on screen
            win.setVisible(true);
        });
    }
}