package haunted;

public class StaticChannel extends Channel {
    public StaticChannel(int number, String name) {
        super(number, name);
    }

    @Override
    public String broadcast() {
        return "~~~ ssssshhhkkkzzz... static fills the air... ~~~";
    }
}
