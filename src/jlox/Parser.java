package jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static jlox.TokenType.*;
import static jlox.TokenType.AND;

@SuppressWarnings({"ThrowableNotThrown", "UnnecessaryLocalVariable"})
public class Parser {

    enum ExprType {
        COMMA,
        ASSIGNMENT,
        LAMBDA,
        TERNARY,
        OR,
        AND,
        EQUALITY,
        COMPARISON,
        TERM,
        FACTOR,
        UNARY,
        CALL,
        PRIMARY_OR_ERROR,
        PRIMARY,
    }

    private static class ParseError extends RuntimeException {}

    private final Stdio stdio;
    private final List<Token> tokens;
    private int current = 0;

    // TODO push/pop loopNesting inside function body
    private int loopNesting = 0;

    // TODO what happens inside functions ?
    private boolean hasContinue = false;

    Parser(List<Token> tokens, Stdio stdio) {
        this.tokens = tokens;
        this.stdio = stdio;

        // Order expressions by precedence

    }

    private ExprType next(ExprType type) {
        return ExprType.values()[type.ordinal() + 1];
    }

    private Supplier<Expr> nextExpr(ExprType type) {
        return supplierOf(next(type));
    }

    private Supplier<Expr> supplierOf(ExprType type) {
        return switch(type) {
            case COMMA -> this::comma;
            case ASSIGNMENT -> this::assignment;
            case LAMBDA -> this::lambda;
            case TERNARY -> this::ternary;
            case OR -> this::or;
            case AND -> this::and;
            case EQUALITY -> this::equality;
            case COMPARISON -> this::comparison;
            case TERM -> this::term;
            case FACTOR -> this::factor;
            case UNARY -> this::unary;
            case CALL -> this::call;
            case PRIMARY_OR_ERROR -> this::primaryOrError;
            case PRIMARY -> this::primary;
        };
    }

