package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    final String name;
    private final Map<String, Method> methods;
    private final Map<String, LoxCallable> classMethods;

    final Stmt.Class classStmt;

    final List<LoxClass> superclasses;

    public LoxClass(String name, List<LoxClass> superclasses, Map<String, LoxCallable> classMethods, Stmt.Class stmt, Environment.Scoping scoping) {
        this.name = name;
        this.superclasses = superclasses;
        this.classMethods = classMethods;
        this.classStmt = stmt;
        methods = new HashMap<>();
        for (var method : classStmt.methods.methods) {
            methods.put(method.name.lexeme(), new LoxCallable.Method(method, scoping, this));
        }
    }

    public Method findMethod(String name, boolean superclassOnly) {
        var method = methods.getOrDefault(name, null);
        if (method != null && ! superclassOnly) return method;
        for (var superclass: superclasses) {
            method = superclass.findMethod(name, false);
            if (method != null) return method;
        }
        return null;
    }

    public LoxCallable findClassMethod(String name) {
        return classMethods.getOrDefault(name, null);
    }

    @Override
    public String toString() {
        return "<class %s>".formatted(name);
    }

    @Override
    public int arity() {
        // TODO not very efficient, because we need to lookup every time.
        Method init = findMethod("init", false);
        if (init != null) {
            return init.arity();
        }
        return 0;
    }

    @Override
    public boolean isProperty() {
        return false;
    }

    @Override
    public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);
        Method init = findMethod("init", false);
        if (init != null) {
            init.bind(instance).call(interpreter, leftPar, arguments);
        }
        return instance;
    }
}
