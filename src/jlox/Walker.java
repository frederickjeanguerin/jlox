package jlox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Walker implements Stmt.VoidVisitor, Expr.VoidVisitor, WalkerData {

    interface AstVisitor extends Stmt.WalkVisitor, Expr.WalkVisitor {

        default void enter(Expr expr) { expr.enter(this); }
        default void enter(Stmt stmt) { stmt.enter(this); }
        default void leave(Expr expr) { expr.leave(this); }
        default void leave(Stmt stmt) { stmt.leave(this); }

        void enterSequence(List<Stmt> stmts);
        void leaveSequence(List<Stmt> stmts);

        void enterStmt(Stmt stmt);
        void leaveStmt(Stmt stmt);
        void InjectWalker(WalkerData walker);
    }

    private final List<AstVisitor> visitors;
    private final List<AstVisitor> reversedVisitors;

    private final Stdio stdio;
    private int scopeDepth = 0;

    private final Stack<Stmt> stmtStack = new Stack<>();

    public Walker(List<AstVisitor> visitors, Stdio stdio) {
        this.visitors = visitors;
        this.stdio = stdio;
        this.reversedVisitors = new ArrayList<>(visitors);
        Collections.reverse(this.reversedVisitors);
        for (var visitor : visitors) {
            visitor.InjectWalker(this);
        }
    }

    public void walk(Expr expr) {
        if (expr != null) {
            for (var visitor : visitors) visitor.enter(expr);
            expr.voidVisit(this);
            for (var visitor : reversedVisitors) visitor.leave(expr);
        }
    }

    public void walkAll(List<Expr> exprs) {
        if (exprs != null)
            for (var expr : exprs) walk(expr);
    }

    public void walk(Stmt stmt) {
        if (stmt != null) {
            for (var visitor : visitors)  {
                visitor.enterStmt(stmt);
                visitor.enter(stmt);
                stmtStack.push(stmt);
            }
            stmt.voidVisit(this);
            for (var visitor : reversedVisitors)  {
                stmtStack.pop();
                visitor.leave(stmt);
                visitor.leaveStmt(stmt);
            }
        }
    }

    public void walk(List<Stmt> stmts) {
        if (stmts != null) {
            scopeDepth ++;
            for (var visitor : visitors) visitor.enterSequence(stmts);
            for (var stmt : stmts) walk(stmt);
            for (var visitor : reversedVisitors) visitor.leaveSequence(stmts);
            scopeDepth--;
        }
    }

    @Override
    public Stdio stdio() {
        return stdio;
    }

    @Override
    public int scopeDepth() {
        return scopeDepth;
    }

    @Override
    public Stmt parentStmt(int depth) {
        assert depth >= 0;
        if (stmtStack.size() > depth)
            return stmtStack.get(stmtStack.size() - 1 - depth);
        return null;
    }

    @Override
    public void visitAssignExpr(Expr.Assign assign) {
        walk(assign.value);
    }

    @Override
    public void visitBinaryExpr(Expr.Binary binary) {
        walk(binary.left);
        walk(binary.right);
    }

    @Override
    public void visitCallExpr(Expr.Call call) {
        walk(call.callee);
        walkAll(call.arguments);
    }

    @Override
    public void visitGroupingExpr(Expr.Grouping grouping) {
        walk(grouping.expression);
    }

    @Override
    public void visitLiteralExpr(Expr.Literal literal) {
        // done
    }

    @Override
    public void visitTernaryExpr(Expr.Ternary ternary) {
        walk(ternary.left);
        walk(ternary.middle);
        walk(ternary.right);
    }

    @Override
    public void visitTypeCheckExpr(Expr.TypeCheck typeCheck) {
        walk(typeCheck.value);
    }

    @Override
    public void visitUnaryExpr(Expr.Unary unary) {
        walk(unary.right);
    }

    @Override
    public void visitVariableExpr(Expr.Variable variable) {
        // done
    }

    @Override
    public void visitBlockStmt(Stmt.Block stmt) {
        walk(stmt.statements);
    }

    @Override
    public void visitContinueCatcherStmt(Stmt.ContinueCatcher stmt) {
        walk(stmt.statement);
    }

    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        walk(stmt.expression);
    }

    @Override
    public void visitFunctionStmt(Stmt.Function stmt) {
        walk(stmt.body);
    }

    @Override
    public void visitIfStmt(Stmt.If stmt) {
        walk(stmt.condition);
        walk(stmt.then);
        walk(stmt.else_);
    }

    @Override
    public void visitKeywordStmt(Stmt.Keyword stmt) {
        // done
    }

    @Override
    public void visitLastStmt(Stmt.Last stmt) {
        walk(stmt.expression);
    }

    @Override
    public void visitPrintStmt(Stmt.Print stmt) {
        walk(stmt.expression);
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        walk(stmt.value);
    }

    @Override
    public void visitVarStmt(Stmt.Var stmt) {
        walk(stmt.initializer);
    }

    @Override
    public void visitWhileStmt(Stmt.While stmt) {
        walk(stmt.condition);
        walk(stmt.body);
    }
}
