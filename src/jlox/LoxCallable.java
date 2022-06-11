package jlox;

import java.util.List;

interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, Token leftPar, List<Object> arguments);

    class Native {
        static final LoxCallable clock = new LoxCallable(){

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                return System.currentTimeMillis()/1000.0;
            }

            @Override
            public String toString() { return "<native fun: clock>"; }

        };

        static final LoxCallable localSeparator = new LoxCallable(){

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                return System.lineSeparator();
            }

            @Override
            public String toString() { return "<native fun: lineSeparator>"; }

        };

        static final LoxCallable exit = new LoxCallable(){

            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                if (arguments.get(0) instanceof Double exitCode)
                    System.exit(exitCode.intValue());
                throw new Interpreter.TypeMismatchError(leftPar, Double.class, arguments.get(0), "First argument.");
            }

            @Override
            public String toString() { return "<native fun: exit>"; }

        };
    }
}
