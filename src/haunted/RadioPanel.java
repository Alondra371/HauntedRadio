package haunted;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

/**
 * RadioPanel (Interactive, Power + Tuning + Volume)
 * -------------------------------------------------
 * - Center POWER button toggles the radio on/off.
 * - Right TUNING knob snaps to 1..5 and calls onChannelChanged for each detent.
 * - Left VOLUME knob adjusts gain 0..1 and calls onVolumeChanged continuously while dragging.
 * - Dial window shows ghost.gif scaled to "cover" the window (fills completely).
 *
 * Resources expected on classpath:
 *   src/resources/ghost.gif
 *
 * Wire it from your frame:
 *   RadioPanel panel = new RadioPanel();
 *   ChannelManagerSwing manager = new ChannelManagerSwing(null);
 *   panel.wireDefaults(manager); // connects power/tune/volume to audio
 */
public class RadioPanel extends JPanel {

    // --- Haunted palette ---
    private static final Color BG           = new Color(13, 11, 26);   // dark navy
    private static final Color BODY_START   = new Color(48, 25, 52);   // purple
    private static final Color BODY_END     = new Color(20, 10, 30);   // deep purple-black
    private static final Color NEON         = new Color(0, 255, 128);  // neon green
    private static final Color PANEL_DARK   = new Color(25, 20, 40);
    private static final Color GRILLE_TOP   = new Color(30, 20, 50);
    private static final Color GRILLE_BOT   = new Color(10, 10, 20);
    private static final Color SILVER       = new Color(185, 200, 195);
    private static final Color KNOB_FACE    = new Color(40, 20, 60);
    private static final Color DIAL_BG      = Color.BLACK;

    // --- Dial ghost image ---
    private Image ghostImage;

    // --- Layout rects (computed per layout) ---
    private Rectangle bodyR   = new Rectangle();
    private Rectangle grilleR = new Rectangle();
    private Rectangle dialR   = new Rectangle();
    private Rectangle ctrlR   = new Rectangle();
    private Point     antennaA = new Point();
    private Point     antennaB = new Point();

    // --- Interactive controls (hit areas) ---
    private Ellipse volumeKnobShape;   // left knob
    private Ellipse tuningKnobShape;   // right knob
    private Ellipse powerButtonShape;  // center button

    // --- State ---
    private double volumeAngleDeg = 300; // 30..330 sweep (visual)
    private double tuningAngleDeg = 30;  // initialize at channel 1 detent
    private boolean powerOn = false;     // starts OFF so user must press center button

    private enum DragTarget { VOLUME, TUNING }
    private DragTarget dragging = null;

    // --- Public callbacks for wiring into audio/logic layer ---
    public interface ChannelListener { void onChannelChanged(int channel); }
    public interface VolumeListener  { void onVolumeChanged(float gain0to1); }
    public interface PowerListener   { void onPowerChanged(boolean on); }

    private ChannelListener onChannelChanged;
    private VolumeListener  onVolumeChanged;
    private PowerListener   onPowerChanged;

    private int currentChannel = 1;

