package jlox;

import java.util.List;
import java.util.Stack;

public class WalkSymbol extends Walk.Base<Void> {

    private final Environment environment;

    private final Stack<Symbol> functions = new Stack<>();

    public WalkSymbol(Environment environment) {
        super(null);
        this.environment = environment;
    }

    @Override
    public void enterClassStmt(Stmt.Class stmt) {
        defineSymbol(stmt.name, Symbol.Type.CLASS);
        if (stmt.superclass != null && stmt.name.lexeme().equals(stmt.superclass.name.lexeme())) {
            stdio().errorAtToken(stmt.superclass.name, "A class can't inherit from itself");
        }
        environment.push(true);
        stmt.self = Token.Special("self");
        defineSymbol(stmt.self, Symbol.Type.SPECIAL);
    }

    @Override
    public void leaveClassStmt(Stmt.Class stmt) {
        environment.pop();
    }

    @Override
    public void enterFunctionStmt(Stmt.Function stmt) {
        var fun = defineSymbol(stmt.name, Symbol.Type.FUN);
        functions.push(fun);
        enterFunction(stmt.parameters);
    }

    @Override
    public void leaveFunctionStmt(Stmt.Function stmt) {
        pop();
        var fun = functions.pop();
        if (fun != null) {
             // recursive call in a function don't count for function usage
             fun.resetUseCount();
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
