package jlox;

record Token(TokenType type, String lexeme, Object literal, int line) {
    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
    public static Token Special(String name) { return new Token(TokenType.IDENTIFIER, name, null, -1); }

    public boolean lexEquals(Token tok) {
        return lexeme.equals(tok.lexeme);
    }

    public record Compound(Token operator, Token equal){}
}
