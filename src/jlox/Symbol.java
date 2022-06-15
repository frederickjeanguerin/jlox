package jlox;

class Symbol {
    static final Object UNINITIALIZED = new Object();

    final Token token;
    private Object value;
    final boolean readonly;

    Symbol(Token token, boolean readonly, Object value) {
        this.token = token;
        this.readonly = readonly;
        this.value = value;
    }

    public Object getValue(Token emitter) {
        if (value == UNINITIALIZED)
            throw new Interpreter.RuntimeError(emitter, "Uninitialized variable '%s'".formatted(emitter.lexeme()));
        return value;
    }

    public void setValue(Token receiver, Object value) {
        if (readonly)
            throw new Interpreter.RuntimeError(receiver, "Readonly symbol '%s'.".formatted(receiver.lexeme()));
        this.value = value;
    }
}
