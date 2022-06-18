package jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static jlox.TokenType.*;

class Scanner {
    private final String source;
    private final Stdio stdio;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;      // offset in source
    private int current = 0;    // offset in source
    private int line = 1;

    Scanner(String source, Stdio stdio) {
        this.source = source;
        this.stdio = stdio;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // we are at the beginning of the next lexeme
            start = current;
            scanToken();
        }
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
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
            case '*':
                addToken(STAR);
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

            // slash or comment
            case '/':
                if (match('/')) {
                    // Line comments
                    advanceAfter('\n');
                } else if (match('*')) {
                    blockComment();
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

    private boolean advanceAfter(char expected) {
        while (peek() != expected) {
            if (isAtEnd()) return false;
            advance();
        }
        advance();
        return true;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean advanceAfter(char expected, char nextExpected) {
        while (peek() != expected || peek(1) != nextExpected) {
            if (isAtEnd()) return false;
            advance();
        }
        advance(); advance();
        return true;
    }

    private boolean match(char expected) {
        if (peek() != expected)
            return false;
        advance();
        return true;
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
        // TODO Add escape sequences or at least the possibility to insert a " inside a string
        if (!advanceAfter('"')) {
            stdio.errorAtLine(line, "Unterminated string");
        } else {
            String value = source.substring(start + 1, current - 1);
            addToken(STRING, value.translateEscapes());
        }
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

    private void blockComment() {
        // TODO add the possibility to nest them (chapter 4 challenge)
        if (!advanceAfter('*', '/')) {
            stdio.errorAtLine(line, "Unterminated multiline comment");
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private static final Map<String, TokenType> keywords;

    static {
        keywords = Map.ofEntries(
                entry("and", AND),
                entry("break", BREAK),
                entry("class", CLASS),
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
                entry("var", VAR),
                entry("while", WHILE)
        );
    }
}
