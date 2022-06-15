package jlox;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

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

    /**
     * Save the current scope and replace with a new one (function call).
     * @param scoping from @getScoping
     */
    void swap(Scoping scoping) {
        swapped.push(scope);
        scope = scoping.scope;
        push();
    }

    /**
     * Call to reestablish a previously swapped scope.
     */
    void unswap() {
        pop();
        scope = swapped.pop();
    }

    void defineUninitializedSymbol(Token name) {
        defineSymbol(name, Symbol.UNINITIALIZED);
    }

    void defineSymbol(Token name, Object value) {
        // NB Symbol can be redefined without error
        scope.define(name.lexeme(), name, value);
    }

    Symbol getSymbol(Token symbol) {
        return getSymbol(symbol, null);
    }

    Symbol getSymbol(Token symbol, Token target) {
        var name = symbol.lexeme();
        var value = scope.get(name, target);
        if (value == null) {
            throw new Interpreter.RuntimeError(symbol, "Undefined symbol '%s'.".formatted(name));
        }
        return value;
    }

    void assignSymbol(Token symbol, Object value, Token target) {
        getSymbol(symbol, target).setValue(symbol, value);
    }

    static class Scoping {
        private final Scope scope;

        Scoping(Scope scope) {
            this.scope = scope;
        }
    }

    private static class Scope {
        private final Map<String, Symbol> symbols = new HashMap<>();
        private final Scope outer;
        private final boolean readonly;

        private Scope(Scope outer, boolean readonly) {
            this.outer = outer;
            this.readonly = readonly;
        }

        void define(String name, Token token, Object value) {
            symbols.put(name, new Symbol(token, readonly, value));
        }

        Symbol get(String name, Token target) {
            if (symbols.containsKey(name)) {
                Symbol symbol = symbols.get(name);
                if (target == null || target == symbol.token) return symbol;
            }
            if (outer != null) {
                return outer.get(name, target);
            }
            return null; // not found
        }

        static final Scope GLOBAL = new Scope(null, true);

        static {
            GLOBAL.define("clock", null, LoxCallable.Native.clock);
            GLOBAL.define("lineSeparator", null, LoxCallable.Native.localSeparator);
            GLOBAL.define("exit", null, LoxCallable.Native.exit);
        }
    }
}
