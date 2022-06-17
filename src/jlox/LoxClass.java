package jlox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    private final Map<String, Function> methods;
    final Stmt.Class classStmt;

    public LoxClass(String name, Map<String, Function> methods, Stmt.Class klass) {
        this.name = name;
        this.methods = methods;
        for (var method : methods.values()) {
            method.parent = this;
        }
        classStmt = klass;
    }

    public Function findMethod(String name) {
        return methods.getOrDefault(name, null);
    }

    @Override
    public String toString() {
        return "<class %s>".formatted(name);
    }

    @Override
    public int arity() {
        // TODO not very efficient, because we need to lookup every time.
        Function init = findMethod("init");
        if (init != null) {
            return init.arity();
        }
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        Function init = findMethod("init");
        if (init != null) {
            init.bind(instance).call(interpreter, leftPar, arguments);
        }
        return instance;
    }
}
