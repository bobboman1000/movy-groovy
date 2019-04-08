package core.userinterface.terminal;

import core.util.Option;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


public class Terminal {
    private final TerminalOutput outputHandler;
    private final TerminalInput inputHandler;

    /** Generate a new terminal that handles both input and output thru some helper functions
     *  @param inputHandler what to query for input
     *  @param outputHandler what to prompt to print */
    public Terminal(TerminalInput inputHandler, TerminalOutput outputHandler) {
        this.inputHandler = inputHandler;
        this.outputHandler = outputHandler;
    }

    public void put(Object any) { outputHandler.print(any); }
    public void put(List any) { outputHandler.print(any); }
    public void put(String prompt, Map options) { outputHandler.print(prompt, options); }
    public void put(String prompt, List options) { outputHandler.print(prompt, options); }

    /** Returns the selected option of a set of options
     * @param options the options the user can select from
     * @return int the key of the selected option */
    public Option<Integer> selectFrom(Map<Integer, ?> options) {
        return inputHandler.getNextInteger().flatMap(key ->
                options.keySet().contains(key) ? Option.of(key) : Option.empty()
        );
    }

    public Option<String> getString(String prompt) {
        outputHandler.print(prompt);
        return inputHandler.getNextString();
    }

    public Option<Boolean> selectYesOrNo(String prompt) {
        outputHandler.print(prompt);
        return inputHandler.getNextBoolean();
    }

    /** Will prompt the user to enter something, check if the entered value fits the predicat, if so you will get the
     *  value, otherwise the fallbackmessage will be printed and the fallbackvalue returned
     *  @param predicate the predicate to match
     *  @param prompt the prompt to present the user before asking for a value
     *  @param fallbackMessage the message to print if the entry was invalid
     *  @param fallbackValue the value to return if the entered value was invalid */
    public int getSelectionMatching(Predicate<Integer> predicate, String prompt, String fallbackMessage, int fallbackValue) {
        outputHandler.print(prompt);
        return inputHandler.getNextInteger()
                .flatMap(integer -> (predicate.test(integer)) ? Option.of(integer) : Option.empty())
                .orElseGet(() -> {
                    outputHandler.print(fallbackMessage);
                    return fallbackValue;
                });
    }
}
