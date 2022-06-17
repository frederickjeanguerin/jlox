package jlox;

import java.util.List;

interface LoxCallable {
    int arity();

    Object call(Interpreter interpreter, Token leftPar, List<Object> arguments);

    class Function implements LoxCallable, Cloneable {

        private enum Type { FUN, LAMBDA, METHOD}
        private final Type type;
        private final String name;
        private final List<Token> parameters;
        private final Stmt body;
        private Environment.Scoping scoping;
        public LoxClass parent = null;

        public Function(Stmt.Function declaration, Environment.Scoping scoping) {
            this.name = declaration.name.lexeme();
            this.parameters = declaration.parameters;
            this.body = declaration.body;
            this.scoping = scoping;
            this.type = switch(declaration.kind) {
                case "function" -> Type.FUN;
                case "method" -> Type.METHOD;
                default -> throw new IllegalStateException("Unexpected value: " + declaration.kind);
            };
        }

        public Function(Expr.Lambda lambda, Environment.Scoping scoping) {
            this.name = null;
            this.parameters = lambda.parameters;
            this.body = lambda.body;
            this.scoping = scoping;
            this.type = Type.LAMBDA;
        }

        public Function bind(LoxInstance instance) {
            assert this.type == Type.METHOD; // TODO define class Method that inherits from Function
            try {
                var clone = (Function)clone();
                clone.scoping = clone.scoping.bind(parent.classStmt.self, instance);
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean isInit() {
            return type == Type.METHOD && name.equals("init");
        }

        @Override
        public int arity() {
            return parameters.size();
        }

        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            var environment = interpreter.environment;
            // TODO swap without new scope if no parameters or join parameters with locals in block
            environment.swap(scoping);
            var self = isInit()
                    ? environment.getSymbol(parent.classStmt.self, parent.classStmt.self).getValue(leftPar)
                    : null;
            try {
                for (int i = 0; i < arity(); i++) {
                    environment.defineSymbol(
                            parameters.get(i), arguments.get(i), Symbol.Type.PARAMETER);
                }
                if (body instanceof Stmt.Expression expr) {
                    return interpreter.evaluate(expr.expression);
                } else {
                    interpreter.execute(body);
                }
                if (isInit()) return self;
            } catch (Interpreter.ReturnException ex) {
                if (ex.value != null) {
                    return ex.value;
                }
                if (isInit()) return self;
            } finally {
                environment.unswap();
            }
            return null;
        }

        @Override
        public String toString() {
            return switch (type) {
                case FUN -> "<fun: %s>".formatted(name);
                case LAMBDA -> "<lambda>";
                case METHOD -> "<method: %s>".formatted(name);
            };
        }
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
