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
        if (method != null) return method;

        throw new LoxError(name, "Undefined property '%s'.".formatted(lexeme));
    }

    public Object set(Token name, Object value) {
        fields.put(name.lexeme(), value);
        return value;
    }
}
