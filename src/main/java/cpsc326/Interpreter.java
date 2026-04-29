package cpsc326;

import java.util.List;
import java.util.ArrayList;

import cpsc326.Expr.Variable;

class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void>{
    final Environment globals = new Environment();
    private Environment environment = globals;


    void interpret(List<Stmt> statements) {
        try {
            for(Stmt statement : statements){
                if (statement != null){
                    statement.accept(this);
                }
            }
        } catch (RuntimeError error) {
            OurPL.runtimeError(error);
        }
    }

    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitFunctionStatement(Stmt.Function stmt){
        OurPLFunction function = new OurPLFunction(stmt);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr){
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for(Expr argument : expr.arguments){
            arguments.add(evaluate(argument));
        }
        if(!(callee instanceof OurPLCallable)){
            throw new RuntimeError(expr.paren, "Should only call functions/classes.");
        }
        OurPLCallable function = (OurPLCallable)callee;

        // check for arity thing
        if (arguments.size() != function.arity()){
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + "but instead got size of " + arguments.size());
        }
        return function.call(this, arguments);
    }

    @Override 
    public Void visitReturnStatement(Stmt.Return stmt){
        Object value = null;
        if (stmt.value != null) value = evaluate(stmt.value);
        throw new Return(value);
    }

    @Override
    public Void visitVarStatement(Stmt.Var stmt){
        Object value = null;
        if(stmt.initializer != null){
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }
    @Override
    public Void visitBlockStatement(Stmt.Block stmt){
        Environment previous = this.environment;
        try {
            this.environment = new Environment(previous);
            for (Stmt statement : stmt.statements){
                statement.accept(this);
            }
        } finally {
            this.environment = previous;
        }
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStatement(Stmt.Expression stmt){
        evaluate(stmt.expression);
        return null;
    }
    @Override   
    public Void visitIfStatement(Stmt.If stmt){
        if(isTruthy(evaluate(stmt.condition))){ 
            stmt.thenBranch.accept(this);
        } else if (stmt.elseBranch != null) {
            stmt.elseBranch.accept(this);
        }
        return null;
    }
    @Override
    public Void visitWhileStatement(Stmt.While stmt){
        while(isTruthy(evaluate(stmt.condition))){
            stmt.body.accept(this);
        }
        return null;
    }
    @Override
    public Void visitPrintStatement(Stmt.Print stmt){
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr){
        return environment.get(expr.name);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr){
        Object left = evaluate(expr.left);
        switch(expr.operator.type){
            case OR:
                // short circuit or if left is true
                if(isTruthy(left)){
                    return true;
                } else {
                    return isTruthy(evaluate(expr.right));
                }
            case AND:
                // short circuit and if left is false
                if(!isTruthy(left)){
                    return false;
                } else {
                    return isTruthy(evaluate(expr.right));
                }
        }
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch(expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
            case BANG:
                return !isTruthy(right);
        }

        return null;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator,"Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator,"Operandd must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean)object;
        return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null) return true;
        if (left == null) return false;

        return left.equals(right);
    }

    private String stringify(Object object) {
        if (object == null) {
            return "nil";
        }

        if (object instanceof Double) {
            String text = object.toString();
            if(text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch(expr.operator.type){
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left,right);
            case EQUAL_EQUAL:
                return isEqual(left,right);
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double)left - (double)right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double)left * (double)right;
            
        }

        return null;
    }
}
