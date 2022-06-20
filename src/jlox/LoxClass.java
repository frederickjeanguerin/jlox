package jlox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    private final Map<String, Function> methods;
    final Stmt.Class classStmt;

    final List<LoxClass> superclasses;

    public LoxClass(String name, List<LoxClass> superclasses, Map<String, Function> methods, Stmt.Class stmt) {
        this.name = name;
        this.methods = methods;
        this.superclasses = superclasses;
        for (var method : methods.values()) {
            method.parent = this;
        }
        classStmt = stmt;
    }

    public Function findMethod(String name, boolean superclassOnly) {
        var method = methods.getOrDefault(name, null);
        if (method != null && ! superclassOnly) return method;
        for (var superclass: superclasses) {
            method = superclass.findMethod(name, false);
            if (method != null) return method;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<class %s>".formatted(name);
    }

    @Override
    public int arity() {
        // TODO not very efficient, because we need to lookup every time.
        Function init = findMethod("init", false);
        if (init != null) {
            return init.arity();
        }
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        Function init = findMethod("init", false);
        if (init != null) {
            init.bind(instance).call(interpreter, leftPar, arguments);
        }
        return instance;
    }
}
