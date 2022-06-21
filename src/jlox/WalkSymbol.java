package jlox;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class WalkSymbol extends Walk.Base<Void> {

    private final Environment environment;

    private final Stack<Symbol> functions = new Stack<>();
    private final Stack<Stmt.Class> classes = new Stack<>();

    public WalkSymbol(Environment environment) {
        super(null);
        this.environment = environment;
    }

    @Override
    public void enterClassStmt(Stmt.Class stmt) {
        defineSymbol(stmt.name, Symbol.Type.CLASS);
        List<String> visited = new ArrayList<>();
        for (var superclass: stmt.superclasses) {
            if (stmt.name.lexeme().equals(superclass.name.lexeme())) {
                stdio().errorAtToken(superclass.name, "A class can't inherit from itself.");
            }
            if (visited.contains(superclass.name.lexeme())) {
                stdio().errorAtToken(superclass.name, "A class can't be inherited more than once.");
            }
            visited.add(superclass.name.lexeme());
        }
        classes.push(stmt);

        // Defining class symbols
        environment.push(true);
        stmt.self = Token.Special("self");
    }

    @Override
    public void leaveClassStmt(Stmt.Class stmt) {
        environment.pop();
        classes.pop();
    }

    @Override
    public void enterMethodsStmt(Stmt.Methods methods) {
        environment.push(true);
        defineSymbol(classes.peek().self, Symbol.Type.SPECIAL);
    }

    @Override
    public void leaveMethodsStmt(Stmt.Methods methods) {
        environment.pop();
    }

    @Override
    public void enterFunctionStmt(Stmt.Function stmt) {
        if (!stmt.kind.equals("method") || stmt.isClass) {
            var fun = defineSymbol(stmt.name, Symbol.Type.FUN);
            functions.push(fun);
        }
        enterFunction(stmt.parameters);
    }

    @Override
    public void leaveFunctionStmt(Stmt.Function stmt) {
        pop();
        if (!stmt.kind.equals("method") || stmt.isClass) {
            var fun = functions.pop();
            if (fun != null) {
                // recursive call in a function don't count for function usage
                fun.resetUseCount();
            }
        }
    }

    @Override
    public void enterLambdaExpr(Expr.Lambda lambda) {
        enterFunction(lambda.parameters);
    }

    @Override
    public void leaveLambdaExpr(Expr.Lambda expr) {
        pop();
    }

    @Override
    public void enterBlockStmt(Stmt.Block block) {
        if (environment.hasNoLocalParameters())
            environment.push();
    }

    @Override
    public void leaveBlockStmt(Stmt.Block block) {
        if (environment.hasNoLocalParameters())
            pop();
    }

    @Override
    public void leaveVarStmt(Stmt.Var var) {
        defineSymbol(var.name, Symbol.Type.VAR);
    }

    @Override
    public void enterAssignExpr(Expr.Assign assign) {
        try {
            var assignee = environment.getSymbol(assign.name);
            if (assignee.readonly) {
                stdio().errorAtToken(assignee.token, "%s cannot be modified.".formatted(assignee.name()));
            }
            assign.target = assignee.token;
        } catch (LoxError error) {
            stdio().errorAtToken(error.token, error.getMessage());
        }
    }

    @Override
    public void enterVariableExpr(Expr.Variable variable) {
        try {
            var sym = environment.getSymbol(variable.name);
            sym.use();
            variable.target = sym.token;
        } catch (LoxError error) {
            stdio().errorAtToken(error.token, error.getMessage());
        }
    }

    @Override
    public void enterSuperExpr(Expr.Super expr) {
        if (classes.empty()) {
            stdio().errorAtToken(expr.keyword, "Super used outside any classes");
            return;
        }
        var klass = classes.peek();
        if (klass.superclasses.isEmpty()) {
            stdio().errorAtToken(expr.keyword, "Class %s has no superclasses.".formatted(klass.name.lexeme()));
            return;
        }
        if (expr.explicitSuperclass != null) {
            for (var superclass : klass.superclasses) {
                if (expr.explicitSuperclass.lexEquals(superclass.name)) {
                    expr.targetClass = superclass.target;
                    return;
                }
            }
            stdio().errorAtToken(expr.explicitSuperclass,
                    "Class %s is not a direct superclass of %s."
                            .formatted(expr.explicitSuperclass.lexeme(), klass.name.lexeme()));
        } else {
            expr.targetClass = klass.name;
        }
    }

    private void enterFunction(List<Token> parameters) {
        environment.push();
        for (var parameter : parameters) {
            defineSymbol(parameter, Symbol.Type.PARAMETER);
        }
    }



    private boolean checkIdentifierName(Token name, Symbol.Type type) {
        if (name.type() != TokenType.IDENTIFIER) {
            switch(type) {
                case VAR:
                case FUN:
                case PARAMETER:
                case CLASS:
                    stdio().errorAtToken(name, "'%s' is not a valid identifier for a %s."
                            .formatted(name.lexeme(), Symbol.typeName(type)));
                    return false;
                case SPECIAL:
                    /* OK */
                    break;
            }
        }
        return true;
    }

    private Symbol defineSymbol(Token token, Symbol.Type type) {
        if (! checkIdentifierName(token, type))
            return null;
        try {
            return environment.defineSymbol(token, token, type);
        } catch (LoxError error) {
            stdio().errorAtToken(error.token, error.getMessage());
        }
        return null;
    }

    private void pop() {
        for(Symbol sym : environment.localSymbols()) {
            if (sym.isUnused() && sym.type != Symbol.Type.SPECIAL) {
                stdio().warningAtToken(sym.token, sym.name() + " is unused.");
            }
        }
        environment.pop();
    }
}
