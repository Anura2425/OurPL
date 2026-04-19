package cpsc326;
import java.util.List;

abstract class Stmt {
    interface Visitor<R>{
        R visitPrintStatement(Stmt.Print Stmt);
        R visitExpressionStatement(Stmt.Expression Stmt);
        R visitVarDeclaration(Stmt.VarDecl Stmt);
        R visitBlockStatement(Stmt.Block Stmt);
        R visitIfStatement(Stmt.If Stmt);
        R visitWhileStatement(Stmt.While Stmt);
    }

    static class Block extends Stmt {
        final List<Stmt> stmt;
        Block(List<Stmt> stmts){
            stmt = stmts;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    static class If extends Stmt{
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;
        If(Expr cond, Stmt thenBr, Stmt elseBr){
            condition = cond;
            thenBranch = thenBr;
            elseBranch = elseBr;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
        }
    }

    static class While extends Stmt{
        final Expr condition;
        final Stmt body;
        While(Expr cond, Stmt bod){
            condition = cond;
            body = bod;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }

    static class Print extends Stmt {
        final Expr expression;
        Print(Expr expr){
            expression = expr;
        }
        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitPrintStatement(this);
        }
    }

    static class Expression extends Stmt {
        final Expr expression;
        Expression(Expr expr){
            expression = expr;
        }
        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitExpressionStatement(this);
        }
    }

    static class VarDecl extends Stmt {
        final Token name;
        final Expr initializer;
        VarDecl(Token n, Expr i){
            initializer = i;
            name = n;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarDeclaration(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
