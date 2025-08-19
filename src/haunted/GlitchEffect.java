package haunted;

import java.util.Random;

public class GlitchEffect {
    private static Random random = new Random();

    public static String apply(String text) {
        StringBuilder glitched = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (random.nextInt(10) < 2) { 
                char[] noise = { '#', '*', '_', '%', '@' };
                glitched.append(noise[random.nextInt(noise.length)]);
            } else if (random.nextInt(20) == 0) {
                continue; // drop char
            } else {
                glitched.append(c);
            }
        }
        return glitched.toString();
    }
}
