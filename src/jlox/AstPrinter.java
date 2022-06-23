package jlox;

import java.util.List;

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

    private void space() {
        append(' ');
    }

    private void append(Stmt stmt) {
        if (stmt != null)
            stmt.voidVisit(this);
        else
            append(';');
    }

    private void append(List<? extends Stmt> statements) {
        for (var stmt: statements) {
            append(stmt);
        }
    }

    private void append(Token token) {
        append(token.lexeme());
    }

    private void spaced(Token token) {
        space();
        append(token.lexeme());
        space();
    }

    private void append(String string) {
        sb.append(string);
    }

    private void append(char c) {
        sb.append(c);
    }

    private void trimEnd() {
        while (sb.length() > 0 && sb.charAt(sb.length() - 1) == ' ') {
            sb.setLength(sb.length() - 1);
        }
    }

    public String print(List<Stmt> statements) {
        sb.setLength(0);
        append(statements);
        trimEnd();
        return sb.toString();
    }

    public String print(Stmt statement) {
        sb.setLength(0);
        append(statement);
        trimEnd();
        return sb.toString();
    }

    public String print(Expr expr) {
        sb.setLength(0);
        append(expr);
        trimEnd();
        return sb.toString();
    }

    @Override
    public void visitAssignExpr(Expr.Assign expr) {
        append(expr.name.lexeme());append(" = ");append(expr.value);
    }

    @Override
    public void visitBinaryExpr(Expr.Binary expr) {
        append(expr.left); spaced(expr.operator); append(expr.right);
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
    public void visitGetExpr(Expr.Get get) {
        append(get.object); append('.'); append(get.name);
    }

    @Override
    public void visitGroupingExpr(Expr.Grouping expr) {
        append('('); append(expr.expression); append(')');
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
        append(stringify(expr.value));
    }

    public static String stringify(Object value) {
        return value instanceof String str ? escape(str) : Stdio.stringify(value);
    }

    public static String escape(String str) {
        var sb = new StringBuilder();
        sb.append('\"');
        str.chars().forEach(c -> {
            switch(c) {
                case '\n', '\t', '\"', '\\' -> sb.append('\\');
            }
            sb.append((char)c);
        });
        sb.append('\"');
        return sb.toString();
    }

    @Override
    public void visitSetExpr(Expr.Set set) {
        append(set.object); append('.'); append(set.name); append(" = "); append(set.value);
    }

    @Override
    public void visitSuperExpr(Expr.Super expr) {
        append(expr.keyword);
        if (expr.explicitSuperclass != null) {
            append('('); append(expr.explicitSuperclass); append(')');
        }
        append('.'); append(expr.method);
    }

    @Override
    public void visitUnaryExpr(Expr.Unary expr) {
        append(expr.operator);
        if (expr.right.getClass() == Expr.Unary.class) {
            space();
        }
        append(expr.right);
    }

    @Override
    public void visitVariableExpr(Expr.Variable expr) {
        append(expr.name.lexeme());
    }

    @Override
    public void visitTernaryExpr(Expr.Ternary expr) {
        append(expr.left); spaced(expr.leftOp); append(expr.middle); spaced(expr.rightOp); append(expr.right);
    }

    @Override
    public void visitTypeCheckExpr(Expr.TypeCheck expr) {
        append("/* check type: "); append(expr.type.getSimpleName()); append(" */"); append(expr.value);
    }

    @Override
    public void visitBlockStmt(Stmt.Block stmt) {
        append('{'); eol();
        append(stmt.statements);
        append('}'); eol();
    }

    @Override
    public void visitForBlockStmt(Stmt.ForBlock stmt) {
        append("/* ForBlock */ { "); append(stmt.body); append(stmt.updater); append('}');
    }

    @Override
    public void visitClassStmt(Stmt.Class stmt) {
        append("class "); append(stmt.name);
        if (!stmt.superclasses.isEmpty()) {
            append(" < "); append(stmt.superclasses.get(0).name);
            stmt.superclasses.stream().skip(1).forEach( superclass -> {
                append(", "); append(superclass.name);
            });
        }
        append(" {"); eol();
        append(stmt.classMethods);
        append(stmt.methods);
        append("}"); eol();
    }

    @Override
    public void visitMethodsStmt(Stmt.Methods stmt) {
        append(stmt.methods);
    }

    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        append(stmt.expression); eos();
    }

    @Override
    public void visitFunctionStmt(Stmt.Function stmt) {
        if (stmt.isClass)
            append("class ");
        if (!stmt.kind.equals("method"))
            append("fun ");
        append(stmt.name);
        append("(");
        for (var token : stmt.parameters) {
            if (token != stmt.parameters.get(0)) append(", ");
            append(token);
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
        append(stmt.keyword); eos();
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
        append(stmt.expression); space();
    }
}
