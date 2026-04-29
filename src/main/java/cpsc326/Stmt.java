package cpsc326;
import java.util.List;

abstract class Stmt {
    interface Visitor<R>{
        R visitPrintStatement(Stmt.Print Stmt);
        R visitExpressionStatement(Stmt.Expression Stmt);
        R visitVarStatement(Stmt.Var Stmt);
        R visitBlockStatement(Stmt.Block Stmt);
        R visitIfStatement(Stmt.If Stmt);
        R visitWhileStatement(Stmt.While Stmt);
        R visitFunctionStatement(Stmt.Function Stmt);
        R visitReturnStatement(Stmt.Return Stmt);
    }

    static class Function extends Stmt {
        Token name;
        List<Token> params;
        List<Stmt> body;
        Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
           return visitor.visitFunctionStatement(this);
        }
    }

    static class Return extends Stmt{
        final Token keyword;
        final Expr value;
        Return(Token keyword, Expr value){
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitReturnStatement(this);
        }
    }

    static class Block extends Stmt {
        final List<Stmt> statements;
        Block(List<Stmt> stmts){
            statements = stmts;
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

    static class Var extends Stmt {
        final Token name;
        final Expr initializer;
        Var(Token n, Expr i){
            initializer = i;
            name = n;
        }
        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStatement(this);
        }
    }

    abstract <R> R accept(Visitor<R> visitor);
}
