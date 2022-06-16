package jlox;

import java.util.List;

public class WalkSymbol extends Walk.Base<Void> {

    private final Environment environment;

    public WalkSymbol(Environment environment) {
        super(null);
        this.environment = environment;
    }

    @Override
    public void enterFunctionStmt(Stmt.Function stmt) {
        defineSymbol(stmt.name, Symbol.Type.FUN);
        enterFunction(stmt.parameters);
    }

    @Override
    public void leaveFunctionStmt(Stmt.Function stmt) {
        pop();
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
            assign.target = environment.getSymbol(assign.name).token;
        } catch (LoxError error) {
            stdio().errorAtToken(error.token, error.getMessage());
        }
    }

    @Override
    public void enterVariableExpr(Expr.Variable variable) {
        try {
            variable.target = environment.getSymbol(variable.name).token;
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

    private void defineSymbol(Token token, Symbol.Type type) {
        try {
            environment.defineSymbol(token, token, type);
        } catch (LoxError error) {
            stdio().errorAtToken(error.token, error.getMessage());
        }
    }

    private void pop() {
        for(Symbol sym : environment.localSymbols()) {
            if (sym.isUnused()) {
                stdio().warningAtToken(sym.token, sym.name() + " is unused.");
            }
        }
        environment.pop();
    }



}
