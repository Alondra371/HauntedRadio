package haunted;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.sound.sampled.*;
import java.net.URL;

/**
 * HauntedSplashScreen
 * -------------------
 * Visuals are unchanged, but the intro audio now fades out smoothly
 * when you leave the splash (SPACE). We do a non-blocking fade using a Swing Timer.
 *
 * Place audio at: src/resources/audio/ghost_intro.wav
 * It will be resolved via getClass().getClassLoader().getResource("audio/ghost_intro.wav").
 */
public class HauntedSplashScreen extends JPanel {
    public static final int BASE_W = 256;
    public static final int BASE_H = 240;

    private long startNanos = System.nanoTime();
    private boolean spacePressed = false;
    private final Runnable onSpacebarPressed;

    // Colors (unchanged)
    private static final Color SKY = new Color(12, 22, 36);
    private static final Color TEXT_WHITE = new Color(206, 216, 230);
    private static final Color CLOUD = new Color(160, 184, 202);
    private static final Color GHOST = new Color(188, 204, 220);
    private static final Color GHOST_SHADOW = new Color(40, 56, 76);

    // Intro audio
    private Clip introClip;
    private FloatControl introGain; // cached gain control if available
    private volatile boolean fading = false; // prevent multiple simultaneous fades

    public HauntedSplashScreen(Runnable onSpacebarPressed) {
        this.onSpacebarPressed = onSpacebarPressed;
        setPreferredSize(new Dimension(BASE_W * 3, BASE_H * 3));
        setBackground(Color.black);

        setFocusable(true);
        requestFocusInWindow();

        // SPACE → fade out intro, then callback
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!spacePressed && e.getKeyCode() == KeyEvent.VK_SPACE) {
                    spacePressed = true;
                    // Smooth fade (non-blocking). The clip will stop/close at the end.
                    stopIntro(); // now does a fade instead of an immediate stop
                    if (onSpacebarPressed != null) {
                        onSpacebarPressed.run();
                    }
                }
            }
        });

        // Start ambient intro audio (optional resource)
        startIntro();
    }

    @Override
    protected void paintComponent(Graphics gOuter) {
        super.paintComponent(gOuter);

        BufferedImage buf = new BufferedImage(BASE_W, BASE_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = buf.createGraphics();
        double t = (System.nanoTime() - startNanos) / 1_000_000_000.0;

        // Background
        g.setColor(SKY);
        g.fillRect(0, 0, BASE_W, BASE_H);

        // HUD
        drawPixelText(g, "AJC", 1, 3, 5, true, TEXT_WHITE);

        // Clouds
        drawCloud(g, 10, 30);
        drawCloud(g, 206, 44);

        // Title text
        String title = "GHOST FREQUENCY";
        g.setFont(new Font("Monospaced", Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        int titleX = (BASE_W - fm.stringWidth(title)) / 2;
        int titleY = 120;

        // Extra clouds around title
        drawCloud(g, titleX - 40, titleY - 60);
        drawCloud(g, titleX + 100, titleY - 50);
        drawCloud(g, titleX + 20, titleY - 70);

        // Ghosts around title
        drawCuteGhost(g, titleX - 60, titleY - 40, t + 0.5);
        drawCuteGhost(g, titleX + 140, titleY - 30, t + 1.0);
        drawCuteGhost(g, titleX + 60, titleY - 50, t + 1.5);

        // Floating ghosts
        drawCuteGhost(g, 24, 128, t);
        drawCuteGhost(g, 212, 120, t + 1.2);
        drawCuteGhost(g, 168, 144, t + 2.0);

        // Title glow
        for (int i = 0; i < 6; i++) {
            g.setColor(new Color(0, 255, 100, 40));
            g.drawString(title, titleX - i, titleY - i);
        }
        g.setColor(new Color(0, 255, 0));
        g.drawString(title, titleX, titleY);

        // Prompt
        g.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g.setColor(Math.random() > 0.5 ? new Color(0, 255, 0) : new Color(0, 200, 0));
        String prompt = "Press START to tune in...";
        int promptX = (BASE_W - g.getFontMetrics().stringWidth(prompt)) / 2;
        g.drawString(prompt, promptX, 180);

        // Bottom text
        drawPixelText(g, "TOP - 000000", (BASE_W - ("TOP - 000000".length() * 8)) / 2, 200, 12, true, TEXT_WHITE);

        g.dispose();
        gOuter.drawImage(buf, 0, 0, getWidth(), getHeight(), null);
    }

    // ---------- Drawing helpers (unchanged visuals) ----------

    private void drawPixelText(Graphics2D g, String s, int x, int y, int pxSize, boolean bold, Color color) {
        Font f = new Font("Monospaced", bold ? Font.BOLD : Font.PLAIN, pxSize);
        g.setFont(f);
        g.setColor(color);
        g.drawString(s, x, y);
    }

    private void drawCloud(Graphics2D g, int x, int y) {
        g.setColor(CLOUD);
        g.fillRect(x + 6, y + 6, 20, 8);
        g.fillRect(x + 2, y + 10, 28, 6);
        g.fillRect(x + 12, y + 2, 10, 8);
    }

    private void drawCuteGhost(Graphics2D g, int x, int y, double t) {
        int yy = y + (int) Math.round(Math.sin(t * 2.0 + x * 0.1) * 2);

        g.setColor(GHOST_SHADOW);
        g.fillRoundRect(x + 1, yy + 1, 16, 20, 10, 10);

        g.setColor(GHOST);
        g.fillRoundRect(x, yy, 16, 20, 10, 10);

        int waveY = yy + 18;
        g.fillOval(x, waveY, 5, 5);
        g.fillOval(x + 5, waveY + 1, 5, 5);
        g.fillOval(x + 10, waveY, 5, 5);

        g.setColor(Color.BLACK);
        g.fillOval(x + 4, yy + 6, 3, 4);
        g.fillOval(x + 9, yy + 6, 3, 4);

        g.setColor(Color.WHITE);
        g.fillOval(x + 5, yy + 7, 1, 1);
        g.fillOval(x + 10, yy + 7, 1, 1);
    }

    // ---------- Audio: start + smooth fade-out stop ----------

    private void startIntro() {
        try {
            // Look for /audio/ghost_intro.wav on the classpath
            URL url = getClass().getClassLoader().getResource("audio/ghost_intro.wav");
            System.out.println("[Splash] intro URL = " + url);
            if (url == null) return; // optional audio

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            introClip = AudioSystem.getClip();
            introClip.open(ais);

            // Cache gain control (if device supports it)
            introGain = getGainControl(introClip);

            // Set initial ambience volume
            setGainDb(introGain, -10.0f);

            introClip.loop(Clip.LOOP_CONTINUOUSLY);
            introClip.start();
        } catch (Exception ex) {
            // Keep visuals working even if audio fails
            System.out.println("[Splash] Intro audio unavailable: " + ex.getMessage());
        }
    }

    /**
     * Public stop that now fades the intro out smoothly (approx 600ms) and closes the clip.
     * Existing callers don't need to change anything.
     */
    public void stopIntro() {
        fadeOutAndStop(600);
    }

    /**
     * Fade the clip down over durationMs using a Swing Timer (non-blocking),
     * then stop/close the clip. If gain control is unavailable, stops immediately.
     */
    public void fadeOutAndStop(int durationMs) {
        if (introClip == null) return;
        if (fading) return; // already fading
        fading = true;

        // If no gain control, just stop immediately
        if (introGain == null || !introClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            safeStopAndClose();
            fading = false;
            return;
        }

        // Figure out current and minimum gain in dB
        final float startDb = introGain.getValue();
        final float minDb = introGain.getMinimum();
        final int steps = Math.max(8, durationMs / 30); // ~30ms per step, at least 8 steps
        final long startAt = System.currentTimeMillis();

        Timer timer = new Timer(30, null);
        timer.addActionListener(ev -> {
            long elapsed = System.currentTimeMillis() - startAt;
            float t = Math.min(1f, elapsed / (float) durationMs);

            // Smooth curve for fade (ease-out)
            float eased = (float) Math.pow(t, 1.8);

            float currDb = lerp(startDb, minDb, eased);
            setGainDb(introGain, currDb);

            if (t >= 1f) {
                timer.stop();
                safeStopAndClose();
                fading = false;
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

    private void safeStopAndClose() {
        try {
            if (introClip != null) {
                introClip.stop();
                introClip.flush();
                introClip.close();
            }
        } catch (Exception ignored) {
        } finally {
            introClip = null;
            introGain = null;
        }
    }

    private FloatControl getGainControl(Clip clip) {
        try {
            if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                return (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            }
        } catch (Exception ignored) { }
        return null;
    }

    private void setGainDb(FloatControl gain, float db) {
        if (gain == null) return;
        try {
            db = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db));
            gain.setValue(db);
        } catch (Exception ignored) { }
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // If the panel is removed (window closes), ensure we fade quickly to avoid abrupt cut.
        fadeOutAndStop(250);
    }
}

