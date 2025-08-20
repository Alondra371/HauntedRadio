package haunted;

public class MusicChannel extends Channel {
    public MusicChannel(int number, String name, String frequency, String description, String audioUrl, String theme) {
        super(number, name, frequency, description, audioUrl, theme);
    }

    // Overloaded constructor for 2 arguments
    public MusicChannel(int number, String name) {
        this(number, name, "Unknown Frequency", "No description", "No audio", "No theme");
    }

    @Override
    public String broadcast() {
        return "Playing smooth haunted tunes...";
    }
}
