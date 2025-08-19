package haunted;

public abstract class Channel {
    protected int number;
    protected String name;

    public Channel(int number, String name) {
        this.number = number;
        this.name = name;
    }

    public int getNumber() { return number; }
    public String getName() { return name; }

    public abstract String broadcast();
}
