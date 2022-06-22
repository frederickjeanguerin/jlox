package jlox;

import java.util.*;

public class WalkReturn extends Walk.Base {

    enum Return { CANNOT, VOID_ONLY, ANY }
    private final Set<Stmt> deadCodes = new HashSet<>();

    private final Stack<Return> returns = new Stack<>();

    public WalkReturn() {
        returns.push(Return.CANNOT);
    }

    @Override
    public void enterFunctionStmt(Stmt.Function stmt) {
        boolean isInit = stmt.name.lexeme().equals("init") && stmt.kind.equals("method");
        returns.push(isInit ? Return.VOID_ONLY : Return.ANY);
    }

    @Override
    public void leaveFunctionStmt(Stmt.Function stmt) {
        returns.pop();
    }

    @Override
    public void enterLambdaExpr(Expr.Lambda lambda) {
        returns.push(Return.ANY);
    }

    @Override
    public void leaveLambdaExpr(Expr.Lambda expr) {
        returns.pop();
    }

    @Override
    public void enterReturnStmt(Stmt.Return stmt) {
        if (returns.peek() == Return.CANNOT) {
            stdio().errorAtToken(stmt.keyword, "Return outside function.");
        } else if (stmt.value != null && returns.peek() == Return.VOID_ONLY) {
            stdio().errorAtToken(stmt.keyword, "Initializers cannot return something.");
        } else {
            checkDeadCode(0, stmt, stmt.keyword);
        }
    }

    private void checkDeadCode(int depth, Stmt noReturn, Token token)
    {
        /*
            We flag all statements following the noReturn statement,
            provided these statements appears in a block, at the same level.

            Then we level up, because we must account for the following case:

                {
                    {
                        stmt1;
                        return;
                        stmt3;  // dead code
                    }
                    stmt4;      // dead code
                }

            Note that we don't level-up for dead code after if-else, while loop and for loop.

            For the loops, we never know for sure that the loop block will be executed
            without evaluating its conditions (we might do that, but not for the moment).
            Hence the enclosed return migth not propagate up.

                {
                    while (someCond) {
                        stmt1;
                        return;     // TODO We should probably flag this as a warning
                    }
                    stmt2;          // if the loop is not entered, then this will execute.
                }

            For if-else, we could level-up the case where both are returning, but we are not checking it
            TODO: check if both then and else have dead code and if so, level-up.

                {
                    if (cond) {
                        stmt1;
                        return;
                    } else {
                        stmt2;
                        return;
                    }
                    stmt3;          // TODO: DEAD CODE
                }

            We put dead code in a Set, to not report it twice. Because we might meet more than one return;

                {
                    return 1;
                    return 2;
                    return 3;   // This could be reported twice.
                    stmt4;      // and this thrice.
                }

         */
        if (parentStmt(depth) instanceof Stmt.Block block) {
            boolean deadCodeFollowing = false;
            for (var stmt : block.statements) {
                if ( stmt == noReturn) {
                    deadCodeFollowing = true;
                    continue;
                }
                if (deadCodeFollowing && deadCodes.add(stmt)) {
                    // We report the warning at the return token causing the dead code, because
                    // we have no information token or source code line available for the dead code statement.
                    // TODO (one day) add line info for every ast element.
                    stdio().warningAtToken(token, "Dead code: " + Stdio.astOneLinePrinter.print(stmt));
                }
            }
            // Level up
            checkDeadCode(depth + 1, block, token);
        }
    }
}
