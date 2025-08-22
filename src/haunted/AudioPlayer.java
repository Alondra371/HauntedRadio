package haunted;

import javax.sound.sampled.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * AudioPlayer
 *  - Streams WAV audio (or formats decodable to PCM) via SourceDataLine.
 *  - Provides a Stop call that fully halts playback and frees the line.
 *  - Supports simple volume via MASTER_GAIN when the device exposes it.
 *  - Can inject short static bursts between segments for a “glitchy” effect.
 *
 * Methods used elsewhere:
 *   setVolume(float)
 *   stopAudio()
 *   playWavWithOccasionalGlitch(Path wav, Path staticWav, double glitchChance)
 *   playWavForMillis(Path wavPath, int millis)
 */
public class AudioPlayer {
    private final Random rng = new Random();

    // We keep references so STOP can interrupt and close the active line/thread.
    private volatile SourceDataLine currentLine; // active audio line, if any
    private volatile Thread playThread;          // background thread for segmented playback
    private volatile float volume = 0.85f;       // logical volume [0..1]

    /** Set target volume (0.0 = mute, 1.0 = max). */
    public void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        // If we’re already playing, try to update the current line’s gain live.
        SourceDataLine line = currentLine;
        if (line != null && line.isOpen()) {
            setGainIfSupported(line, volume);
        }
    }

    /**
     * Hard stop whatever is currently playing. This fully resets state and frees the audio device.
     * Always safe to call (no-op if nothing is playing).
     */
    public void stopAudio() {
        // Interrupt the background playback loop if it exists
        Thread t = playThread;
        if (t != null) {
            t.interrupt();
        }
        // Stop and close the active line if present
        SourceDataLine line = currentLine;
        if (line != null) {
            try {
                line.stop();
                line.flush();
                line.close();
            } catch (Exception ignored) {
                // We want to guarantee teardown even if some device throws
            }
        }
        // Clear references
        currentLine = null;
        playThread = null;
    }

    /**
     * Plays content in 2–4 segments, randomly inserting short static bursts in between
     * to simulate a glitchy broadcast.
     *
     * @param wav          main audio file to play in chunks
     * @param staticWav    static noise file (short bursts)
     * @param glitchChance probability [0..1] to inject a burst between segments
     */
    public void playWavWithOccasionalGlitch(Path wav, Path staticWav, double glitchChance) {
        stopAudio(); // ensure only one active playback at a time

        playThread = new Thread(() -> {
            int segments = 2 + rng.nextInt(3); // 2–4 segments
            for (int i = 0; i < segments && !Thread.currentThread().isInterrupted(); i++) {
                // 4–9 seconds of the main clip (or until EOF)
                playWavForMillis(wav, 4000 + rng.nextInt(5000));
                if (Thread.currentThread().isInterrupted()) break;

                // Random short static burst (300–700 ms)
                if (rng.nextDouble() < glitchChance) {
                    playWavForMillis(staticWav, 300 + rng.nextInt(400));
                }
            }
        }, "audio-play");
        playThread.start();
    }

    /**
     * Core streaming helper. If the file is missing or format is unsupported,
     * we just “sleep” for the requested duration so callers’ timing stays consistent.
     *
     * @param wavPath path to a WAV file
     * @param millis  maximum duration to stream (may end earlier on EOF)
     */
    public void playWavForMillis(Path wavPath, int millis) {
        if (millis <= 0) return;

        if (!Files.exists(wavPath)) {
            // Missing file — preserve timing so UX doesn’t feel broken
            sleep(millis);
            System.out.println("[Audio] play " + wavPath + " for " + millis + "ms (exists=" + java.nio.file.Files.exists(wavPath) + ")");

            return;
        }

        // If a higher-level call is in progress, allow STOP to interrupt it
        if (Thread.currentThread().isInterrupted()) return;

        try (AudioInputStream in = AudioSystem.getAudioInputStream(wavPath.toFile())) {
            AudioFormat base = in.getFormat();

            // Normalize to signed 16-bit PCM, little-endian, preserving channels & sample rate.
            // Many WAVs are already PCM, but this guarantees a streamable target format.
            AudioFormat decoded = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(),
                    16,
                    base.getChannels(),
                    base.getChannels() * 2,
                    base.getSampleRate(),
                    false // little-endian
            );

            try (AudioInputStream din = AudioSystem.getAudioInputStream(decoded, in)) {
                DataLine.Info info = new DataLine.Info(SourceDataLine.class, decoded);
                try (SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info)) {

                    // Store the line so stopAudio() can halt it
                    currentLine = line;

                    line.open(decoded);
                    setGainIfSupported(line, volume); // apply current volume if supported
                    line.start();

                    byte[] buffer = new byte[4096];
                    long end = System.currentTimeMillis() + millis;

                    while (System.currentTimeMillis() < end && !Thread.currentThread().isInterrupted()) {
                        int n = din.read(buffer, 0, buffer.length);
                        if (n <= 0) break; // EOF
                        line.write(buffer, 0, n);
                    }

                    // Finalize playback cleanly
                    line.drain();
                    line.stop();
                } finally {
                    currentLine = null; // line is closed by try-with-resources above
                }
            }
        } catch (UnsupportedAudioFileException e) {
            // Some WAVs/codecs might not be decodable on this JVM/audio stack
            sleep(millis);
        } catch (LineUnavailableException e) {
            // Device busy or not available — fail softly
            sleep(millis);
        } catch (Exception e) {
            // Any other I/O/runtime issue — keep UX timing consistent
            sleep(millis);
        }
    }

    // ---------- Internals ----------

    /**
     * Try to set MASTER_GAIN in decibels based on our 0..1 volume.
     * If unsupported on this device/line, we silently skip.
     */
    private void setGainIfSupported(SourceDataLine line, float vol) {
        try {
            if (line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                float min = gain.getMinimum(); // usually around -80 dB
                float max = gain.getMaximum(); // usually around +6 dB
                // Naive linear mapping 0..1 -> [min..max]; simple but effective
                float db = min + (max - min) * vol;
                gain.setValue(db);
            }
        } catch (Exception ignored) {
            // Some mixers/drivers throw on access; we keep going without volume control.
        }
    }

    /** Sleep helper that ignores InterruptedException (we check interruption in loops). */
    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            // If interrupted during a timed fallback, just return
        }
    }
}
