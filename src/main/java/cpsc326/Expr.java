package cpsc326;

abstract class Expr {
    interface Visitor<R> {
        R visitBinaryExpr(Binary expr);

        R visitGroupingExpr(Grouping expr);

        R visitLiteralExpr(Literal expr);

        R visitUnaryExpr(Unary expr);
    }

    static class Unary extends Expr {
        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
        
        final Expr left;
        final Token operator;
        final Expr right;
    }

    static class Grouping extends Expr {
        Grouping(Token left_paren, Expr expression, Token right_paren){
            this.left_paren = left_paren;
            this.expression = expression;
            this.right_paren = right_paren;
        }

        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitGroupingExpr(this);
        }

        final Token left_paren;
        final Expr expression;
        final Token right_paren;
    }

    static class Literal extends Expr {
        Literal(Literal value){
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor){
            return visitor.visitLiteralExpr(this);
        }

        final Literal value;
    }

    abstract <R> R accept(Visitor<R> visitor);
}