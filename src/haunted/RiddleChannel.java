package haunted;

import javax.swing.*;

public class RiddleChannel extends Channel {
    private boolean solved = false;

    public RiddleChannel(int number, String name) {
        super(number, name);
    }

    @Override
    public String broadcast() {
        if (!solved) {
            String answer = JOptionPane.showInputDialog(
                null,
                "A ghostly voice whispers:\n" +
                "\"I speak without a mouth and hear without ears.\n" +
                "I have no body, but I come alive with wind.\n" +
                "What am I?\"",
                "Ghostly Riddle",
                JOptionPane.QUESTION_MESSAGE
            );

            if (answer != null && answer.equalsIgnoreCase("echo")) {
                solved = true;
                HauntedRadio.unlockSecret();

                // Show ghost splash
                SwingUtilities.invokeLater(() -> {
                    JFrame main = (JFrame) SwingUtilities.getWindowAncestor(new JPanel());
                    if (main != null) {
                        main.setVisible(false); // hide radio
                        GhostUnlockSplash.showSplash(main);
                    }
                });

                return "☠️ The spirits wail... You solved the riddle!\nChannel 666 is unlocked!";
            } else {
                return "👻 The voice fades: \"Wrong... try again...\"";
            }
        } else {
            return "You solved the riddle. Channel 666 is unlocked!";
        }
    }
}