    public RadioPanel() {
        setOpaque(true);
        setBackground(BG);

        // Load ghost.gif from classpath and keep as Image
        URL imgURL = getClass().getClassLoader().getResource("ghost.gif");
        if (imgURL != null) ghostImage = new ImageIcon(imgURL).getImage();

        // Mouse handling: click center to power toggle; drag knobs
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();
                if (powerButtonShape != null && powerButtonShape.contains(p)) {
                    // Toggle power state
                    powerOn = !powerOn;
                    if (onPowerChanged != null) onPowerChanged.onPowerChanged(powerOn);
                    // If powering ON, immediately (re)play current channel
                    if (powerOn && onChannelChanged != null) onChannelChanged.onChannelChanged(currentChannel);
                    repaint();
                    return;
                }
                if (tuningKnobShape != null && tuningKnobShape.contains(p)) {
                    dragging = DragTarget.TUNING;
                    return;
                }
                if (volumeKnobShape != null && volumeKnobShape.contains(p)) {
                    dragging = DragTarget.VOLUME;
                }
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (dragging == null) return;
                if (dragging == DragTarget.TUNING && tuningKnobShape != null) {
                    // Convert mouse to angle, clamp sweep, update detent channel, notify
                    tuningAngleDeg = clampSweep(angleFromCenter(tuningKnobShape.centerX(), tuningKnobShape.centerY(), e.getX(), e.getY()), 30, 330);
                    int ch = channelFromAngle(tuningAngleDeg);
                    if (ch != currentChannel) {
                        currentChannel = ch;
                        // Only actually change audio when powered on
                        if (powerOn && onChannelChanged != null) onChannelChanged.onChannelChanged(currentChannel);
                    }
                    repaint();
                } else if (dragging == DragTarget.VOLUME && volumeKnobShape != null) {
                    volumeAngleDeg = clampSweep(angleFromCenter(volumeKnobShape.centerX(), volumeKnobShape.centerY(), e.getX(), e.getY()), 30, 330);
                    if (onVolumeChanged != null) onVolumeChanged.onVolumeChanged(volumeFromAngle(volumeAngleDeg));
                    repaint();
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                if (dragging == DragTarget.TUNING) {
                    // Snap to exact detent on release
                    tuningAngleDeg = detentAngleForChannel(currentChannel);
                    repaint();
                }
                dragging = null;
            }

            @Override public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean hover = (tuningKnobShape != null && tuningKnobShape.contains(p))
                        || (volumeKnobShape != null && volumeKnobShape.contains(p))
                        || (powerButtonShape != null && powerButtonShape.contains(p));
                setCursor(hover ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    // --- Wiring API ---
    public void setOnChannelChanged(ChannelListener l) { this.onChannelChanged = l; }
    public void setOnVolumeChanged(VolumeListener l)   { this.onVolumeChanged  = l; }
    public void setOnPowerChanged(PowerListener l)     { this.onPowerChanged   = l; }

    // Convenience: typical wiring to ChannelManagerSwing+AudioPlayer
    public void wireDefaults(ChannelManagerSwing manager) {
        setOnChannelChanged(ch -> {
            if (powerOn && manager != null) manager.playChannel(ch);
        });
        setOnVolumeChanged(gain -> {
            if (manager != null && manager.getPlayer() != null) manager.getPlayer().setVolume(gain);
        });
        setOnPowerChanged(on -> {
            if (!on && manager != null && manager.getPlayer() != null) {
                manager.getPlayer().stopAudio();
            } else if (on && manager != null) {
                manager.playChannel(currentChannel);
            }
            repaint();
        });
    }

    // --- Layout ---
    @Override public void doLayout() {
        super.doLayout();
        layoutGeometry();
    }

    private void layoutGeometry() {
        int w = getWidth(), h = getHeight();
        int bodyW = Math.min((int)(w * 0.78), 820);
        int bodyH = Math.min((int)(h * 0.52), 380);
        int bodyX = (w - bodyW) / 2;
        int bodyY = (h - bodyH) / 2;

        bodyR.setBounds(bodyX, bodyY, bodyW, bodyH);

        // Speaker on left
        grilleR.setBounds(bodyX + (int)(bodyW*0.04), bodyY + (int)(bodyH*0.12),
                (int)(bodyW*0.42), (int)(bodyH*0.42));

        // Dial window on right-top (ghost should FILL this fully)
        dialR.setBounds(bodyX + (int)(bodyW*0.52), bodyY + (int)(bodyH*0.10),
                (int)(bodyW*0.40), (int)(bodyH*0.42));

        // Controls strip (bottom)
        ctrlR.setBounds(bodyX + (int)(bodyW*0.03), bodyY + (int)(bodyH*0.65),
                bodyW - (int)(bodyW*0.06), (int)(bodyH*0.26));

        // Antenna
        antennaA.setLocation(bodyX + (int)(bodyW*0.08), bodyY);
        antennaB.setLocation(bodyX - (int)(bodyW*0.15), bodyY - (int)(bodyH*0.33));

        // Knobs & power
        int volSize  = Math.min(ctrlR.height - 10, 72);
        int tuneSize = Math.min(ctrlR.height + (int)(bodyH*0.10), 130);

        int volX = ctrlR.x + (int)(ctrlR.width*0.10);
        int volY = ctrlR.y + (ctrlR.height - volSize)/2;
        volumeKnobShape = new Ellipse(volX, volY, volSize);

        int pSize = 32;
        int pX = bodyR.x + bodyR.width/2 - pSize/2;
        int pY = ctrlR.y + ctrlR.height/2 - pSize/2;
        powerButtonShape = new Ellipse(pX, pY, pSize);

        int tuneX = bodyR.x + bodyR.width - tuneSize - (int)(bodyW*0.04);
        int tuneY = ctrlR.y - (int)(tuneSize*0.15);
        tuningKnobShape = new Ellipse(tuneX, tuneY, tuneSize);
    }

    // --- Paint ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Body
        GradientPaint gp = new GradientPaint(bodyR.x, bodyR.y, BODY_START, bodyR.x + bodyR.width, bodyR.y + bodyR.height, BODY_END);
        g2.setPaint(gp);
        g2.fillRoundRect(bodyR.x, bodyR.y, bodyR.width, bodyR.height, 30, 30);

        g2.setColor(NEON);
        g2.setStroke(new BasicStroke(4f));
        g2.drawRoundRect(bodyR.x, bodyR.y, bodyR.width, bodyR.height, 30, 30);

        // Speaker
        GradientPaint gr = new GradientPaint(grilleR.x, grilleR.y, GRILLE_TOP, grilleR.x, grilleR.y + grilleR.height, GRILLE_BOT);
        g2.setPaint(gr);
        g2.fillRoundRect(grilleR.x, grilleR.y, grilleR.width, grilleR.height, 16, 16);

        g2.setColor(new Color(0, 255, 100, 110));
        for (int y = grilleR.y + 10; y < grilleR.y + grilleR.height - 6; y += 12) {
            g2.fillRoundRect(grilleR.x + 6, y, grilleR.width - 12, 5, 6, 6);
        }

        // Dial window
        g2.setColor(DIAL_BG);
        g2.fillRoundRect(dialR.x, dialR.y, dialR.width, dialR.height, 22, 22);
        g2.setColor(NEON);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRoundRect(dialR.x, dialR.y, dialR.width, dialR.height, 22, 22);

        // Ghost GIF: COVER (fill the whole dial, possibly cropping, no letterbox)
        if (ghostImage != null) {
            drawImageCover(g2, ghostImage, dialR);
        }

        // Control strip
        g2.setColor(PANEL_DARK);
        g2.fillRoundRect(ctrlR.x, ctrlR.y, ctrlR.width, ctrlR.height, 16, 16);

        // Antenna
        g2.setColor(SILVER);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(antennaA.x, antennaA.y, antennaB.x, antennaB.y);

        // Power button
        drawPowerButton(g2, powerButtonShape, powerOn);

        // Volume knob
        drawGenericKnob(g2, volumeKnobShape, volumeAngleDeg, "VOL");

        // Tuning knob with detents 1..5
        drawTuningKnob(g2, tuningKnobShape, tuningAngleDeg);

        g2.dispose();
    }

    // --- Drawing helpers ---

    private void drawPowerButton(Graphics2D g2, Ellipse e, boolean on) {
        if (on) {
            g2.setColor(new Color(0, 255, 128, 70));
            g2.fillOval(e.x - 7, e.y - 7, e.d + 14, e.d + 14);
        }
        g2.setColor(on ? new Color(25, 140, 80) : new Color(40, 40, 40));
        g2.fillOval(e.x, e.y, e.d, e.d);

        g2.setColor(NEON);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(e.x, e.y, e.d, e.d);

        // power glyph
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = e.centerX(), cy = e.centerY();
        int r  = (int)(e.d*0.28);
        g2.drawArc(cx - r, cy - r, r*2, r*2, 45, 270);
        g2.drawLine(cx, cy - (int)(r*1.1), cx, cy - (int)(r*1.9));
    }

    private void drawGenericKnob(Graphics2D g2, Ellipse e, double angleDeg, String label) {
        g2.setColor(new Color(0, 255, 128, 50));
        g2.fillOval(e.x - 6, e.y - 6, e.d + 12, e.d + 12);

        g2.setColor(KNOB_FACE);
        g2.fillOval(e.x, e.y, e.d, e.d);

        g2.setColor(NEON);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(e.x, e.y, e.d, e.d);

        // arc ticks
        drawArcTicks(g2, e.centerX(), e.centerY(), e.d/2 - 6, 30, 330, 7);

        // pointer
        drawPointer(g2, e.centerX(), e.centerY(), e.d/2 - 10, angleDeg);

        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        int tw = g2.getFontMetrics().stringWidth(label);
        g2.drawString(label, e.centerX() - tw/2, e.y + e.d + 14);
    }

    private void drawTuningKnob(Graphics2D g2, Ellipse e, double angleDeg) {
        g2.setColor(new Color(0, 255, 128, 50));
        g2.fillOval(e.x - 8, e.y - 8, e.d + 16, e.d + 16);

        g2.setColor(KNOB_FACE);
        g2.fillOval(e.x, e.y, e.d, e.d);

        g2.setColor(NEON);
        g2.setStroke(new BasicStroke(3f));
        g2.drawOval(e.x, e.y, e.d, e.d);

        // dense ticks along 30..330 sweep
        drawArcTicks(g2, e.centerX(), e.centerY(), e.d/2 - 8, 30, 330, 21);

        // detent ticks + labels 1..5
        for (int ch = 1; ch <= 5; ch++) {
            double a = detentAngleForChannel(ch);
            Point p1 = pointOnCircle(e.centerX(), e.centerY(), e.d/2 - 18, a);
            Point p2 = pointOnCircle(e.centerX(), e.centerY(), e.d/2 - 6,  a);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);

            String lbl = String.valueOf(ch);
            g2.setFont(g2.getFont().deriveFont(Font.BOLD, 13f));
            int tw = g2.getFontMetrics().stringWidth(lbl);
            Point lp = pointOnCircle(e.centerX(), e.centerY(), e.d/2 - 30, a);
            g2.drawString(lbl, lp.x - tw/2, lp.y + 5);
        }

        // pointer
        drawPointer(g2, e.centerX(), e.centerY(), e.d/2 - 12, angleDeg);

        // caption
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        String txt = "TUNE";
        int tw = g2.getFontMetrics().stringWidth(txt);
        g2.drawString(txt, e.centerX() - tw/2, e.y + e.d + 16);
    }

