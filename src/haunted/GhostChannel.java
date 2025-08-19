package haunted;

public class GhostChannel extends Channel {
    private String morseCode = "- .... .- - .----. ...   .- -.   .-   .--. .-.. ..- ...";
    private String translation = "That's an A plus";
    private int revealIndex = 0;
    private boolean done = false;

    public GhostChannel(int number, String name) {
        super(number, name);
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
