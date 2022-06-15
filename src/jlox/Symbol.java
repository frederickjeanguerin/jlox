package jlox;

class Symbol {

    enum Type { VAR, FUN, PARAMETER }
    static final Object UNINITIALIZED = new Object();

    final Token token;
    private Object value;
    final boolean readonly;
    final Type type;

    Symbol(Token token, boolean readonly, Object value, Type type) {
        this.token = token;
        this.readonly = readonly;
        this.value = value;
        this.type = type;
    }

    public Object getValue(Token emitter) {
        if (value == UNINITIALIZED)
            throw new LoxError(emitter, "Uninitialized variable '%s'".formatted(emitter.lexeme()));
        return value;
    }

    public void setValue(Token receiver, Object value) {
        if (readonly)
            throw new LoxError(receiver, "Readonly symbol '%s'.".formatted(receiver.lexeme()));
        this.value = value;
    }
}
