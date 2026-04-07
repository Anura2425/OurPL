package cpsc326;

abstract class Stmt {
    interface Visitor<R>{
        R visitPrintStatement(Stmt.Print Stmt);
        R visitExpressionStatement(Stmt.Expression Stmt);
        R visitVarDeclaration(Stmt.VarDecl Stmt);
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
