package jlox;

import java.util.List;
import java.util.function.Supplier;

import static jlox.TokenType.*;

class Parser {

    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        return comma();
    }

    // Challenge 1, Chap 6 : C comma expression
    private Expr comma() {
        return binary(this::equality, COMMA);
    }

    private Expr equality() {
        return binary(this::comparison, BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expr comparison() {
        return binary(this::term, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expr term() {
        return binary(this::factor, MINUS, PLUS);
    }

    private Expr factor() {
        return binary(this::unary, SLASH, STAR);
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal());
        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Expr binary(Supplier<Expr> supplier, TokenType... types) {
        Expr expr = supplier.get();
        while (match(types)) {
            Token operator = previous();
            Expr right = supplier.get();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private boolean match(TokenType... types) {
        for (var type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    @SuppressWarnings("unused")
    private void Synchronize() {
        advance();
        while (!isAtEnd()) {
            if (previous().type() == SEMICOLON) return;

            switch(peek().type()) {
                case CLASS:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }
            advance();
        }
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type() == type;
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
