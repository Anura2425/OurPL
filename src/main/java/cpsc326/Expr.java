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
        Binary(Expr Left, Token Operator, Expr Right) {
            // TODO complete class
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
        // TODO complete class
    }

    static class Grouping extends Expr {
        // TODO complete class
    }

    static class Literal extends Expr {
        // TODO complete class
    }

    abstract <R> R accept(Visitor<R> visitor);
}