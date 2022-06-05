package jlox;

public class AstPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.visit(this);
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
        if (expr.value == null)
            return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme(), expr.right);
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