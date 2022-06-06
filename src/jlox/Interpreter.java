package jlox;

public class Interpreter implements  Expr.Visitor<Object> {

    private static class RuntimeError extends RuntimeException {
        final Token token;

        public RuntimeError(Token token, String message) {
            super(message);
            this.token = token;
        }
    }

    public Object interpret(Expr expr, Error errors) {
        try {
            return evaluate(expr);
        } catch (RuntimeError error) {
            errors.atToken(error.token, error.getMessage());
        }
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var right = evaluate(expr.right);
        var left = evaluate(expr.left);
        return switch (expr.operator.type()) {
            case BANG_EQUAL -> !areEqual(left, right);
            case EQUAL_EQUAL -> areEqual(left, right);
            case GREATER -> (double) left > (double) right;
            case GREATER_EQUAL -> (double) left >= (double) right;
            case LESS -> (double) left < (double) right;
            case LESS_EQUAL -> (double) left <= (double) right;
            case MINUS -> (double) left - (double) right;
            case PLUS -> (double) left + (double) right;
            case SLASH -> (double) left / (double) right;
            case STAR -> (double) left * (double) right;
            default -> throw new IllegalStateException("Unexpected value: " + expr.operator.type());
        };
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        var right = evaluate(expr.right);
        return switch (expr.operator.type()) {
            case BANG -> !isTruthy(right);
            case MINUS -> -(double) right;
            default -> throw new IllegalStateException("Unexpected value: " + expr.operator.type());
        };
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        return null;
    }

    private Object evaluate(Expr expr) {
        return expr.visit(this);
    }

    private boolean isTruthy(Object obj) {
        return obj instanceof Boolean ? (boolean) obj : obj != null;
    }

    private boolean areEqual(Object a, Object b) {
        return a == null && b == null || a != null && a.equals(b);
    }
}
