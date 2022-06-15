package jlox;

class LoxError extends RuntimeException {
    final Token token;

    public LoxError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public LoxError(Token token) {
        this(token, "Unhandled exception (internal error)");
    }
}
