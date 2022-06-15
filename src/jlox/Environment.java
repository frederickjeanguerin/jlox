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
        scope = new Scope(Scope.GLOBAL, false, false);
    }

    void push() {
        scope = new Scope(scope, false, true);
    }

    void pop() {
        scope = scope.outer;
        assert scope.outer != null; // We don't want to pop to the global scope!
    }

    int depth() {
        int depth = 0;
        Scope current = scope;
        while (current != null) {
            depth++;
            current = current.outer;
        }
        return depth;
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

    void defineUninitializedVariable(Token name) {
        defineSymbol(name, Symbol.UNINITIALIZED, Symbol.Type.VAR);
    }

    void defineSymbol(Token name, Object value, Symbol.Type type) {
        // NB Symbol can be redefined without error
        scope.define(name.lexeme(), name, value, type);
    }

    /**
     *
     * @return True if the current scope has parameters defined into it
     */
    boolean hasNoLocalParameters() {
        return scope.symbols.values().stream().noneMatch(sym -> sym.type == Symbol.Type.PARAMETER);
    }

    Symbol getSymbol(Token symbol) {
        return getSymbol(symbol, null);
    }

    Symbol getSymbol(Token symbol, Token target) {
        var name = symbol.lexeme();
        var value = scope.get(name, target);
        if (value == null) {
            throw new LoxError(symbol, "Undefined symbol '%s'.".formatted(name));
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

        private final boolean oneDefinitionOnly;

        private Scope(Scope outer, boolean readonly, boolean oneDefinitionOnly) {
            this.outer = outer;
            this.readonly = readonly;
            this.oneDefinitionOnly = oneDefinitionOnly;
        }

        void define(String name, Token token, Object value, Symbol.Type type) {
            if (oneDefinitionOnly && symbols.get(name) != null) {
                throw new LoxError(token,
                        "Symbol '%s' cannot be redeclared.".formatted(token.lexeme()));
            }
            symbols.put(name, new Symbol(token, readonly, value, type));
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

        static final Scope GLOBAL = new Scope(null, true, true);

        static {
            GLOBAL.define("clock", Token.Special("<fun clock>"), LoxCallable.Native.clock, Symbol.Type.FUN);
            GLOBAL.define("lineSeparator", Token.Special("<fun lineSeparator>"), LoxCallable.Native.localSeparator, Symbol.Type.FUN);
            GLOBAL.define("exit", Token.Special("<fun exit>"), LoxCallable.Native.exit, Symbol.Type.FUN);
        }
    }
}
