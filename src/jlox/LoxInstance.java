package jlox;

import java.util.HashMap;
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
        var method = klass.findMethod(lexeme);
        if (method != null) return method.bind(this);

        throw new LoxError(name, "Undefined property '%s'.".formatted(lexeme));
    }

    public LoxCallable.Function getSuper(Token methodName, Token targetClassName) {
        var targetClass = klass;
        while (targetClass != null && targetClass.classStmt.name != targetClassName)
            targetClass = targetClass.superclass;
        if (targetClass == null) {
            // this error should not occur if static analysis is fine
            throw new LoxError(methodName, "(Internal Error) Class '%s' in not reachable."
                    .formatted(targetClassName.lexeme()));
        }
        var method = targetClass.superclass.findMethod(methodName.lexeme());
        if (method == null) {
            throw new LoxError(methodName, "Superclass '%s' has no available method '%s'."
                    .formatted(targetClass.superclass.name, methodName.lexeme()));
        }
        return method.bind(this);
    }

    public Object set(Token name, Object value) {
        fields.put(name.lexeme(), value);
        return value;
    }
}
