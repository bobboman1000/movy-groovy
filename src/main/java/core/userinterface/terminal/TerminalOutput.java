package core.userinterface.terminal;

import java.util.List;
import java.util.Map;

public class TerminalOutput {
    /** Prints the parameter
     *  @param lines print that*/
    public void print(List<?> lines) {
        System.out.flush();
        lines.forEach(this::print);
    }

    /** Prints the parameter
     *  @param line print that */
    public void print(String line) {
        System.out.println(line);
    }

    /** Prints the prompt with a map like key - value
     *  @param prompt the prompt
     *  @param options the options */
    public void print(String prompt, Map<?, ?> options) {
        print(prompt); options.forEach((k, v) -> System.out.println(k + " -> " + v));
    }

    /** Prints the prompt with the options as lines
     *  @param prompt the prompt
     *  @param options the options */
    public void print(String prompt, List<?> options) {
        print(prompt); options.forEach(System.out::println);
    }

    /** Prints an object with toString
     *  @param o the object*/
    public void print(Object o) {
        System.out.println(o);
    }
}
