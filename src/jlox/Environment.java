package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static jlox.Environment.Result.*;

public class Environment {

    private Scope scope;
    private final Stack<Scope> swapped = new Stack<>();

    Environment() {
        reset();
    }

    void reset() {
        scope = new Scope(Scope.GLOBAL, false);
    }

    void push() {
        scope = new Scope(scope, false);
    }

    void pop() {
        scope = scope.outer;
        assert scope.outer != null; // We don't want to pop to the global scope!
    }

    Scoping getScoping() { return new Scoping(scope); }

    void swap(Scoping scoping) {
        swapped.push(scope);
        scope = scoping.scope;
    }

    void unswap() {
        scope = swapped.pop();
    }

    void defineSymbol(String name, Object value) {
        // NB Symbol can be redefined without error
        scope.define(name, value);
    }

    Object getSymbol(Token symbol) {
        var name = symbol.lexeme();
        var value = scope.get(name);
        if (value == NOT_FOUND) {
            throw new Interpreter.RuntimeError(symbol, "Undefined symbol '%s'.".formatted(name));
        }
        return value;
    }

    void assignSymbol(Token symbol, Object value) {
        var name = symbol.lexeme();
        switch (scope.modify(name, value)) {
            case MODIFIED -> {} // OK
            case NOT_FOUND -> throw new Interpreter.RuntimeError(symbol, "Undefined symbol '%s'.".formatted(name));
            case READ_ONLY -> throw new Interpreter.RuntimeError(symbol, "Readonly symbol '%s'.".formatted(name));
            default -> throw new IllegalStateException("Unexpected value when assigning '%s'.".formatted(name));
        }
    }

    static class Scoping {
        private final Scope scope;

        Scoping(Scope scope) {
            this.scope = scope;
        }
    }

    enum Result { NOT_FOUND, READ_ONLY, MODIFIED }

    private static class Scope {
        private final Map<String, Object> symbols = new HashMap<>();
        private final Scope outer;
        private final boolean readonly;

        private Scope(Scope outer, boolean readonly) {
            this.outer = outer;
            this.readonly = readonly;
        }

        void define(String name, Object value) {
            symbols.put(name, value);
        }

        Object get(String name) {
            if (symbols.containsKey(name)) {
                return symbols.get(name);
            }
            if (outer != null) {
                return outer.get(name);
            }
            return NOT_FOUND;
        }

        Result modify(String name, Object value) {
            if (symbols.containsKey(name)) {
                if (readonly) return READ_ONLY;
                symbols.put(name, value);
                return MODIFIED;
            }
            if (outer != null) {
                return outer.modify(name, value);
            }
            return NOT_FOUND;
        }

        static final Scope GLOBAL = new Scope(null, true);

        static {
            GLOBAL.define("clock", new Interpreter.Callable(){

                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                    return System.currentTimeMillis()/1000.0;
                }

                @Override
                public String toString() { return "<native fun: clock>"; }

            });
            GLOBAL.define("lineSeparator", new Interpreter.Callable(){

                @Override
                public int arity() {
                    return 0;
                }

                @Override
                public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                    return System.lineSeparator();
                }

                @Override
                public String toString() { return "<native fun: lineSeparator>"; }

            });
            GLOBAL.define("exit", new Interpreter.Callable(){

                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                    if (arguments.get(0) instanceof Double exitCode)
                        System.exit(exitCode.intValue());
                    throw new Interpreter.TypeMismatchError(leftPar, Double.class, arguments.get(0), "First argument.");
                }

                @Override
                public String toString() { return "<native fun: exit>"; }

            });
        }
    }
}
