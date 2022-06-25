package jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static jlox.TokenType.*;

class Scanner {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = Map.ofEntries(
                entry("and", AND),
                entry("break", BREAK),
                entry("class", CLASS),
                entry("const", CONST),
                entry("continue", CONTINUE),
                entry("else", ELSE),
                entry("false", FALSE),
                entry("for", FOR),
                entry("fun", FUN),
                entry("if", IF),
                entry("nil", NIL),
                entry("or", OR),
                entry("print", PRINT),
                entry("return", RETURN),
                entry("self", SELF),
                entry("super", SUPER),
                entry("true", TRUE),
                entry("this", SELF),
                entry("var", VAR),
                entry("while", WHILE)
        );
    }

    private final String source;
    private final Stdio stdio;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;      // offset in source
    private int current = 0;    // offset in source
    private int line;

    Scanner(String source, Stdio stdio, int startingLine) {
        this.source = source;
        this.stdio = stdio;
        this.line = startingLine;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
            formCompoundOperator();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    /**
     * It is tedious to create a different token for all compound operators.
     * It is almost impossible to parse them correctly.
     * The best way I found is to produce them from already scanned tokens.
     */
    private void formCompoundOperator() {
        var last = previous(0);
        var previous = previous(1);
        if (last != null && last.type() == EQUAL && isCompoundable(previous)) {
            var equal = tokens.remove(tokens.size() - 1);
            var operator = tokens.remove(tokens.size() - 1);
            tokens.add(new Token(COMPOUND_EQUAL,
                    operator.lexeme() + equal.lexeme(),
                    new Token.Compound(operator, equal),
                    equal.line()));
        }
    }

    private boolean isCompoundable(Token token) {
        if (token == null) return false;
        return switch (token.type()) {
            case PERCENT, SLASH, MINUS, PLUS, STAR, STAR_STAR, AND, OR -> true;
            default -> false;
        };
    }

    private Token previous(int lookBack) {
        assert lookBack >= 0;
        if (tokens.size() > lookBack)
            return tokens.get(tokens.size() - 1 - lookBack);
        return null;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            // Single char tokens
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case ':':
                addToken(COLON);
                break;
            case '.':
                addToken(DOT);
                break;
            case '%':
                addToken(PERCENT);
                break;
            case '?':
                addToken(QUESTION);
                break;
            case ';':
                addToken(SEMICOLON);
                break;

            // Single or double char tokens
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '-':
                addToken(match('-') ? MINUS_MINUS : MINUS);
                break;
            case '+':
                addToken(match('+') ? PLUS_PLUS : PLUS);
                break;
            case '*':
                if (match('/')) {
                    stdio.errorAtLine(line, "Unexpected block comment end.");
                } else {
                    addToken(match('*') ? STAR_STAR : STAR);
                }
                break;

            // slash or comment
            case '/':
                if (match('/')) {
                    // Line comments
                    advanceAfter('\n');
                } else if (match('*')) {
                    blockComment(line);
                } else {
                    addToken(SLASH);
                }
                break;

            // Whites
            case ' ':
            case '\n':
            case '\t':
            case '\r':
                // Ignore
                break;

            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // TODO Collapse consecutive unexpected chars together in a single error message.
                    stdio.errorAtLine(line, "Unexpected character '" + c + "'");
                }
        }
    }

    private boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    private boolean isAlpha(char c) {
        return 'a' <= c && c <= 'z'
                || 'A' <= c && c <= 'Z'
                || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    private char advance() {
        char c = source.charAt(current++);
        if (c == '\n') line++;
        return c;
    }

    @SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
    private boolean advanceAfter(char expected) {
        while (peek() != expected) {
            if (isAtEnd()) return false;
            advance();
        }
        advance();
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean advanceAfterAnyOf(String expected) {
        while (expected.indexOf(peek()) < 0) {
            if (isAtEnd()) return false;
            advance();
        }
        advance();
        return true;
    }

    private String advanceAfterAnyMatch(String ... matches) {
        while (!isAtEnd()) {
            for (String m : matches) {
                if (match(m)) return m;
            }
            advance();
        }
        return null;
    }

    private boolean match(char expected) {
        if (peek() != expected)
            return false;
        advance();
        return true;
    }

    private boolean match(String expected) {
        if (source.startsWith(expected, current)) {
            current += expected.length();
            return true;
        }
        return false;
    }

    private char peek() {
        return peek(0);
    }

    private char peek(int ahead) {
        if (current + ahead < source.length())
            return source.charAt(current + ahead);
        return '\0';
    }

    private void string() {
        while (advanceAfterAnyOf("\\\"")) {
            if (peek(-1) == '\"') {
                String value = source.substring(start + 1, current - 1);
                addToken(STRING, value.translateEscapes());
                return;
            }
            advance();
        }
        stdio.errorAtLine(line, "Unterminated string");
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peek(1)))
            advance();

        while (isDigit(peek())) advance();

        String numeric = source.substring(start, current);

        // NB parseDouble will succeed here
        addToken(NUMBER, Double.parseDouble(numeric));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        var type = keywords.get(text);
        if (type == null)
            type = IDENTIFIER;
        addToken(type);
    }

    private void blockComment(int startLine) {
        int nesting = 1;
        while (nesting > 0) {
            String match = advanceAfterAnyMatch("/*", "*/");
            if (match == null) {
                String msg = "Unterminated block comment";
                if (line != startLine) msg += ", started on line " + startLine;
                if (nesting > 1) msg += ", nesting level " + nesting;
                msg += ".";
                stdio.errorAtLine(line, msg);
                return;
            } else if (match.equals("/*")) {
                nesting++;
            } else {
                assert match.equals("*/");
                nesting--;
            }
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
