package haunted;

public class MusicChannel extends Channel {
    public MusicChannel(int number, String name) {
        super(number, name);
    }

    @Override
    public String broadcast() {
        return "♪ Creepy organ music drifts through the static...";
    }
}
