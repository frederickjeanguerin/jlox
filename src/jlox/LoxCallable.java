package jlox;

import java.util.List;

interface LoxCallable {
    int arity();

    boolean isProperty();

    Object call(Interpreter interpreter, Token leftPar, List<Object> arguments);

    class Function implements LoxCallable, Cloneable {

        private enum Type { FUN, LAMBDA, METHOD }
        private final Type type;
        private final String name;
        private final List<Token> parameters;
        private final Stmt body;
        private Environment.Scoping scoping;
        public LoxClass parent = null;
        private boolean isProperty = false;

        @Override
        public boolean isProperty() {
            return isProperty;
        }

        public Function(Stmt.Function declaration, Environment.Scoping scoping) {
            this.name = declaration.name.lexeme();
            this.parameters = declaration.parameters;
            this.body = declaration.body;
            this.scoping = scoping;
            this.isProperty = declaration.isProperty;
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
                            parameters.get(i), arguments.get(i), Symbol.Type.PARAMETER, false);
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

    abstract class Native implements LoxCallable {

        private final int arity;
        private final boolean isProperty;
        private final String name;

        public Native(String name, int arity, boolean isProperty){
            this.name = name;
            this.arity = arity;
            this.isProperty = isProperty;
        }

        @Override
        public int arity() {
            return arity;
        }

        @Override
        public boolean isProperty() {
            return isProperty;
        }

        @Override
        public String toString() {
            return "<native fun: %s>".formatted(name);
        }

        static final Native clock = new Native("clock", 0, false){
            @Override
            public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                return System.currentTimeMillis()/1000.0;
            }
        };

        static final Native lineSeparator = new Native("lineSeparator", 0, true){
            @Override
            public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                return System.lineSeparator();
            }
        };
        static final Native exit = new Native("exit", 1, false){
            @Override
            public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
                if (arguments.get(0) instanceof Double exitCode)
                    System.exit(exitCode.intValue());
                throw new Interpreter.TypeMismatchError(leftPar, Double.class, arguments.get(0), "First argument.");
            }
        };
    }
}
