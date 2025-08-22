package haunted;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main interactive window for the Haunted Radio GUI.
 * - Center: a custom-drawn radio panel (RadioPanel) that shows your animated GIF in the dial.
 * - Bottom: controls to tune channels, stop audio, and adjust volume.
 *
 * Channels:
 *   1–4  -> play random Spanish spooky podcasts (with glitch bursts)
 *   5    -> shows a riddle dialog; correct answer unlocks 666
 *   666  -> ghost broadcast + hidden Morse message
 */
public class HauntedRadioScreen extends JFrame {
    // ----- UI components -----
    private final RadioPanel panel;         // draws the radio body + ghost GIF in the dial
    private final JButton ch1, ch2, ch3, ch4, ch5, ch666, stopBtn;
    private final JSlider volume;
    private final JLabel status;

    // ----- Logic / audio -----
    private final ChannelManagerSwing channels; // bridges buttons to audio + riddle logic

    public HauntedRadioScreen() {
        // Basic window setup
        setTitle("Haunted Radio — Interactive");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Audio root (relative to working dir): audio/static.wav, audio/ghost_broadcast.wav, audio/spanish_podcasts/*.wav
        Path audioRoot = Paths.get("audio");
        channels = new ChannelManagerSwing(audioRoot);

        // Center: custom-painted radio with animated GIF in the dial
        panel = new RadioPanel();
        add(panel, BorderLayout.CENTER);

        // Bottom: control strip (buttons + volume + status)
        JPanel controls = new JPanel(new GridBagLayout());
        controls.setBackground(new Color(22, 20, 24)); // dark theme to match the radio
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);

        // Row 1: Channel buttons + Stop
        c.gridx = 0; c.gridy = 0; ch1   = mkBtn("1");            controls.add(ch1, c);
        c.gridx = 1;             ch2   = mkBtn("2");            controls.add(ch2, c);
        c.gridx = 2;             ch3   = mkBtn("3");            controls.add(ch3, c);
        c.gridx = 3;             ch4   = mkBtn("4");            controls.add(ch4, c);
        c.gridx = 4;             ch5   = mkBtn("5 (Riddle)");   controls.add(ch5, c);
        c.gridx = 5;             ch666 = mkBtn("666");          ch666.setEnabled(false); controls.add(ch666, c);

        c.gridx = 6;             stopBtn = new JButton("Stop");
        // Keep button style consistent
        stopBtn.setFocusPainted(false);
        stopBtn.setBackground(new Color(50, 30, 30));
        stopBtn.setForeground(new Color(210, 240, 210));
        controls.add(stopBtn, c);

        // Row 2: Volume label + slider (map 0..100 -> 0.0..1.0)
        c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
        JLabel volLabel = new JLabel("Volume");
        volLabel.setForeground(new Color(200, 220, 210));
        controls.add(volLabel, c);

        c.gridx = 2; c.gridwidth = 4;
        volume = new JSlider(0, 100, 85);
        controls.add(volume, c);

        // Initialize player volume and wire slider to update live
        channels.getPlayer().setVolume(volume.getValue() / 100f);
        volume.addChangeListener(ev -> channels.getPlayer().setVolume(volume.getValue() / 100f));

        // Row 3: status bar
        c.gridx = 0; c.gridy = 2; c.gridwidth = 7;
        status = new JLabel("Ready. Tune a channel.");
        status.setForeground(new Color(190, 230, 200));
        controls.add(status, c);

        add(controls, BorderLayout.SOUTH);

        // ---------- Actions ----------
        // Shared handler for channels 1–4
        ch1.addActionListener(this::playChan);
        ch2.addActionListener(this::playChan);
        ch3.addActionListener(this::playChan);
        ch4.addActionListener(this::playChan);

        // Channel 5 -> riddle popup flow; correct answer enables 666
        ch5.addActionListener(evt -> handleRiddle());

        // Channel 666 -> ghost broadcast + Morse beeps
        ch666.addActionListener(evt -> {
            status.setText("Channel 666 — Ghost broadcast + Morse");
            channels.playGhost();
        });

        // Stop -> immediately stop any current audio
        stopBtn.addActionListener(e -> channels.getPlayer().stopAudio());
    }

    /**
     * Small helper to create consistently styled buttons for the radio.
     * Centralizing styling makes it easy to tweak the look in one place.
     */
    private JButton mkBtn(String label) {
        JButton b = new JButton(label);
        b.setFocusPainted(false);
        b.setBackground(new Color(50, 30, 30));   // subtle spooky vibe
        b.setForeground(new Color(210, 240, 210)); // readable on dark bg
        return b;
    }

    /**
     * Shared handler for channels 1–4. We parse the number from the button label
     * so we can reuse the same code for all four buttons.
     */
    private void playChan(ActionEvent evt) {
        String txt = ((JButton) evt.getSource()).getText();
        // Ensure we extract just the digits (in case labels change later)
        int ch = Integer.parseInt(txt.replaceAll("\\D", ""));
        status.setText("Playing Channel " + ch + "...");
        channels.playChannel(ch);
    }

    /**
     * Pops the riddle dialog for Channel 5.
     * Correct answers: "shadow" or "sombra" (case-insensitive, partial accepted via contains()).
     * On success: enable Channel 666; on failure: play a short static burst.
     */
    private void handleRiddle() {
        String prompt = "Channel 5 — Riddle\n"
                + "I follow you by day, I leave you by night,\n"
                + "I vanish in darkness yet cling to the light. What am I?";
        String ans = JOptionPane.showInputDialog(this, prompt, "Riddle", JOptionPane.QUESTION_MESSAGE);
        if (ans == null) return; // user canceled the dialog

        if (channels.trySolveRiddle(ans)) {
            ch666.setEnabled(true);
            status.setText("✅ Correct. Channel 666 unlocked.");
        } else {
            status.setText("❌ Not quite. Static...");
            channels.playStatic(1000);
        }
    }
}
