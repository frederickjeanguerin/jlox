package jlox;

import java.util.HashMap;
import java.util.Map;

public class WalkReturn extends Walk.Base<WalkReturn.State> {

    private static final AstPrinter astPrinter = new AstPrinter(" ");

    /*
        When we enter a new function, we create a new Map.
        This map contains the parent block or statement of every 'return' encountered
        together with the matching return.

        When later we encounter a new statement, we check its parent stmt.
        If its parent is the same as a previous return, then we know for sure
        it is unreachable code.

        There is no map created before entering a new function declaration, it is null.
        Thus, if a return is matched and the map is null, we know we are outside a function.
     */
    record State(Map<Stmt, Stmt.Return> returnParents) {
        boolean returnPermitted() { return returnParents != null; }
    }

    public WalkReturn() {
        super(new State(null));
    }

    @Override
    public void enterFunctionStmt(Stmt.Function stmt) {
        enterState(new State(new HashMap<>()));
    }

    @Override
    public void leaveFunctionStmt(Stmt.Function stmt) {
        leaveState();
    }

    @Override
    public void enterReturnStmt(Stmt.Return stmt) {
        if (!state().returnPermitted()) {
            stdio().errorAtToken(stmt.keyword, "Return outside function.");
        } else {
            state().returnParents.put(parentStmt(0), stmt);
        }
    }

    @Override
    public void enterStmt(Stmt stmt) {
        // Check for unreachable code.
        // Since we don't have a marking token for every statement, we flag the error at the return statement.
        boolean afterReturn = state().returnPermitted() && state().returnParents.containsKey(parentStmt(0));
        if (afterReturn) {
            Token targetReturn = state().returnParents.get(parentStmt(0)).keyword;
            stdio().warningAtToken(targetReturn,
                    "Unreachable code after return: '%s'.".formatted(astPrinter.print(stmt)));
        }
    }
}
