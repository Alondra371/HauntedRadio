package haunted;

import java.util.*;

public class Radio {
    private Map<Integer, Channel> channels = new HashMap<>();

    public Radio() {
        channels.put(0, new MusicChannel(0, "Creepy Music"));
        channels.put(1, new StaticChannel(1, "Static Noise"));
        channels.put(2, new MusicChannel(2, "Haunted Choir"));
        channels.put(3, new StaticChannel(3, "Distorted Static"));
        channels.put(4, new MusicChannel(4, "Distorted Melody"));
        channels.put(5, new RiddleChannel(5, "Riddle Channel"));
        channels.put(666, new GhostChannel(666, "Ghost Broadcast"));
    }

    public String tune(int number) {
        Channel c = channels.get(number);
        if (c != null) {
            return c.broadcast();
        }
        return "No signal...";
    }
}
