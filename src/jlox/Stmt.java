// Automatically generated by jlox.tool.GenerateAst

package jlox;
import java.util.List;

@SuppressWarnings("unused")
abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitContinueCatcherStmt(ContinueCatcher stmt);
    R visitClassStmt(Class stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitIfStmt(If stmt);
    R visitKeywordStmt(Keyword stmt);
    R visitLastStmt(Last stmt);
    R visitPrintStmt(Print stmt);
    R visitReturnStmt(Return stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
  }
  interface VoidVisitor {
    void visitBlockStmt(Block stmt);
    void visitContinueCatcherStmt(ContinueCatcher stmt);
    void visitClassStmt(Class stmt);
    void visitExpressionStmt(Expression stmt);
    void visitFunctionStmt(Function stmt);
    void visitIfStmt(If stmt);
    void visitKeywordStmt(Keyword stmt);
    void visitLastStmt(Last stmt);
    void visitPrintStmt(Print stmt);
    void visitReturnStmt(Return stmt);
    void visitVarStmt(Var stmt);
    void visitWhileStmt(While stmt);
  }
  interface WalkVisitor {
    void enterBlockStmt(Block stmt);
    void leaveBlockStmt(Block stmt);
    void enterContinueCatcherStmt(ContinueCatcher stmt);
    void leaveContinueCatcherStmt(ContinueCatcher stmt);
    void enterClassStmt(Class stmt);
    void leaveClassStmt(Class stmt);
    void enterExpressionStmt(Expression stmt);
    void leaveExpressionStmt(Expression stmt);
    void enterFunctionStmt(Function stmt);
    void leaveFunctionStmt(Function stmt);
    void enterIfStmt(If stmt);
    void leaveIfStmt(If stmt);
    void enterKeywordStmt(Keyword stmt);
    void leaveKeywordStmt(Keyword stmt);
    void enterLastStmt(Last stmt);
    void leaveLastStmt(Last stmt);
    void enterPrintStmt(Print stmt);
    void leavePrintStmt(Print stmt);
    void enterReturnStmt(Return stmt);
    void leaveReturnStmt(Return stmt);
    void enterVarStmt(Var stmt);
    void leaveVarStmt(Var stmt);
    void enterWhileStmt(While stmt);
    void leaveWhileStmt(While stmt);
  }
  static class Block extends Stmt {

    final List<Stmt> statements;

    Block ( List<Stmt> statements ) {
      this.statements = statements;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitBlockStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterBlockStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveBlockStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitBlockStmt(this);
    }
  }
  static class ContinueCatcher extends Stmt {

    final Stmt statement;

    ContinueCatcher ( Stmt statement ) {
      this.statement = statement;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitContinueCatcherStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterContinueCatcherStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveContinueCatcherStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitContinueCatcherStmt(this);
    }
  }
  static class Class extends Stmt {

    final Token name;
    final List<Expr.Variable> superclasses;
    final List<Stmt.Function> methods;
    Token self = null;

    Class ( Token name, List<Expr.Variable> superclasses, List<Stmt.Function> methods ) {
      this.name = name;
      this.superclasses = superclasses;
      this.methods = methods;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitClassStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterClassStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveClassStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitClassStmt(this);
    }
  }
  static class Expression extends Stmt {

    final Expr expression;

    Expression ( Expr expression ) {
      this.expression = expression;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitExpressionStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterExpressionStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveExpressionStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitExpressionStmt(this);
    }
  }
  static class Function extends Stmt {

    final Token name;
    final List<Token> parameters;
    final Stmt body;
    final String kind;

    Function ( Token name, List<Token> parameters, Stmt body, String kind ) {
      this.name = name;
      this.parameters = parameters;
      this.body = body;
      this.kind = kind;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitFunctionStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterFunctionStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveFunctionStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitFunctionStmt(this);
    }
  }
  static class If extends Stmt {

    final Expr condition;
    final Stmt then;
    final Stmt else_;

    If ( Expr condition, Stmt then, Stmt else_ ) {
      this.condition = condition;
      this.then = then;
      this.else_ = else_;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitIfStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterIfStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveIfStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitIfStmt(this);
    }
  }
  static class Keyword extends Stmt {

    final Token keyword;

    Keyword ( Token keyword ) {
      this.keyword = keyword;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitKeywordStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterKeywordStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveKeywordStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitKeywordStmt(this);
    }
  }
  static class Last extends Stmt {

    final Expr expression;

    Last ( Expr expression ) {
      this.expression = expression;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitLastStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterLastStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveLastStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitLastStmt(this);
    }
  }
  static class Print extends Stmt {

    final Expr expression;

    Print ( Expr expression ) {
      this.expression = expression;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitPrintStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterPrintStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leavePrintStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitPrintStmt(this);
    }
  }
  static class Return extends Stmt {

    final Token keyword;
    final Expr value;

    Return ( Token keyword, Expr value ) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitReturnStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterReturnStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveReturnStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitReturnStmt(this);
    }
  }
  static class Var extends Stmt {

    final Token name;
    final Expr initializer;

    Var ( Token name, Expr initializer ) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitVarStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterVarStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveVarStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitVarStmt(this);
    }
  }
  static class While extends Stmt {

    final Expr condition;
    final Stmt body;

    While ( Expr condition, Stmt body ) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    void voidVisit(VoidVisitor visitor) {
        visitor.visitWhileStmt(this);
    }

    @Override
    void enter(WalkVisitor visitor) {
        visitor.enterWhileStmt(this);
    }

    @Override
    void leave(WalkVisitor visitor) {
        visitor.leaveWhileStmt(this);
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
        return visitor.visitWhileStmt(this);
    }
  }

    abstract void voidVisit(VoidVisitor visitor);

    @SuppressWarnings("UnusedReturnValue")
    abstract <R> R visit(Visitor<R> visitor);

    abstract void enter(WalkVisitor visitor);
    abstract void leave(WalkVisitor visitor);
}
