package haunted;

public abstract class Channel {
    protected int number;
    protected String name;
    protected String frequency;
    protected String description;
    protected String audioUrl;
    protected String theme;

    public Channel(int number, String name, String frequency, String description, String audioUrl, String theme) {
        this.number = number;
        this.name = name;
        this.frequency = frequency;
        this.description = description;
        this.audioUrl = audioUrl;
        this.theme = theme;
    }

    public int getNumber() { return number; }
    public String getName() { return name; }
    public String getFrequency() { return frequency; }
    public String getDescription() { return description; }
    public String getAudioUrl() { return audioUrl; }
    public String getTheme() { return theme; }

    // Output to screen
    public abstract String broadcast();

    // Optional: add audio playback behavior stub
    public void playAudio() {
        // Java audio playback logic could go here
        // (or this could be handled externally)
    }
}
