// Automatically generated by jlox.tool.GenerateAst

package jlox;
import java.util.List;

abstract class Stmt {
  interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitContinueCatcherStmt(ContinueCatcher stmt);
    R visitExpressionStmt(Expression stmt);
    R visitFunctionStmt(Function stmt);
    R visitIfStmt(If stmt);
    R visitKeywordStmt(Keyword stmt);
    R visitLastStmt(Last stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
    R visitWhileStmt(While stmt);
  }

  static class Block extends Stmt {
    final List<Stmt> statements;

    Block ( List<Stmt> statements ) {
      this.statements = statements;
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
    <R> R visit(Visitor<R> visitor) {
      return visitor.visitContinueCatcherStmt(this);
    }
  }

  static class Expression extends Stmt {
    final Expr expression;

    Expression ( Expr expression ) {
      this.expression = expression;
    }

    @Override
    <R> R visit(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }
  }

  static class Function extends Stmt {
    final Token name;
    final List<Token> parameters;
    final List<Stmt> body;

    Function ( Token name, List<Token> parameters, List<Stmt> body ) {
      this.name = name;
      this.parameters = parameters;
      this.body = body;
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
    <R> R visit(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
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
    <R> R visit(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }
  }

    @SuppressWarnings("UnusedReturnValue")
    abstract <R> R visit(Visitor<R> visitor);
}
