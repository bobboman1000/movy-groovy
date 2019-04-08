package core.userinterface.terminal;

public class CommandLineInterface {
    private Terminal terminal;

    public CommandLineInterface(Terminal terminal) {
        this.terminal = terminal;
    }

    public void start() {
        while (true) {
            terminal.put("Yeah we running");
            boolean stop = terminal
                    .selectYesOrNo("You want to stop running?")
                    .getOrElse(true);

            if (stop) System.exit(0);
        }
    }
}
