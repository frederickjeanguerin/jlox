package jlox;

import java.util.List;

import static jlox.TokenType.COMMA;

public class AstPrinter implements Expr.VoidVisitor, Stmt.VoidVisitor {

    private final String EOL;
    private final StringBuilder sb = new StringBuilder();

    public AstPrinter() {
        this("\n");
    }

    public AstPrinter(String eol) {
        EOL = eol;
    }

    private void append(Expr expr) {
        expr.voidVisit(this);
    }

    private void eol() {
        append(EOL);
    }

    private void eos() {
        append(';'); eol();
    }

    private void append(Stmt stmt) {
        if (stmt != null)
            stmt.voidVisit(this);
        else
            append(';');
    }

    private void append(List<Stmt> statements) {
        for (var stmt: statements) {
            append(stmt);
        }
    }

    private void append(Token token) {
        append(token.lexeme());
    }

    private void append(String string) {
        sb.append(string);
    }

    private void append(char c) {
        sb.append(c);
    }

    private void trimend() {
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
            sb.setLength(sb.length() - 1);
        }
    }

    public String print(List<Stmt> statements) {
        sb.setLength(0);
        append(statements);
        trimend();
        return sb.toString();
    }

    public String print(Stmt statement) {
        sb.setLength(0);
        append(statement);
        trimend();
        return sb.toString();
    }

    @Override
    public void visitAssignExpr(Expr.Assign expr) {
        append("(= ");append(expr.name.lexeme());append(" ");
        append(expr.value);append(")");
    }

    @Override
    public void visitBinaryExpr(Expr.Binary expr) {
        if (expr.operator.type() == COMMA) {
            append(expr.left); append(", "); append(expr.right);
        } else {
            parenthesize(expr.operator.lexeme(), expr.left, expr.right);
        }
    }

    @Override
    public void visitCallExpr(Expr.Call expr) {
        append(expr.callee); append("(");
        for (var arg : expr.arguments) {
            if (arg != expr.arguments.get(0)) append(", ");
            append(arg);
        }
        append(')');
    }

    @Override
    public void visitGroupingExpr(Expr.Grouping expr) {
        parenthesize("group", expr.expression);
        // append('('); append(expr.expression); append(')');
    }

    @Override
    public void visitLambdaExpr(Expr.Lambda lambda) {
        append("fun ");
        append("(");
        for (var token : lambda.parameters) {
            if (token != lambda.parameters.get(0)) append(", ");
            append(token.lexeme());
        }
        append (") "); append(lambda.body);
    }

    @Override
    public void visitLiteralExpr(Expr.Literal expr) {
        append(Stdio.stringify(expr.value));
    }

    @Override
    public void visitUnaryExpr(Expr.Unary expr) {
        parenthesize(expr.operator.lexeme(), expr.right);
    }

    @Override
    public void visitVariableExpr(Expr.Variable expr) {
        append(expr.name.lexeme());
    }

    @Override
    public void visitTernaryExpr(Expr.Ternary expr) {
        parenthesize(expr.leftOp.lexeme() + expr.rightOp.lexeme(), expr.left, expr.middle, expr.right);
    }

    @Override
    public void visitTypeCheckExpr(Expr.TypeCheck expr) {
        parenthesize(expr.type.getSimpleName()); append(expr.value);
    }

    private void parenthesize(String name, Expr... exprs) {
        append('('); append(name);
        for (var expr: exprs) {
            append(' '); append(expr);
        }
        append(')');
    }

    @Override
    public void visitBlockStmt(Stmt.Block stmt) {
        append('{'); eol();
        append(stmt.statements);
        append('}'); eol();
    }

    @Override
    public void visitContinueCatcherStmt(Stmt.ContinueCatcher stmt) {
        append("{ CC: "); append(stmt.statement); append("}");
    }

    @Override
    public void visitClassStmt(Stmt.Class stmt) {
        append("class "); append(stmt.name); append(" {"); eol();
        for(var method : stmt.methods) {
            visitFunctionStmt(method);
        }
        append("}"); eol();
    }

    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        append(stmt.expression); eos();
    }

    @Override
    public void visitFunctionStmt(Stmt.Function stmt) {
        append("fun "); append(stmt.name.lexeme());
        append("(");
        for (var token : stmt.parameters) {
            if (token != stmt.parameters.get(0)) append(", ");
            append(token.lexeme());
        }
        append (") "); append(stmt.body);
    }

    @Override
    public void visitIfStmt(Stmt.If stmt) {
        append("if ("); append(stmt.condition); append(") "); append(stmt.then);
        if (stmt.else_ != null) {
            append("else "); append(stmt.else_);
        }
    }

    @Override
    public void visitKeywordStmt(Stmt.Keyword stmt) {
        append(stmt.keyword.lexeme()); eos();
    }

    @Override
    public void visitPrintStmt(Stmt.Print stmt) {
        append("print "); append(stmt.expression); eos();
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        append("return");
        if (stmt.value != null) {
            append(" ");
            append(stmt.value);
        }
        eos();
    }

    @Override
    public void visitVarStmt(Stmt.Var stmt) {
        append("var "); append(stmt.name.lexeme());
        if (stmt.initializer != null) {
            append(" = "); append(stmt.initializer);
        }
        eos();
    }

    @Override
    public void visitWhileStmt(Stmt.While stmt) {
        append("while ("); append(stmt.condition); append(") "); append(stmt.body);
    }

    @Override
    public void visitLastStmt(Stmt.Last stmt) {
        append(stmt.expression); append(' ');
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
