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

---

<mark>TODO</mark>: Add block comment nesting.

---

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

---

<mark>TODO</mark>: exponentiation `**`

---

## 8 - Statements

### 8.1 - REPL still working

Added `#ast` modifier to print only the AST without evaluation.

Example: `#ast 4 + 5` will output `(+ 4 5)`.

### 8.2 - Variables should be initialized

This is checked at runtime, although some checking could also be implemented 
at "compile time" (static analysis phase).

---

<mark>TODO</mark>: do some variable initialisation checking at compile time.

---

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

Prefix increment and decrement operator provided with syntax desugaring.

* `++x` desugared to `x = x + 1`
* `--x` desugared to `x = x - 1`

Type checked for Double, to prohibit something like:

    var str = "string";
    ++str;                  // string1, by x = x + 1 with concatenation

---

<mark>TODO</mark>: compound assignment `+=`, etc.

---

## 10 - Functions

### 10.2 - Lambdas (anonymous functions)

The statement `fun () {};` will generate a parsing error, 
since the parser is expecting function statement, and will
be missing a function name. 
An error seems fine here, but a parsing error is not the best.

### 10.3 - Redeclaration of parameters (prohibited)

The following program will generate an error because local variables 
at the top function level cannot shadow parameters:

    fun scope(a) {
        var a = "local";            // semantic error
    }

### 10.x4 - Oneliner functions

Functions can be defined as oneliners as follows:

    fun sum(a, b) a + b;            // oneliner function

    var product = fun(a, b) a * b;  // oneliner lambda

### 10.x5 - Primitive exit

The `exit` function has been added as a primitive. 
It expects an exit code as an argument and terminate the program immediately.

### 10.x6 - Return and dead code

Some dead code warning reported after `return`.

---
<mark>TODO</mark>: make function definition readonly. 
The following should trigger a semantic error:

    fun f(){}
    f = 0;          // error, f is readonly
---

## 11 - Resolving and Binding

To enable lexical scoping, I attach semantic information to the AST nodes themselves, 
instead of collecting information in a map.

### 11.1 - Safe early bing of functions

Early binding of functions is not only safe but desirable, to enable recursion.

    fun factorial(n) {
        if (n < 2) return 1;
        return n * factorial(n - 1);    // binding here desirable
    }

For variable, early binding is not desirable

    var a = 
            a + 1;       // symbol a should not reference the newly declared var here.

### 11.3 - Unused variable

Are reported as warnings outside the global scope. 
Assignment/initialization doesn't count.

### 11.x5 - Unused function

Are reported just like unused variables.

## 12 - Classes

### 12.1 - Static method

* Not implemented using metaclasses.
* Cannot access `this` (or `self`).
* Cannot define static fields.
* Can be used without prefix inside the class itself.


    class Math {
        class factorial(n) {
            if (n < 2) return 1;
            return factorial (n - 1)
        }

        class permutationCoefficient(n, k) {
            return factorial(n) / factorial(k);     // no prefix necessary here
        }
    }

    print Math.factorial(10);                       // prefix necessary here

### 12.2 - Readonly properties (getters)

Are introduced with a colon `:` because the notation 
proposed in the book would be conflictual for one-liners.

Static properties are available as well.

Hence, the following class is perfectly legal.

    class Circle {
    
        class PI : 3.14159;         // static oneliner property (constant like)
        class TAU : 2 * PI;         // idem
    
        init(radius) {
            self.radius = radius;
        }
    
        area: PI * self.radius * self.radius;   // instance oneliner property
    
        circumference: TAU * self.radius;       // idem
    }

## 13 - Inheritance

### 13.1 - Multiple inheritance

* Implemented the Python way. 
* The order of the superclasses is important for name resolution in parents.
* A specific superclass can be targeted with the `super` keyword as `super(superclassName)`.
This makes it possible to initialize every parent classes in its own way.


    class Jedi < Person, ForceBearer {
        init(name, age, force) {
            super.init(name, age);              // Init Person superclass
            super(ForceBearer).init(force);     // Init ForceBearer superclass
        }
    }

