package jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static jlox.TokenType.*;

@SuppressWarnings("ThrowableNotThrown")
public class Parser {

    private static class ParseError extends RuntimeException {}

    private final Stdio stdio;
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens, Stdio stdio) {
        this.tokens = tokens;
        this.stdio = stdio;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            var decl = declaration();
            if (decl != null)
                statements.add(decl);
        }
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(VAR))
                return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        var name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }
        semicolon();
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(SEMICOLON)) return null;
        if (match(PRINT)) return printStatement();
        return expressionStatement();
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if (isAtEnd())
            return new Stmt.Last(expr);
        semicolon();
        return new Stmt.Expression(expr);
    }

    private Stmt printStatement() {
        Expr expr = expression();
        semicolon();
        return new Stmt.Print(expr);
    }

    private void semicolon() {
        consume(SEMICOLON, "Expect ';' at end of statement.");
    }

    private Expr expression() {
        return comma();
    }

    // Challenge 1, Chap 6 : C comma expression
    private Expr comma() {
        return binary(this::assignment, COMMA);
    }

    private Expr assignment() {
        Expr expr = ternary();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment(); // right-associative
            if (expr instanceof Expr.Variable var) {
                Token name = var.name;
                return new Expr.Assign(name, value);
            }
            error(equals, "Invalid assignment target (asa lvalue).");
        }
        return expr;
    }

    // Challenge 2, Chap 6 : Ternary
    private Expr ternary() {
        Expr expr = equality();
        if (match(QUESTION)) {
            Token leftOp = previous();
            Expr middle = equality();
            Token rightOp = consume(COLON, "Expect ':' in ternary.");
            Expr right = ternary();
            expr =  new Expr.Ternary(expr, leftOp, middle, rightOp, right);
        }
        return expr;
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
        return primaryOrError();
    }

    // Challenge 3, Chap 6 : Error production for missing first operand
    private Expr primaryOrError() {
        if (peek(QUESTION, BANG_EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, PLUS, SLASH, STAR)) {
            error(peek(), "Expect first operand (expression) before operator.");
            insert(new Token(ERROR, peek().lexeme(), peek().literal(), peek().line()));
            return ternary(); // retry to parse with added dummy first operand
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);
        if (match(ERROR)) return new Expr.Literal("#Error");
        if (match(NUMBER, STRING)) return new Expr.Literal(previous().literal());
        if (match(IDENTIFIER)) return new Expr.Variable(previous());
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
        if (peek(types)) {
            advance();
            return true;
        }
        return false;
    }

    @SuppressWarnings({"UnusedReturnValue", "SameParameterValue"})
    private Token consume(TokenType type, String message) {
        if (peek(type)) return advance();
        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        stdio.errorAtToken(token, message);
        return new ParseError();
    }

    private void synchronize() {
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

    private void insert(Token token) {
        tokens.add(current, token);
    }

    private boolean peek(TokenType... types) {
        if (isAtEnd()) return false;
        for (var type : types) {
            if (peek().type() == type) return true;
        }
        return false;
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
