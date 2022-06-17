package jlox;

enum TokenType {
    // Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COLON, COMMA, DOT, PERCENT, QUESTION, SEMICOLON, SLASH, STAR,

    // One or two character tokens
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    MINUS, MINUS_MINUS, PLUS, PLUS_PLUS,

    // Literals
    IDENTIFIER, STRING, NUMBER,

    // Keywords
    AND, BREAK, CLASS, CONTINUE, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SELF, SUPER, TRUE, VAR, WHILE,

    // Special
    EOF, ERROR,
}

