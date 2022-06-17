package jlox;

class Symbol {

    enum Type { VAR, FUN, PARAMETER, CLASS, SPECIAL }
    static final Object UNINITIALIZED = new Object();

    final Token token;
    private Object value;
    final boolean readonly;
    final Type type;
    private int useCount = 0;

    Symbol(Token token, boolean readonly, Object value, Type type) {
        this.token = token;
        this.readonly = readonly;
        this.value = value;
        this.type = type;
    }

    public Object getValue(Token emitter) {
        if (value == UNINITIALIZED)
            throw new LoxError(emitter, "%s is uninitialized.".formatted(name()));
        return value;
    }

    public void setValue(Token receiver, Object value) {
        if (readonly)
            throw new LoxError(receiver, "%s is read only.".formatted(name()));
        this.value = value;
    }

    public void use() {
        useCount++;
    }

    public boolean isUnused() { return useCount == 0; }

    public void resetUseCount() { useCount = 0; }

    public String name() {
        return  typeName() + " '" + token.lexeme() + "'";
    }

    public String typeName() {
        return typeName(type);
    }

    public static String typeName(Type type) {
        return switch(type){
            case VAR -> "variable";
            case FUN -> "function";
            case PARAMETER -> "parameter";
            case CLASS -> "class";
            case SPECIAL -> "special";
        };
    }

    @Override
    public String toString() {
        return name();
    }
}
