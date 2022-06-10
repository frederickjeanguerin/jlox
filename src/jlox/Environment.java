package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {

    private Scope scope;

    Environment() {
        reset();
    }

    void reset() {
        scope = new Scope(Scope.global());
    }

    void push() {
        scope = new Scope(scope);
    }

    void pop() {
        scope = scope.outer;
        assert scope.outer != null; // We don't want to pop to the global scope!
    }

    void defineSymbol(String name, Object value) {
        // NB Symbol can be redefined without error
        scope.define(name, value);
    }

    Object getSymbol(Token symbol) {
        var name = symbol.lexeme();
        var value = scope.get(name);
        if (value == Scope.NOT_FOUND) {
            throw new Interpreter.RuntimeError(symbol, "Undefined symbol '%s'.".formatted(name));
        }
        return value;
    }

    void assignSymbol(Token symbol, Object value) {
        var name = symbol.lexeme();
        if (!scope.modify(name, value)) {
            throw new Interpreter.RuntimeError(symbol, "Undefined symbol '%s'.".formatted(name));
        }
    }

    private static class Scope {
        private final Map<String, Object> symbols = new HashMap<>();
        private final Scope outer;

        private static final Object NOT_FOUND = new Object();

        private Scope(Scope outer) {
            this.outer = outer;
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

        boolean modify(String name, Object value) {
            // Note: global symbols can be modified (should they be?)
            if (symbols.containsKey(name)) {
                symbols.put(name, value);
                return true;
            }
            if (outer != null) {
                return outer.modify(name, value);
            }
            return false;
        }

        static Scope global() {
            var scope = new Scope(null);
            scope.define("clock", new Interpreter.Callable(){

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
            scope.define("lineSeparator", new Interpreter.Callable(){

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
            scope.define("exit", new Interpreter.Callable(){

                @Override
                public int arity() {
                    return 1;
                }

                @Override
                public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                    if (arguments.get(0) instanceof Double exitCode)
                        System.exit(exitCode.intValue());
                    throw new Interpreter.TypeMismatchError(leftPar, Double.class, arguments.get(0).getClass(), "First argument.");
                }

                @Override
                public String toString() { return "<native fun: exit>"; }

            });
            return scope;
        }
    }
}
