package core;

import core.userinterface.terminal.CommandLineInterface;
import core.userinterface.terminal.Terminal;
import core.userinterface.terminal.TerminalInput;
import core.userinterface.terminal.TerminalOutput;

public class Main {
    public static void main(String[] args) {
        Terminal terminal = new Terminal(new TerminalInput(), new TerminalOutput());
        CommandLineInterface userInterface = new CommandLineInterface(terminal);

        userInterface.start();
    }
}
