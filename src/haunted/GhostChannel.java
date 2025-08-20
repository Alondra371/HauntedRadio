package haunted;

public class GhostChannel extends Channel {
    private String morseCode = "- .... .- - .----. ...   .- -.   .-   .--. .-.. ..- ...";
    private String translation = "That's an A plus";
    private int revealIndex = 0;
    private boolean done = false;

    // Full constructor matching Channel's constructor
    public GhostChannel(int number, String name, String frequency, String description, String audioUrl, String theme) {
        super(number, name, frequency, description, audioUrl, theme);
    }

    // Optional: keep simple constructor for backward compatibility,
    // initializing missing fields with placeholders
    public GhostChannel(int number, String name) {
        super(number, name, "666 FM", "Secret Morse Code Station", "", "ghost");
    }

    @Override
    public String broadcast() {
        if (!done) {
            if (revealIndex < morseCode.length()) {
                revealIndex++;
                return "📡 Channel 666 crackles alive...\n" +
                        morseCode.substring(0, revealIndex);
            } else {
                done = true;
                return "📡 Channel 666 crackles alive...\n" +
                        morseCode + "\n\n☠️ Translation: \"" + translation + "\"";
            }
        } else {
            return "📡 Channel 666 crackles alive...\n" +
                    morseCode + "\n\n☠️ Translation: \"" + translation + "\"";
        }
    }
}
