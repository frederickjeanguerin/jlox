package jlox;

public class Stdio {

    private int printCount = 0;
    private int errorCount = 0;
    private int warningCount = 0;

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
        return errorCount > 0;
    }
    boolean hasWarning() {
        return warningCount > 0;
    }
    boolean hasPrint() {
        return !_stdout.isEmpty();
    }

    void print(Object value) {
        printCount++;
        _stdout.append(stringify(value)).append('\n');
    }

    void reset() {
        printCount = 0;
        errorCount = 0;
        warningCount = 0;
        _stderr.setLength(0);
        _stdout.setLength(0);
    }

    boolean report() {
        if (hasPrint()) {
            System.out.print(stdout());
            System.out.flush();
        }
        if (hasError() || hasWarning()) {
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

    void warningAtToken(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            reportWarning(token.line(), " at end", message);
        } else {
            reportWarning(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    private void reportError(int line, String where, String message) {
        errorCount++;
        reportSome(line, where, message, "Error");
    }

    private void reportWarning(int line, String where, String message) {
        warningCount++;
        reportSome(line, where, message, "Warning");
    }

    private void reportSome(int line, String where, String message, String errorType) {
        _stderr .append("[line ")
                .append(line)
                .append("] ")
                .append(errorType)
                .append(where)
                .append(": ")
                .append(message)
                .append('\n');
    }

    public int getPrintCount() {
        return printCount;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public int getWarningCount() {
        return warningCount;
    }
}
