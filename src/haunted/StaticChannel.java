package haunted;

public class StaticChannel extends Channel {
    public StaticChannel(int number, String name, String frequency, String description, String audioUrl, String theme) {
        super(number, name, frequency, description, audioUrl, theme);
    }

    // Overloaded constructor for 2 arguments
    public StaticChannel(int number, String name) {
        this(number, name, "Unknown Frequency", "Just static noise", "No audio", "Static");
    }

    @Override
    public String broadcast() {
        return "sssshhhhhhhhhhhhh... *static intensifies*";
    }
}
