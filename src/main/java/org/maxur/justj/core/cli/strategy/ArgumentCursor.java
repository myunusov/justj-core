package org.maxur.justj.core.cli.strategy;

import org.maxur.justj.core.cli.argument.Argument;
import org.maxur.justj.core.cli.exception.InvalidCommandLineError;

import java.util.Arrays;

import static java.lang.String.format;

/**
 * @author myunusov
 * @version 1.0
 * @since <pre>27.02.2016</pre>
 */
public class ArgumentCursor {

    private static final Character[] TOKEN_SYMBOLS = {'_', '?',','};

    private static final String NAME_PREFIX = "--";

    private static final String KEY_PREFIX = "-";

    private static final String TEXT_SYMBOL = "\"";

    private static final String DELIMITER = " ";

    private static final String LIST_SEPARATOR = ",";

    private final OptionDetector detector;

    private final String expression;

    private int pos = 0;

    private State state = State.INIT;

    private ArgumentCursor(final String expression, final OptionDetector detector) {
        this.expression = expression;
        this.detector = detector;
    }

    public static ArgumentCursor cursor(final String[] items, final OptionDetector detector) {
        final String expression = String.join(DELIMITER, items);
        return new ArgumentCursor(expression, detector);
    }

    public boolean hasNext() {
        return pos < expression.length();
    }

    public Argument next() throws InvalidCommandLineError {
        final Argument result;
        switch (state) {
            case INIT:
                skipSpace();
                result = argument();
                populate(result);
                break;
            case OPERATOR:
                skipSpace();
                result = new Argument(readKey());
                populate(result);
                break;
            default:
                throw new IllegalStateException("Unreachable statement");
        }
        return result;
    }

    private Argument argument() throws InvalidCommandLineError {
        if (startWith(NAME_PREFIX)) {
            skip(NAME_PREFIX);
            return new Argument(readName());
        } else if (startWith(KEY_PREFIX)) {
            skip(KEY_PREFIX);
            return new Argument(readKey());
        } else {
            final Argument result = new Argument();
            result.setOptionArgument(operand());
            return result;
        }
    }

    private void populate(final Argument argument) throws InvalidCommandLineError {
        if (!detector.findOptionBy(argument).isEmpty()) {
            argument.setOptionArgument(optionArgument());
        } else if (!detector.findListBy(argument).isEmpty()) {
            argument.setOptionArgument(listArgument());
        }
    }

    private String listArgument() throws InvalidCommandLineError {
        skipSpace();
        return readList();
    }

    private String optionArgument() throws InvalidCommandLineError {
        skipSpace();
        if (startWith(TEXT_SYMBOL)) {
            return readText();
        } else {
            return readWord();
        }
    }


    private String operand() {
        return readOperand();
    }

    private boolean startWith(final String s) {
        skipSpace();
        return expression.startsWith(s, pos);
    }

    private void skip(final String s) throws InvalidCommandLineError {
        skipSpace();
        if (startWith(s)) {
            pos += s.length();
        } else {
            throw new InvalidCommandLineError(format(
                    "Invalid expression '%s'. Must be '%s' in '%d' position"
                    , expression, s, pos
            )
            );
        }
        skipSpace();
    }

    private void skipSpace() {
        while (hasNext() && expression.charAt(pos) == ' ') {
            pos++;
        }
    }

    private Character readKey() {
        state = State.OPERATOR;
        if (isTokenSymbol()) {
            if (hasNext()) {
                return expression.charAt(pos++);
            }
        } else {
            state = State.INIT;
        }
        return null;
    }

    private String readName() {
        if (isTokenSymbol()) {
            return readToken();
        }
        return null;
    }

    private String readList() throws InvalidCommandLineError {
        String list = "";
        do {
            if (!hasNext()) {
                throw new InvalidCommandLineError(format("List '%s' is not closed", list));
            }
            list += readWord();
        } while(list.endsWith(LIST_SEPARATOR) || startWith(LIST_SEPARATOR));
        return list;
    }

    private String readWord() {
        skipSpace();
        String word = "";
        while (!expression.startsWith(DELIMITER, pos) && hasNext()) {
            word += Character.toString(expression.charAt(pos++));
        }
        return word;
    }

    private String readOperand() {
        String operand = "";
        while (!expression.startsWith(" ", pos) && hasNext()) {
            operand += Character.toString(expression.charAt(pos++));
        }
        return operand;
    }

    private String readText() throws InvalidCommandLineError {
        skip(TEXT_SYMBOL);
        String text = "";
        while (!expression.startsWith(TEXT_SYMBOL, pos)) {
            text += Character.toString(expression.charAt(pos++));
            if (!hasNext()) {
                throw new InvalidCommandLineError(format(
                        "Invalid expression '%s'. Must be '%s' in '%d' position"
                        , expression, "\"", pos
                ));
            }
        }
        skip(TEXT_SYMBOL);
        return text;
    }

    private String readToken() {
        String token = "";
        while (hasNext() && isTokenSymbol()) {
            token += Character.toString(expression.charAt(pos++));
        }
        return token;
    }

    private boolean isTokenSymbol() {
        final Character character = expression.charAt(pos);
        return Character.isLetterOrDigit(expression.charAt(pos)) ||
                Arrays.asList(TOKEN_SYMBOLS).contains(character);
    }

    private enum State {
        INIT,
        OPERATOR
    }
}
