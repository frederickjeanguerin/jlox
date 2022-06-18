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

    public LoxCallable.Function getSuper(Token methodName, Token superClassName) {
        var superClass = klass;
        while (superClass != null && superClass.classStmt.name != superClassName)
            superClass = superClass.superclass;
        if (superClass == null) {
            // this error should not occur if static analysis is fine
            throw new LoxError(methodName, "Superclass '%s' in not reachable."
                    .formatted(superClassName.lexeme()));
        }
        var method = superClass.findMethod(methodName.lexeme());
        if (method == null) {
            throw new LoxError(methodName, "Superclass '%s' has no available method '%s'."
                    .formatted(superClassName.lexeme(), methodName.lexeme()));
        }
        return method.bind(this);
    }

    public Object set(Token name, Object value) {
        fields.put(name.lexeme(), value);
        return value;
    }
}
