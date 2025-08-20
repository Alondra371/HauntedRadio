package haunted;

import javax.swing.*;

public class RiddleChannel extends Channel {
    private boolean solved = false;

    // ✅ 2-argument constructor with default values
    public RiddleChannel(int number, String name) {
        this(number, name, "101.1 FM", "Cryptic riddles from the void", "riddle.mp3", "Mysterious");
    }

    // Existing 6-argument constructor
    public RiddleChannel(int number, String name, String frequency, String description, String audioUrl, String theme) {
        super(number, name, frequency, description, audioUrl, theme);
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

            if (answer != null && answer.trim().equalsIgnoreCase("echo")) {
                solved = true;
                HauntedRadio.unlockSecret();

                // Optional GUI effect — if GhostUnlockSplash is implemented
                SwingUtilities.invokeLater(() -> {
                    JFrame main = (JFrame) SwingUtilities.getWindowAncestor(new JPanel());
                    if (main != null) {
                        main.setVisible(false);
                        GhostUnlockSplash.showSplash(main);
                    }
                });

                return " The spirits wail... You solved the riddle!\nChannel 666 is unlocked!";
            } else {
                return " The voice fades: \"Wrong... try again...\"";
            }
        } else {
            return "You solved the riddle. Channel 666 is unlocked!";
        }
    }
}
