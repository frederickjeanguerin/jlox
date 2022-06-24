package jlox;

import java.util.List;

interface LoxCallable {
    int arity();

    boolean isProperty();

    Object call(Interpreter interpreter, Token leftPar, List<Object> arguments);

    class Lambda implements LoxCallable {
        private final List<Token> parameters;
        private final Stmt body;
        protected final Environment.Scoping scoping;

        public Lambda(List<Token> parameters, Stmt body, Environment.Scoping scoping) {
            this.parameters = parameters;
            this.body = body;
            this.scoping = scoping;
        }

        @Override
        public int arity() {
            return parameters.size();
        }

        @Override
        public boolean isProperty() {
            return false;
        }
        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            var environment = interpreter.environment;
            environment.swap(scoping);
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
            } catch (Interpreter.ReturnException ex) {
                if (ex.value != null) {
                    return ex.value;
                }
            } finally {
                environment.unswap();
            }
            return null;
        }

        @Override
        public String toString() {
            return "<lambda>";
        }

    }

    class LoxFunction extends Lambda {

        protected final Stmt.Function stmt;

        public LoxFunction(Stmt.Function fun, Environment.Scoping scoping) {
            super(fun.parameters, fun.body, scoping);
            this.stmt = fun;
        }

        protected String name() {
            return stmt.name.lexeme();
        }

        @Override
        public boolean isProperty() {
            return stmt.isProperty;
        }

        @Override
        public String toString() {
            return "<%s %s>".formatted(this.getClass().getSimpleName(), name());
        }
    }

   class Method extends LoxFunction {

        private final LoxClass parent;

        public Method(Stmt.Function stmt, Environment.Scoping scoping, LoxClass parent) {
            super(stmt, scoping);
            this.parent = parent;
        }

        public BoundedMethod bind(LoxInstance instance) {
            return new BoundedMethod(stmt, scoping.bind(parent.classStmt.self, instance), instance);
        }

        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            throw new LoxError(leftPar, "Internal error: cannot call an unbounded method");
        }
    }

    class BoundedMethod extends LoxFunction {
        private final LoxInstance self;

        public BoundedMethod(Stmt.Function fun, Environment.Scoping scoping, LoxInstance instance) {
            super(fun, scoping);
            this.self = instance;
        }

        private boolean isInit() {
            return name().equals("init");
        }

        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            Object result = super.call(interpreter, leftPar, arguments);
            return isInit() ? self : result;
        }
    }

}
