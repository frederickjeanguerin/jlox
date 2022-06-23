# JLox FG flavor

Welcome to my very own version of the Lox language Java interpreter, as proposed in the book
[Crafting Interpreters](https://craftinginterpreters.com/) by _Robert Nystrom_.

This implementation is far from perfect and contains still many bugs for sure, 
although I managed to reduce them somewhat.
Nevertheless, you might find here some interesting ideas from language extensions
I made. 

First and foremost, this implementation comes with many *test cases*, because I found it
almost impossible to trust my own code after a few chapters.

Throughout the book, I managed to implement most of the challenges presented to the reader.
I detail these challenges and together with more personal stuff I added hereafter.

## 4 - Scanning

### 4.4 - Block comment (non nesting)
Chapter 4 challenge 4 asked for block comment: ``/* my block comment */``

TODO: Add nesting.

### 4.x5 - String with escape sequences
Some escape sequence are permitted, like `\n`, `\t`, `\"`, or `\\`. See [translateEscapes](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/lang/String.html#translateEscapes()).

## 6 - Parsing

### 6.1 - Comma expression

This one is tricky because it can mess with arguments list in function calls. 
If it were just for me, I would use another separator than the comma.

### 6.2 - Ternary

The famous `condition ? expr_if_true : expr_if_false` operator.

### 6.3 - Binary without first operand.

An error token is provided as a missing first operand and the parsing continues.

## 7 - Evaluating expressions

### 7.1 - String comparisons 

Using lexicographic comparison, like `"ab" < "cd"`.
It is case-sensitive, and not localized. 

### 7.2 - String concatenation with any type

Works with a string in either position (left or right).

- `"str" + 4` yields `"str4"`
- `4 + "str"` yields `"4str"`

### 7.3 - Division by Zero

Generate a runtime error.

### 7.x4 - Modulo operator `%`

May generate a division by zero.

## 8 - Statements

### 8.1 - REPL still working

Added `#ast` modifier to print only the AST without evaluation.

Example: `#ast 4 + 5` will output `(+ 4 5)`.

### 8.2 - Variables should be initialized

This is checked at runtime, although some checking could also be implemented 
at "compile time" (static analysis phase).

TODO: do some checking at compile time.

## 9 - Control Flow

### 9.3 - Break from loops

Added `break` statement using an exception mechanism in Java. 

A check for using a break outside any loop is made during the 
analysis phase (after parsing but before interpretation).
A check for dead code is also made (not exhaustive).

### 9.4x - Continue from loops

A `continue` statement is also provided. Works like break. 
I had to create a special construct to accommodate the desugared `for` loop.

### 9.5x - Increment and decrement

Increment and decrement provided with syntax sugaring.

TODO: make it work with fields.
