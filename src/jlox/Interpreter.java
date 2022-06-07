package jlox;

import java.util.function.Supplier;

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
        var left = evaluate(expr.left);
        var right = evaluate(expr.right);

        Supplier<Double> leftNumber = () -> number(left, expr.operator, "left operand");
        Supplier<Double> rightNumber = () -> number(right, expr.operator, "right operand");

        return switch (expr.operator.type()) {
            case BANG_EQUAL -> !areEqual(left, right);
            case COMMA -> right;
            case EQUAL_EQUAL -> areEqual(left, right);
            case GREATER -> leftNumber.get() > rightNumber.get();
            case GREATER_EQUAL -> leftNumber.get() >= rightNumber.get();
            case LESS -> leftNumber.get() < rightNumber.get();
            case LESS_EQUAL -> leftNumber.get() <= rightNumber.get();
            case MINUS -> leftNumber.get() - rightNumber.get();
            case PLUS -> {
                if (right instanceof Double && left instanceof Double)
                    yield (double) right + (double) left;
                // Challenge 7.2
                if (right instanceof String || left instanceof String)
                    yield Lox.stringify(left) + Lox.stringify(right);
                throw new RuntimeError(expr.operator, "Operands cannot be added.");
            }
            case SLASH -> {
                // Challenge 7.3
                double divisor = rightNumber.get();
                if (divisor == 0)
                    throw new RuntimeError(expr.operator, "Division by zero.");
                yield leftNumber.get() / divisor;
            }
            case STAR -> leftNumber.get() * rightNumber.get();
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

        Supplier<Double> rightNumber = () -> number(right, expr.operator, "right operand");

        return switch (expr.operator.type()) {
            case BANG -> !isTruthy(right);
            case MINUS -> - rightNumber.get();
            default -> throw new IllegalStateException("Unexpected value: " + expr.operator.type());
        };
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        return isTruthy(evaluate(expr.left)) ? evaluate(expr.middle) : evaluate(expr.right);
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

    static private <T> T downcast(Object value, Class<T> type, String typeName, Token token, String position) {
        if (type.isInstance(value))
            //noinspection unchecked
            return (T) value;
        throw new RuntimeError(token, position + ": " +  typeName + " expected.");
    }

    static private double number(Object value, Token token, String position) {
        return downcast(value, Double.class, "number", token, position);
    }

    @SuppressWarnings("unused")
    static private String string(Object value, Token token, String position) {
        return downcast(value, String.class, "string", token, position);
    }
}
