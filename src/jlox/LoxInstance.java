package jlox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxInstance {
    private final LoxClass klass;
    private final Map<String, Object> fields = new HashMap<>();

    public LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }

    public Object get(Token name) {
        String lexeme = name.lexeme();
        if (fields.containsKey(lexeme)) {
            return fields.get(lexeme);
        }
        var method = klass.findMethod(lexeme, false);
        if (method != null) return method.bind(this);

        throw new LoxError(name, "Undefined property '%s'.".formatted(lexeme));
    }

    public LoxCallable.Function getSuper(Token methodName, Token targetClassName, boolean isExplicit) {

        var targetClass = findTargetClass(klass, targetClassName);

        if (targetClass == null) {
            // this error should not occur if static analysis is correct
            throw new LoxError(methodName, "(Internal Error) Class '%s' in not reachable."
                    .formatted(targetClassName.lexeme()));
        }
        var method = targetClass.findMethod(methodName.lexeme(), !isExplicit);
        if (method == null) {
            throw new LoxError(methodName,
                    (isExplicit ? "Superclass '%s' has no available method '%s'."
                                : "Class '%s' has no superclasses accepting method '%s'.")
                    .formatted(targetClass.name, methodName.lexeme()));
        }
        return method.bind(this);
    }

    private LoxClass findTargetClass(LoxClass klass, Token targetClassName) {
        if (klass.classStmt.name == targetClassName) return klass;
        for (var superclass : klass.superclasses) {
            var found = findTargetClass(superclass, targetClassName);
            if (found != null) return found;
        }
        return null;
    }

    public Object set(Token name, Object value) {
        fields.put(name.lexeme(), value);
        return value;
    }
}
