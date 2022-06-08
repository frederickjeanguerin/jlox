package jlox;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    private Scope scope = new Scope(null);

    void reset() {
        scope = new Scope(null);
    }

    void push() {
        scope = new Scope(scope);
    }

    void pop() {
        scope = scope.outer;
    }

    void defineVar(String name, Object value) {
        // NB Variables can be redefined without error
        scope.define(name, value);
    }

    Object getVar(Token varToken) {
        var name = varToken.lexeme();
        var value = scope.get(name);
        if (value == Scope.notFound) {
            throw new Interpreter.RuntimeError(varToken, "Undefined variable '%s'.".formatted(name));
        }
        return value;
    }

    void assignVar(Token varToken, Object value) {
        var name = varToken.lexeme();
        if (!scope.set(name, value)) {
            throw new Interpreter.RuntimeError(varToken, "Undefined variable '%s'.".formatted(name));
        }
    }

    private static class Scope {
        private final Map<String, Object> variables = new HashMap<>();
        private final Scope outer;

        private static final Object notFound = new Object();

        private Scope(Scope outer) {
            this.outer = outer;
        }

        void define(String name, Object value) {
            variables.put(name, value);
        }

        Object get(String name) {
            if (variables.containsKey(name)) {
                return variables.get(name);
            }
            if (outer != null) {
                return outer.get(name);
            }
            return notFound;
        }

        boolean set(String name, Object value) {
            if (variables.containsKey(name)) {
                variables.put(name, value);
                return true;
            }
            if (outer != null) {
                return outer.set(name, value);
            }
            return false;
        }
    }
}
