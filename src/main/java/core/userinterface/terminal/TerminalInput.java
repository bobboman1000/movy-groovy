package core.userinterface.terminal;

import core.util.Option;
import core.util.Try;

import java.util.Scanner;

/** Handles blocking calls to IO */
public class TerminalInput {
    private final Scanner scanner = new Scanner(System.in);

    /** @return Option of next Boolean */
    public Option<Boolean> getNextBoolean() { return getNextString().map(string -> string.startsWith("y")); }

    /** @return Option of next Integer */
    public Option<Integer> getNextInteger() { return getNextString().map(Integer::parseInt); }

    /** @return Option of next String */
    public Option<String> getNextString() {
        return Try.apply(scanner::nextLine).flatMap(s ->
                s.isEmpty() ? Try.failed(new Exception()) : Try.successful(s)
        ).toOption(); // there's gonna be exceptions but I so do not care
    }
}