    List<Stmt> parse() {

        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            var decl = declaration();
            if (decl != null)
                statements.add(decl);
        }
        assert loopNesting == 0;
        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return funDeclaration("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");
        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!peek(RIGHT_BRACE) && !isAtEnd() ) {
            methods.add(funDeclaration("method"));
        }
        consume(RIGHT_BRACE, "Expect '}' after class body.");
        return new Stmt.Class(name, methods);
    }

    @SuppressWarnings("SameParameterValue")
    private Stmt.Function funDeclaration(String kind) {
        var name = consume(IDENTIFIER, "Expect %s name".formatted(kind));
        return new Stmt.Function(name, parameters(kind), body(false));
    }

    private List<Token> parameters(String kind) {
        consume(LEFT_PAREN, "Expect '(' to introduce %s parameters".formatted(kind));
        List<Token> params = new ArrayList<>();
        if (!peek(RIGHT_PAREN)) {
            do {
                if (params.size() >= 255) {
                    error(peek(), "%s can't have more than 255 parameters".formatted(kind));
                }
                var parameter = consume(IDENTIFIER, "Expect parameter name");
                params.add(parameter);
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters");
        return params;
    }

    private Stmt body(boolean isExpr) {
        return peek(LEFT_BRACE)
                ? statement()
                : isExpr
                    ? new Stmt.Expression(lambda())
                    : expressionStatement();
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
        if (match(BREAK, CONTINUE)) return keywordStatement();
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(SEMICOLON)) return null;
        if (match(WHILE)) return whileStatement();
        return expressionStatement();
    }

    private Stmt returnStatement() {
        // TODO check if return in function body
        // TODO check for unreachable code
        Token token = previous();
        Expr value = peek(SEMICOLON)? null : expression();
        semicolon();
        return new Stmt.Return(token, value);
    }

    private Stmt keywordStatement() {
        var token = previous();
        semicolon();

        switch (token.type()) {
            case CONTINUE:
                hasContinue = true;
                // fall-through
            case BREAK:
                if (loopNesting == 0)
                    error(token, token.lexeme() + " outside of any loop");
                break;
        }

        return new Stmt.Keyword(token);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'");

        Stmt initializer = null;
        if (match(VAR))
            initializer = varDeclaration();
        else if (!peek(SEMICOLON, RIGHT_PAREN))
            initializer = expressionStatement();
        else
            consume(SEMICOLON, "Expect ';' or initializer");

        Expr condition = null;
        if (!peek(SEMICOLON, RIGHT_PAREN))
            condition = expression();
        consume(SEMICOLON, "Expect condition or ';' after condition.");

        Stmt updater = null;
        if (!peek(SEMICOLON, RIGHT_PAREN))
            updater = new Stmt.Expression(expression());
        consume(RIGHT_PAREN, "Expect ')' after for loop updater");

        Stmt body = loopBody();

        // ----- Desugaring phase -----

        /*
                for (initializer; condition; updater) body;

            is equivalent to:

                {
                    initializer;
                    while (condition) { body; updater; }
                }

            We progress backward: body, updater, condition, initializer.
         */

        var whileBody
                = updater == null ? body
                : body == null ? updater
                : new Stmt.Block(List.of(body, updater));

        if (condition == null)
            condition = new Expr.Literal(true);
        var whileStmt = new Stmt.While(condition, whileBody);

        var forStmt = initializer == null ? whileStmt
            : new Stmt.Block(List.of(initializer, whileStmt));

        return forStmt;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'");
        var condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition");
        var body = loopBody();
        return new Stmt.While(condition, body);
    }

    private Stmt loopBody() {
        loopNesting++;
        try {
            var body = statement();
            if (hasContinue) {
                hasContinue = false;
                body = new Stmt.ContinueCatcher(body);
            }
            return body;
        } finally {
            loopNesting--;
        }
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'");
        var condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition");
        var then = statement();
        Stmt else_ = null;
        if (match(ELSE)) {
            else_ = statement();
        }
        return new Stmt.If(condition, then, else_);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();
        while (!peek(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expect '}' after block");
        return statements;
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        if (isAtEnd() || peek(RIGHT_BRACE))
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
        return binary(nextExpr(ExprType.COMMA), COMMA);
    }

    private Expr assignment() {
        Expr expr = nextExpr(ExprType.ASSIGNMENT).get();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment(); // right-associative
            if (expr instanceof Expr.Variable var) {
                Token name = var.name;
                return new Expr.Assign(name, value);
            }
            if (expr instanceof  Expr.Get get) {
                return new Expr.Set(get.object, get.name, value);
            }
            error(equals, "Invalid assignment target (asa lvalue).");
        }
        return expr;
    }

    private Expr lambda() {
        if (match(FUN)) {
            return new Expr.Lambda(parameters("lambda"), body(true));
        }
        return nextExpr(ExprType.LAMBDA).get();
    }

    // Challenge 2, Chap 6 : Ternary
    private Expr ternary() {
        Expr expr = nextExpr(ExprType.TERNARY).get();
        if (match(QUESTION)) {
            Token leftOp = previous();
            Expr middle = nextExpr(ExprType.TERNARY).get();
            Token rightOp = consume(COLON, "Expect ':' in ternary.");
            Expr right = ternary();
            expr =  new Expr.Ternary(expr, leftOp, middle, rightOp, right);
        }
        return expr;
    }

    private Expr or() {
        return binary(nextExpr(ExprType.OR), OR);
    }

    private Expr and() {
        return binary(nextExpr(ExprType.AND), AND);
    }

    private Expr equality() {
        return binary(nextExpr(ExprType.EQUALITY), BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expr comparison() {
        return binary(nextExpr(ExprType.COMPARISON), GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expr term() {
        return binary(nextExpr(ExprType.TERM), MINUS, PLUS);
    }

    private Expr factor() {
        return binary(nextExpr(ExprType.FACTOR), SLASH, STAR, PERCENT);
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        // from ++i, transform to: i = i + 1
        if (match(PLUS_PLUS, MINUS_MINUS)) {
            Token operator = previous();
            Expr lvalue = unary();
            if (lvalue instanceof Expr.Variable var) {
                Token name = var.name;
                Token pseudoOperator = new Token(operator.type() == PLUS_PLUS ? PLUS : MINUS,
                        operator.lexeme().substring(1), null, operator.line());
                Expr value = new Expr.Binary(
                        new Expr.TypeCheck(var, Double.class, name),
                        pseudoOperator,
                        new Expr.Literal(1.0));
                return new Expr.Assign(name, value);
            }
            error(operator, "Invalid increment/decrement target.");
        }
        return nextExpr(ExprType.UNARY).get();
    }

    private Expr call() {
        Expr expr = nextExpr(ExprType.CALL).get();

        while (true) {
            if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                expr = new Expr.Get(expr, name);
            } else if (match(LEFT_PAREN)) {
                expr = finishCall(expr, previous());
            } else {
                break;
            }
        }

        return expr;
    }

    private Expr finishCall(Expr callee, Token leftPar) {
        List<Expr> arguments = new ArrayList<>();
        if (!peek(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Function call can't have more than 255 arguments");
                }
                // Beware: if we go for expression(), then we will match comma expression...
                arguments.add(nextExpr(ExprType.COMMA).get());
            } while (match(COMMA));
        }
        Token rightPar = consume(RIGHT_PAREN, "Expect ')' after arguments");
        return new Expr.Call(callee, leftPar, arguments, rightPar);
    }

    // Challenge 3, Chap 6 : Error production for missing first operand
    private Expr primaryOrError() {
        if (peek(QUESTION, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL,
                LESS, LESS_EQUAL, PLUS, PERCENT, SLASH, STAR)) {
            error(peek(), "Expect first operand (expression) before operator.");
            insert(new Token(ERROR, peek().lexeme(), peek().literal(), peek().line()));
            return nextExpr(ExprType.COMMA).get(); // retry to parse with added dummy first operand
        }
        return nextExpr(ExprType.PRIMARY_OR_ERROR).get();
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
            if (previous().type() == SEMICOLON || previous().type() == RIGHT_BRACE) return;

            switch(peek().type()) {
                case CLASS:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                case LEFT_BRACE:
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
