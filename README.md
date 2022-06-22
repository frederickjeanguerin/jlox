# JLox FG flavor

This is my very own version of the Lox language Java interpreter, as proposed in the book
[Crafting Interpreters](https://craftinginterpreters.com/) by _Robert Nystrom_.

This implementation is far from perfect and contains still many bugs for sure, 
although I managed to reduce them somewhat.
Nevertheless, you might find here some interesting ideas from language extensions
I made. 

First and foremost, this implementation comes with many *test cases*, because I found it
almost impossible to trust my own code after a few chapters.

Throughout the book, I managed to implement most of the challenges presented to the reader.
I detail these challenges and together with more personal stuff I added hereafter.

## 4.4 - Block comment (non nesting)
Chapter 4 challenge 4 asked for block comment: ``/* my block comment */``

[//]: # (TODO: Add nesting.)

## 4.x1 - String with escape sequences
Some escape sequence are permitted, like `\n`, `\t`, `\"`, or `\\`. See [translateEscapes](https://docs.oracle.com/en/java/javase/13/docs/api/java.base/java/lang/String.html#translateEscapes()).
