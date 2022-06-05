package jlox;

public class Error {
    private final StringBuilder log = new StringBuilder();

    String errors() {
        return log.toString();
    }

    boolean hadError() {
        return !log.isEmpty();
    }

    void reset() {
        log.setLength(0);
    }

    void report() {
        if (hadError()) {
            System.err.print(errors());
            System.err.flush();
        }
    }

    // Lexer error
    void atLine(int line, String message) {
        report(line, "", message);
    }

    // Parser error
    void atToken(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    private void report(int line, String where, String message) {
        log.append("[line ").append(line).append("] Error").append(where).append(": ").append(message).append('\n');
    }
}
