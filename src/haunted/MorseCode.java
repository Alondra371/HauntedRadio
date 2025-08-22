package haunted;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * MorseCode handles encoding strings into Morse code
 * and playing them back as beeps using Java Sound API.
 */
public class MorseCode {

    // Basic Morse encoding table
    private static final Map<Character, String> morseMap = new HashMap<>();
    static {
        morseMap.put('A', ".-");
        morseMap.put('B', "-...");
        morseMap.put('C', "-.-.");
        morseMap.put('D', "-..");
        morseMap.put('E', ".");
        morseMap.put('F', "..-.");
        morseMap.put('G', "--.");
        morseMap.put('H', "....");
        morseMap.put('I', "..");
        morseMap.put('J', ".---");
        morseMap.put('K', "-.-");
        morseMap.put('L', ".-..");
        morseMap.put('M', "--");
        morseMap.put('N', "-.");
        morseMap.put('O', "---");
        morseMap.put('P', ".--.");
        morseMap.put('Q', "--.-");
        morseMap.put('R', ".-.");
        morseMap.put('S', "...");
        morseMap.put('T', "-");
        morseMap.put('U', "..-");
        morseMap.put('V', "...-");
        morseMap.put('W', ".--");
        morseMap.put('X', "-..-");
        morseMap.put('Y', "-.--");
        morseMap.put('Z', "--..");
        morseMap.put('0', "-----");
        morseMap.put('1', ".----");
        morseMap.put('2', "..---");
        morseMap.put('3', "...--");
        morseMap.put('4', "....-");
        morseMap.put('5', ".....");
        morseMap.put('6', "-....");
        morseMap.put('7', "--...");
        morseMap.put('8', "---..");
        morseMap.put('9', "----.");
    }

    /**
     * Play a full message in Morse code.
     * Each character is encoded into dots/dashes and played as tones.
     */
    public static void playMessage(String msg) {
        for (char c : msg.toUpperCase().toCharArray()) {
            if (c == ' ') {
                sleep(700); // Space between words
                continue;
            }
            String code = morseMap.get(c);
            if (code != null) {
                playCode(code);
                sleep(300); // Gap between letters
            }
        }
    }

    /** Convert Morse sequence (like "...") into audio beeps. */
    private static void playCode(String code) {
        for (char symbol : code.toCharArray()) {
            if (symbol == '.') {
                beep(200); // Dot
            } else if (symbol == '-') {
                beep(600); // Dash
            }
            sleep(150); // Gap between parts of a letter
        }
    }

    /** Generate a simple sine wave beep of given duration (ms). */
    private static void beep(int durationMs) {
        try {
            float sampleRate = 44100;
            byte[] buf = new byte[(int) (durationMs * sampleRate / 1000)];
            for (int i = 0; i < buf.length; i++) {
                double angle = i / (sampleRate / 800.0); // tone at ~800Hz
                buf[i] = (byte) (Math.sin(angle) * 127);
            }

            AudioFormat af = new AudioFormat(sampleRate, 8, 1, true, false);
            try (SourceDataLine sdl = AudioSystem.getSourceDataLine(af)) {
                sdl.open(af);
                sdl.start();
                sdl.write(buf, 0, buf.length);
                sdl.drain();
                sdl.stop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Helper sleep without checked exceptions. */
    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