    private void drawArcTicks(Graphics2D g2, int cx, int cy, int radius, double startDeg, double endDeg, int count) {
        g2.setColor(new Color(0, 255, 128, 180));
        g2.setStroke(new BasicStroke(1.5f));
        double span = endDeg - startDeg;
        for (int i = 0; i <= count; i++) {
            double a = startDeg + span * (i / (double)count);
            Point p1 = pointOnCircle(cx, cy, radius - 8, a);
            Point p2 = pointOnCircle(cx, cy, radius, a);
            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
    }

    private void drawPointer(Graphics2D g2, int cx, int cy, int length, double angleDeg) {
        g2.setColor(NEON);
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Point tip = pointOnCircle(cx, cy, length, angleDeg);
        g2.drawLine(cx, cy, tip.x, tip.y);
        g2.fillOval(cx - 3, cy - 3, 6, 6);
    }

    // --- Geometry helpers ---

    /** Convert mouse XY to an angle (deg) around center, 0° at 12 o'clock, clockwise. */
    private double angleFromCenter(int cx, int cy, int mx, int my) {
        double dx = mx - cx;
        double dy = my - cy;
        double rad = Math.atan2(dx, -dy); // rotate axes to put 0° at up
        double deg = Math.toDegrees(rad);
        if (deg < 0) deg += 360;
        return deg;
    }

    /** Clamp angle to a sweep [minDeg..maxDeg] to simulate physical knob limits. */
    private double clampSweep(double angle, double minDeg, double maxDeg) {
        if (angle < minDeg) return minDeg;
        if (angle > maxDeg) return maxDeg;
        return angle;
    }

    /** Map 30..330 degrees into 5 equal detents (channels 1..5). */
    private int channelFromAngle(double angle) {
        double min = 30, max = 330;
        double span = max - min;                 // 300°
        double norm = (angle - min) / span;      // 0..1
        int idx = (int)Math.round(norm * 4.0);   // 0..4
        return Math.min(5, Math.max(1, idx + 1));
    }

    /** Exact detent angle for channel 1..5 across 30..330 sweep. */
    private double detentAngleForChannel(int ch) {
        ch = Math.min(5, Math.max(1, ch));
        double min = 30, max = 330;
        double step = (max - min) / 4.0;
        return min + step * (ch - 1);
    }

    /** Map volume knob angle to gain 0..1 with slight easing. */
    private float volumeFromAngle(double angle) {
        double min = 30, max = 330;
        double norm = (angle - min) / (max - min);
        double eased = Math.pow(Math.max(0, Math.min(1, norm)), 1.2);
        return (float)eased;
    }

    /** Point on circle, 0° at 12 o'clock. */
    private Point pointOnCircle(int cx, int cy, int radius, double angleDeg) {
        double rad = Math.toRadians(angleDeg);
        int x = cx + (int)Math.round(radius * Math.sin(rad));
        int y = cy - (int)Math.round(radius * Math.cos(rad));
        return new Point(x, y);
    }

    /** Draw image with CSS-like "cover": scale to fill bounds, cropping as needed (no letterbox). */
    /** Draw image with CSS-like "cover": scale to fill bounds, cropping as needed (no letterbox). */
    private void drawImageCover(Graphics2D g2, Image img, Rectangle bounds) {
        int imgW = img.getWidth(null);
        int imgH = img.getHeight(null);
        if (imgW <= 0 || imgH <= 0) return;

        // Scale to cover
        double scale = Math.max(bounds.getWidth() / imgW, bounds.getHeight() / imgH);
        int drawW = (int)Math.round(imgW * scale);
        int drawH = (int)Math.round(imgH * scale);

        // Center and crop
        int dx = bounds.x + (bounds.width - drawW)/2;
        int dy = bounds.y + (bounds.height - drawH)/2;

        // Use RoundRectangle2D for clipping instead of non-existent RoundRectangle
        Shape oldClip = g2.getClip();
        g2.setClip(new java.awt.geom.RoundRectangle2D.Double(
                bounds.x, bounds.y, bounds.width, bounds.height, 22, 22
        ));

        g2.drawImage(img, dx, dy, drawW, drawH, null);

        g2.setClip(oldClip);
    }


    // --- Public getters useful for initial wiring ---
    public int   getCurrentChannel()     { return currentChannel; }
    public float getCurrentVolume0to1()  { return volumeFromAngle(volumeAngleDeg); }
    public boolean isPowerOn()           { return powerOn; }

    // --- Small immutable ellipse for hit testing ---
    private static final class Ellipse {
        final int x, y, d;
        Ellipse(int x, int y, int d) { this.x = x; this.y = y; this.d = d; }
        boolean contains(Point p) {
            double rx = d/2.0, ry = d/2.0;
            double nx = (p.x - (x + rx)) / rx;
            double ny = (p.y - (y + ry)) / ry;
            return nx*nx + ny*ny <= 1.0;
        }
        int centerX() { return x + d/2; }
        int centerY() { return y + d/2; }
    }
}
