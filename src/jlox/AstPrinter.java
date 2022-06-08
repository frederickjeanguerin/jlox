package jlox;

import java.util.List;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {

    // TODO use StringBuilder all over instead of string concat (+)

    String print(Expr expr) {
        return expr.visit(this);
    }

    String print(List<Stmt> statements) {
        var sb = new StringBuilder();
        for (var stmt: statements) {
            sb.append(stmt.visit(this));
        }
        return sb.toString();
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme(), expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return Stdio.stringify(expr.value);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme(), expr.right);
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "(var " + expr.name.lexeme() + ")";
    }

    @Override
    public String visitTernaryExpr(Expr.Ternary expr) {
        return parenthesize(expr.leftOp.lexeme() + expr.rightOp.lexeme(), expr.left, expr.middle, expr.right);
    }

    private String parenthesize(String name, Expr... exprs) {
        var sb = new StringBuilder();
        sb.append('(').append(name);
        for (var expr: exprs) {
            sb.append(' ').append(expr.visit(this));
        }
        sb.append(')');
        return sb.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return stmt.expression.visit(this) + ";\n";
    }

    @Override
    public String visitPrintStmt(Stmt.Print stmt) {
        return "print " + stmt.expression.visit(this) + ";\n";
    }

    @Override
    public String visitVarStmt(Stmt.Var stmt) {
        return "var " + stmt.name.lexeme()
                + (stmt.initializer != null ? " = " + stmt.initializer.visit(this):"") + ";\n";
    }

    @Override
    public String visitLastStmt(Stmt.Last stmt) {
        return stmt.expression.visit(this);
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
        System.out.println(new AstPrinter().print(expr));
    }
}
