package jlox;

import java.util.List;

interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, Token leftPar, List<Object> arguments);

    class Function implements LoxCallable {

        private final Stmt.Function declaration;
        private final Environment.Scoping scoping;

        public Function(Stmt.Function declaration, Environment.Scoping scoping) {
            this.declaration = declaration;
            this.scoping = scoping;
        }

        @Override
        public int arity() {
            return declaration.parameters.size();
        }

        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            var environment = interpreter.environment;
            // TODO swap without new scope if no parameters or join parameters with locals in block
            environment.swap(scoping);
            try {
                for (int i = 0; i < arity(); i++) {
                    environment.defineSymbol(
                            declaration.parameters.get(i).lexeme(), arguments.get(i));
                }
                interpreter.executeBlock(declaration.body);
            } catch (Interpreter.ReturnException ex) {
                return ex.value;
            } finally {
                environment.unswap();
            }
            return null;
        }

        @Override
        public String toString() { return "<fun: %s>".formatted(declaration.name.lexeme()); }

    }

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
