package jlox;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class LoxNative implements LoxCallable {

    private final int arity;
    private final boolean isProperty;
    public final String name;

    public LoxNative(String name, int arity, boolean isProperty) {
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

    static final jlox.LoxNative clock = new jlox.LoxNative("clock", 0, false) {
        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            return System.currentTimeMillis() / 1000.0;
        }
    };

    static final jlox.LoxNative lineSeparator = new jlox.LoxNative("lineSeparator", 0, true) {
        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            return System.lineSeparator();
        }
    };
    static final jlox.LoxNative exit = new jlox.LoxNative("exit", 1, false) {
        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            if (arguments.get(0) instanceof Double exitCode)
                System.exit(exitCode.intValue());
            throw new Interpreter.TypeMismatchError(leftPar, Double.class, arguments.get(0), "First argument.");
        }
    };

    static int intArg(Object o, int argPos) {
        if (o instanceof Double d) {
            return d.intValue();
        }
        throw new Interpreter.TypeMismatchError(null, Double.class, o, "Argument #%d.".formatted(argPos));
    }

    static Double doubleArg(Object o, int argPos) {
        if (o instanceof Double d) {
            return d;
        }
        throw new Interpreter.TypeMismatchError(null, Double.class, o, "Argument #%d.".formatted(argPos));
    }

    static String stringArg(Object o, int argPos) {
        if (o instanceof String s) {
            return s;
        }
        throw new Interpreter.TypeMismatchError(null, String.class, o, "Argument #%d.".formatted(argPos));
    }

    public static LoxNative F0(String name, Supplier<Object> func) {
        return new Native(name, 1, false, func);
    }

    public static LoxNative F1(String name, Function<Object, Object> func) {
        return new Native(name, 1, false, func);
    }

    public static LoxNative P1(String name, Function<Object, Object> func) {
        return new Native(name, 1, true, func);
    }

    public static LoxNative F2(String name, Function<Object, Function<Object, Object>> func) {
        return new Native(name, 2, false, func);
    }

    public static LoxNative F3(String name, Function<Object, Function<Object, Function<Object, Object>>> func) {
        return new Native(name, 3, false, func);
    }

    public static LoxNative F4(String name, Function<Object, Function<Object, Function<Object, Function<Object, Object>>>> func) {
        return new Native(name, 4, false, func);
    }

    @SuppressWarnings("unchecked")
    static class Native extends LoxNative {
        private final Object func;

        private Function<Object, Object> func1() { return (Function<Object, Object>) func; }
        private Supplier<Object> func0() { return (Supplier<Object>) func; }

        private Native(String name, int arity, boolean isProperty, Object func) {
            super(name, arity, isProperty);
            this.func = func;
        }

        public Native bind(Object obj, Token token) {
            if (arity() == 0) {
                throw new LoxError(token, "Function %s with 0 argument cannot be bound".formatted(name));
            }
            return new Native("bound " + name, arity() - 1,
                    false, arity() == 1 ?  (Supplier<Object>)() -> func1().apply(obj) : func1().apply(obj));
        }

        @Override
        public Object call(Interpreter interpreter, Token leftPar, List<Object> arguments) {
            if (arity() == 0) return func0().get();
            try {
                var func = this.func;
                for (int i = 0; i < arity(); i++) {
                    func = ((Function<Object, Object>)func).apply(arguments.get(i));
                }
                return func;
            } catch(LoxError error) {
                if (error.token == null) error.token = leftPar;
                throw error;
            }
        }
    }
}
