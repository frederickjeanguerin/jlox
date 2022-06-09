package jlox;

import java.util.List;
import java.util.function.Supplier;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    static class RuntimeError extends RuntimeException {

        final Token token;
        public RuntimeError(Token token, String message) {
            super(message);
            this.token = token;
        }
    }

    static class Lazy {
        private static final Object Uninitialized = new Object();
        private final Supplier<Object> supplier;
        private Object value = Uninitialized;

        Lazy(Supplier<Object> supplier) {
            this.supplier = supplier;
        }

        Object get() {
            if (value == Uninitialized)
                value = supplier.get();
            return value;
        }
    }

    private static final Object UNINITIALIZED = new Object();
    public final Stdio stdio = new Stdio();
    public final Environment environment = new Environment();


    public void interpret(List<Stmt> statements) {
        stdio.reset();
        try {
            for (var stmt: statements) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            stdio.errorAtToken(error.token, error.getMessage());
        }
    }

    public void reset() {
        stdio.reset();
        environment.reset();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block block) {
        executeBlock(block.statements);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition)))
            execute(stmt.then);
        else if (stmt.else_ != null)
            execute(stmt.else_);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object result = evaluate(stmt.expression);
        stdio.print(result);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = UNINITIALIZED;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.defineVar(stmt.name.lexeme(), value);
        return null;
    }

    @Override
    public Void visitLastStmt(Stmt.Last stmt) {
        Object result = evaluate(stmt.expression);
        stdio.print(result);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object result = evaluate(expr.value);
        environment.assignVar(expr.name, result);
        return result;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var left = evaluate(expr.left);
        var right = new Lazy(() -> evaluate(expr.right));

        Supplier<Double> leftNumber = () -> number(left, expr.operator, "left operand");
        Supplier<Double> rightNumber = () -> number(right.get(), expr.operator, "right operand");

        return switch (expr.operator.type()) {
            case BANG_EQUAL -> !areEqual(left, right.get());
            case COMMA -> right.get();
            case EQUAL_EQUAL -> areEqual(left, right.get());
            case GREATER -> leftNumber.get() > rightNumber.get();
            case GREATER_EQUAL -> leftNumber.get() >= rightNumber.get();
            case LESS -> leftNumber.get() < rightNumber.get();
            case LESS_EQUAL -> leftNumber.get() <= rightNumber.get();
            case MINUS -> leftNumber.get() - rightNumber.get();
            case PLUS -> {
                if (left instanceof Double b && right.get() instanceof Double a )
                    yield a + b;
                // Challenge 7.2
                if (left instanceof String || right.get() instanceof String)
                    yield Stdio.stringify(left) + Stdio.stringify(right);
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
    public Object visitVariableExpr(Expr.Variable expr) {
        var value = environment.getVar(expr.name);
        if (value == UNINITIALIZED) {
            throw new RuntimeError(expr.name, "Uninitialized variable '%s'".formatted(expr.name.lexeme()));
        }
        return value;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        return isTruthy(evaluate(expr.left)) ? evaluate(expr.middle) : evaluate(expr.right);
    }

    private void execute(Stmt stmt) {
        stmt.visit(this);
    }

    private void executeBlock(List<Stmt> statements) {
        environment.push();
        try {
            for (var stmt : statements ) {
                execute(stmt);
            }
        } finally {
            environment.pop();
        }
    }

    private Object evaluate(Expr expr) {
        return expr.visit(this);
    }

    private boolean isTruthy(Object obj) {
        return obj instanceof Boolean bool ? bool
                : obj instanceof Double d ? d != 0
                : obj != null;
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
