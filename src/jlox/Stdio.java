package jlox;

public class Stdio {
    private final StringBuilder _stderr = new StringBuilder();
    private final StringBuilder _stdout = new StringBuilder();

    static String stringify(Object value) {
        if (value == null)
            return "nil";
        var str = value.toString();
        if (str.endsWith(".0"))
            str = str.substring(0, str.length() - 2);
        return str;
    }

    String stderr() {
        return _stderr.toString();
    }
    String stdout() {
        return _stdout.toString();
    }

    boolean hasError() {
        return !_stderr.isEmpty();
    }
    boolean hasPrint() {
        return !_stdout.isEmpty();
    }

    void print(Object value) {
        _stdout.append(stringify(value)).append('\n');
    }

    void reset() {
        _stderr.setLength(0);
        _stdout.setLength(0);
    }

    boolean report() {
        if (hasPrint()) {
            System.out.print(stdout());
            System.out.flush();
        }
        if (hasError()) {
            System.err.print(stderr());
            System.err.flush();
            System.out.println();
            System.out.flush();
        }
        return hasError();
    }

    // Lexer error
    void errorAtLine(int line, String message) {
        reportError(line, "", message);
    }

    // Parser error
    void errorAtToken(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            reportError(token.line(), " at end", message);
        } else {
            reportError(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    private void reportError(int line, String where, String message) {
        _stderr.append("[line ").append(line).append("] Error").append(where).append(": ").append(message).append('\n');
    }
}
