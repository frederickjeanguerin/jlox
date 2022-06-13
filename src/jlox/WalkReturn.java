package jlox;

public class WalkReturn extends Walk.Base<WalkReturn.State> {

    record State(boolean insideFunction) {
        boolean returnPermitted() { return insideFunction; }
    }

    public WalkReturn() {
        super(new State(false));
    }

    @Override
    public void enterFunctionStmt(Stmt.Function stmt) {
        enterState(new State(true));
    }

    @Override
    public void leaveFunctionStmt(Stmt.Function stmt) {
        leaveState();
    }

    @Override
    public void enterReturnStmt(Stmt.Return stmt) {
        if (! state().returnPermitted()) {
            stdio().errorAtToken(stmt.keyword, "Return outside function.");
        }
    }
}
