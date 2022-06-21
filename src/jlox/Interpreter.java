package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    static class TypeMismatchError extends LoxError {
        public TypeMismatchError(Token token, Class<?> expected, Object given) {
            this(token, expected, given, "");
        }
        public TypeMismatchError(Token token, Class<?> expected, Object given, String moreInfo) {
            super(token, "Type mismatch. Expected '%s' but got '%s'. %s"
                    .formatted(expected.getSimpleName(),
                            // TODO translate java types to lox types names
                            (given == null ? Void.class : given.getClass()).getSimpleName(), moreInfo));
        }
    }

    static class BreakException extends LoxError {
        public BreakException(Token token) {
            super(token);
        }
    }

    static class ContinueException extends LoxError {
        public ContinueException(Token token) {
            super(token);
        }
    }

    static class ReturnException extends LoxError {

        final Object value;
        public ReturnException(Token token, Object value) {
            super(token);
            this.value = value;
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


    public Stdio stdio = null;
    public final Environment environment = new Environment();


    public void interpret(List<Stmt> statements, Stdio stdio) {
        this.stdio = stdio;
        try {
            for (var stmt: statements) {
                execute(stmt);
            }
        } catch (LoxError error) {
            stdio.errorAtToken(error.token, error.getMessage());
        }
    }

    public void reset() {
        stdio = null;
        environment.reset();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block block) {
        executeBlock(block.statements);
        return null;
    }

    @Override
    public Void visitContinueCatcherStmt(Stmt.ContinueCatcher stmt) {
        try {
            execute(stmt.statement);
        } catch (ContinueException ignored) { }
        return null;
    }

    @Override
    public Void visitClassStmt(Stmt.Class klass) {
        Map<String, LoxCallable.Function> methods = new HashMap<>();
        List<LoxClass> superclasses = new ArrayList<>();
        for (var superclass : klass.superclasses) {
           superclasses.add(
                   downcast(evaluate(superclass), LoxClass.class, "class", superclass.name, "superclass"));
        }
        for (var method : klass.methods) {
            methods.put(method.name.lexeme(), new LoxCallable.Function(method, environment.getScoping()));
        }
        var loxClass = new LoxClass(klass.name.lexeme(), superclasses, methods, klass);
        environment.defineSymbol(klass.name, loxClass, Symbol.Type.CLASS);
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        var function = new LoxCallable.Function(stmt, environment.getScoping());
        environment.defineSymbol(stmt.name, function, Symbol.Type.FUN);
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
    public Void visitKeywordStmt(Stmt.Keyword stmt) {
        switch (stmt.keyword.type()) {
            // NB Exceptions are slow, and they could be triggered outside the body of a loop, e.g. a sub function.
            case BREAK -> throw new BreakException(stmt.keyword);
            case CONTINUE -> throw new ContinueException(stmt.keyword);
            default -> throw new IllegalStateException("Unexpected value: " + stmt.keyword.lexeme());
        }
        // return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object result = evaluate(stmt.expression);
        stdio.print(result);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        throw new ReturnException(stmt.keyword, stmt.value == null ? null : evaluate(stmt.value));
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        if (stmt.initializer == null) {
            environment.defineUninitializedVariable(stmt.name);
        } else {
            environment.defineSymbol(stmt.name, evaluate(stmt.initializer), Symbol.Type.VAR);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {

        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakException Ignored) { break; }
        }

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
        environment.assignSymbol(expr.name, result, expr.target);
        return result;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var left = evaluate(expr.left);
        var right = new Lazy(() -> evaluate(expr.right));

        Supplier<Double> leftNumber = () -> number(left, expr.operator, "left operand");
        Supplier<Double> rightNumber = () -> number(right.get(), expr.operator, "right operand");

        return switch (expr.operator.type()) {
            case AND -> isTruthy(left) ? right.get() : left;
            case BANG_EQUAL -> !areEqual(left, right.get());
            case COMMA -> right.get();
            case EQUAL_EQUAL -> areEqual(left, right.get());
            case GREATER -> leftNumber.get() > rightNumber.get();
            case GREATER_EQUAL -> leftNumber.get() >= rightNumber.get();
            case LESS -> leftNumber.get() < rightNumber.get();
            case LESS_EQUAL -> leftNumber.get() <= rightNumber.get();
            case MINUS -> leftNumber.get() - rightNumber.get();
            case OR -> isTruthy(left) ? left : right.get();
            case PERCENT -> {
                double divisor = rightNumber.get();
                if (divisor == 0)
                    throw new LoxError(expr.operator, "Division by zero.");
                yield leftNumber.get() % divisor;
            }
            case PLUS -> {
                if (left instanceof Double b && right.get() instanceof Double a )
                    yield a + b;
                // Challenge 7.2
                if (left instanceof String || right.get() instanceof String)
                    yield Stdio.stringify(left) + Stdio.stringify(right.get());
                throw new LoxError(expr.operator, "Operands cannot be added.");
            }
            case SLASH -> {
                // Challenge 7.3
                double divisor = rightNumber.get();
                if (divisor == 0)
                    throw new LoxError(expr.operator, "Division by zero.");
                yield leftNumber.get() / divisor;
            }
            case STAR -> leftNumber.get() * rightNumber.get();
            default -> throw new IllegalStateException("Unexpected value: " + expr.operator.type());
        };
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        if (callee instanceof  LoxCallable function) {
            // Check arity
            if (function.arity() != expr.arguments.size()) {
                throw new LoxError(expr.leftPar,
                        "Call expect %d arguments, but got %d.".formatted(function.arity(), expr.arguments.size()));
            }
            // Evaluate arguments
            List<Object> arguments = new ArrayList<>();
            for (var arg : expr.arguments) {
                arguments.add(evaluate(arg));
            }

            return function.call(this, expr.leftPar, arguments);
        }
        throw new LoxError(expr.leftPar, "Can only call function and classes");
    }

    @Override
    public Object visitGetExpr(Expr.Get get) {
        Object object = evaluate(get.object);
        if (object instanceof LoxInstance instance) {
            Object value = instance.get(get.name);
            if (value instanceof LoxCallable.Function function && function.isProperty) {
                return function.call(this, get.name, new ArrayList<>());
            }
            return value;
        }
        throw new LoxError(get.name, "Left side of '.%s' is not an instance".formatted(get.name.lexeme()));
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLambdaExpr(Expr.Lambda lambda) {
        return new LoxCallable.Function(lambda, environment.getScoping());
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitSetExpr(Expr.Set set) {
        if (evaluate(set.object) instanceof LoxInstance instance ) {
            return instance.set(set.name, evaluate(set.value));
        }
        throw new LoxError(set.name, "Left side of '.%s' is not an instance".formatted(set.name.lexeme()));
    }

    @Override
    public Object visitSuperExpr(Expr.Super expr) {
        var self = (LoxInstance) environment.getSymbol(Token.Special("self")).getValue(expr.keyword);
        return self.getSuper(expr.method, expr.targetClass, expr.explicitSuperclass != null);
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
        Object value =  environment.getSymbol(expr.name, expr.target).getValue(expr.name);
        if (value instanceof LoxCallable.Function function && function.isProperty) {
            return function.call(this, expr.name, new ArrayList<>());
        }
        return value;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        return isTruthy(evaluate(expr.left)) ? evaluate(expr.middle) : evaluate(expr.right);
    }

    @Override
    public Object visitTypeCheckExpr(Expr.TypeCheck expr) {
        var value = evaluate(expr.value);
        if (!expr.type.isInstance(value))
            throw new TypeMismatchError(expr.name, expr.type, value);
        return value;
    }

    void execute(Stmt stmt) {
        if (stmt != null)
            stmt.visit(this);
    }

    void executeBlock(List<Stmt> statements) {
        if (environment.hasNoLocalParameters())
            environment.push();
        try {
            for (var stmt : statements ) {
                execute(stmt);
            }
        } finally {
            if (environment.hasNoLocalParameters())
                environment.pop();
        }
    }

    Object evaluate(Expr expr) {
        return expr.visit(this);
    }

    private boolean isTruthy(Object obj) {
        return obj instanceof Boolean bool ? bool
                : obj instanceof Double d ? d != 0
                : obj instanceof String s ? !s.isEmpty()
                : obj != null;
    }

    private boolean areEqual(Object a, Object b) {
        return a == null && b == null || a != null && a.equals(b);
    }

    static private <T> T downcast(Object value, Class<T> type, String typeName, Token token, String position) {
        if (type.isInstance(value))
            //noinspection unchecked
            return (T) value;
        throw new LoxError(token, position + ": " +  typeName + " expected.");
    }

    static private double number(Object value, Token token, String position) {
        return downcast(value, Double.class, "number", token, position);
    }

    @SuppressWarnings("unused")
    static private String string(Object value, Token token, String position) {
        return downcast(value, String.class, "string", token, position);
    }

}
