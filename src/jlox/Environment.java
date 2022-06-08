package jlox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> variables = new HashMap<>();

    void defineVar(String name, Object value) {
        // NB Variables can be redefined without error
        variables.put(name, value);
    }

    Object getVar(Token varToken) {
        var name = varToken.lexeme();
        if (variables.containsKey(name)) {
            return variables.get(name);
        }
        throw new Interpreter.RuntimeError(varToken, "Undefined variable '%s'.".formatted(name));
    }
}
