package jlox;

import java.util.List;

import static jlox.TokenType.COMMA;

public class AstPrinter implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

    private final String EOL;
    private final StringBuilder sb = new StringBuilder();

    public AstPrinter() {
        this("\n");
    }

    public AstPrinter(String eol) {
        EOL = eol;
    }

    private void append(Expr expr) {
        expr.visit(this);
    }

    private void eol() {
        append(EOL);
    }

    private void eos() {
        append(';'); eol();
    }

    private void append(Stmt stmt) {
        if (stmt != null)
            stmt.visit(this);
        else
            append(';');
    }

    private void append(List<Stmt> statements) {
        for (var stmt: statements) {
            append(stmt);
        }
    }

    private void append(String string) {
        sb.append(string);
    }

    private void append(char c) {
        sb.append(c);
    }

    public String print(List<Stmt> statements) {
        sb.setLength(0);
        append(statements);
        return sb.toString();
    }

    @Override
    public Void visitAssignExpr(Expr.Assign expr) {
        append("(= ");append(expr.name.lexeme());append(" ");
        append(expr.value);append(")");
        return null;
    }

    @Override
    public Void visitBinaryExpr(Expr.Binary expr) {
        if (expr.operator.type() == COMMA) {
            append(expr.left); append(", "); append(expr.right);
        } else {
            parenthesize(expr.operator.lexeme(), expr.left, expr.right);
        }
        return null;
    }

    @Override
    public Void visitCallExpr(Expr.Call expr) {
        append(expr.callee); append("(");
        for (var arg : expr.arguments) {
            if (arg != expr.arguments.get(0)) append(", ");
            append(arg);
        }
        append(')');
        return null;
    }

    @Override
    public Void visitGroupingExpr(Expr.Grouping expr) {
        parenthesize("group", expr.expression);
        // append('('); append(expr.expression); append(')');
        return null;
    }

    @Override
    public Void visitLiteralExpr(Expr.Literal expr) {
        append(Stdio.stringify(expr.value));
        return null;
    }

    @Override
    public Void visitUnaryExpr(Expr.Unary expr) {
        parenthesize(expr.operator.lexeme(), expr.right);
        return null;
    }

    @Override
    public Void visitVariableExpr(Expr.Variable expr) {
        append(expr.name.lexeme());
        return null;
    }

    @Override
    public Void visitTernaryExpr(Expr.Ternary expr) {
        parenthesize(expr.leftOp.lexeme() + expr.rightOp.lexeme(), expr.left, expr.middle, expr.right);
        return null;
    }

    @Override
    public Void visitTypeCheckExpr(Expr.TypeCheck expr) {
        parenthesize(expr.type.getSimpleName()); append(expr.value);
        return null;
    }

    private void parenthesize(String name, Expr... exprs) {
        append('('); append(name);
        for (var expr: exprs) {
            append(' '); append(expr);
        }
        append(')');
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        append('{'); eol();
        append(stmt.statements);
        append('}'); eol();
        return null;
    }

    @Override
    public Void visitContinueCatcherStmt(Stmt.ContinueCatcher stmt) {
        append("{ CC: "); append(stmt.statement); append("}");
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        append(stmt.expression); eos();
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        append("fun "); append(stmt.name.lexeme()); append("(");
        for (var token : stmt.parameters) {
            if (token != stmt.parameters.get(0)) append(", ");
            append(token.lexeme());
        }
        append (") "); append(new Stmt.Block(stmt.body));
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        append("if ("); append(stmt.condition); append(") "); append(stmt.then);
        if (stmt.else_ != null) {
            append("else "); append(stmt.else_);
        }
        return null;
    }

    @Override
    public Void visitKeywordStmt(Stmt.Keyword stmt) {
        append(stmt.keyword.lexeme()); eos();
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        append("print "); append(stmt.expression); eos();
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        append("var "); append(stmt.name.lexeme());
        if (stmt.initializer != null) {
            append(" = "); append(stmt.initializer);
        }
        eos();
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        append("while ("); append(stmt.condition); append(") "); append(stmt.body);
        return null;
    }

    @Override
    public Void visitLastStmt(Stmt.Last stmt) {
        append(stmt.expression); append(' ');
        return null;
    }

    public static void main(String[] args) {
        Expr expr = new Expr.Binary(
                new Expr.Unary(
                        new Token(TokenType.MINUS, "-", null, 1),
                        new Expr.Literal(123)),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Literal(45.67))
        );
        System.out.println(new AstPrinter().print(List.of(new Stmt.Expression(expr))));
    }
}
