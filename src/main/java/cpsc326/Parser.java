package cpsc326;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.text.AbstractDocument.LeafElement;

import static cpsc326.TokenType.*;

class Parser {
    private static class ParseError extends RuntimeException{ }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        return program();
    }

    private List<Stmt> program(){
        try {
            List<Stmt> declarations = new ArrayList<Stmt>();
            while(!isAtEnd()){
                declarations.add(declaration());
            }
            consume(EOF, "Expected EOF");
            return declarations;
        } catch (ParseError error) {
            return null;
        }
    }

    private Stmt declaration(){
        try{
            if(match(FUN)) {
                return funDecl();
            }
            if(match(VAR)){
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error){
            synchronize();
            return null;
        }
    }

    private Stmt funDecl(){
        return function();
    }

    private Stmt function(){
        Token name = consume(IDENTIFIER, "Expect function name identifier.");
        consume(LEFT_PAREN, "Except '(' after identifier.");

        List<Token> parameters = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            do{
                if (parameters.size() >= 255){
                    error(peek(), "Can't have more than 255 parameters.");
                }
                parameters.add(consume(IDENTIFIER, "Except parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "Expect ')' after parameters of function.");
        consume(LEFT_BRACE, "Expect '{' before body.");

        Stmt.Block bodyBlock = (Stmt.Block)block();
        List<Stmt> body = bodyBlock.statements;
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)){
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(keyword, value);
    }

    private Stmt varDeclaration(){
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;
        if(match(EQUAL)){
            initializer = expression();
        }
        consume(SEMICOLON, "Expected ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return block();
        // if it doesnt hit any of the above statement types it should just default to an expression statement
        return expressionStatement();
    }
    
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition.");
        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt forStatement(){
        consume(LEFT_PAREN, "Expected '(' after 'for'.");
        // initializer
        Stmt init;
        if(match(SEMICOLON)){
            init = null;
        } else if (match(VAR)){
            init = varDeclaration();
        } else {
            init = expressionStatement();
        }
        // condition
        Expr cond = null;
        if (!check(SEMICOLON)) {
            cond = expression();
        }
        consume(SEMICOLON, "Expected second ';'.");
        // incrementor
        Expr inc = null;
        if (!check(RIGHT_PAREN)) {
            inc = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for clauses.");
        Stmt body = statement();

        // NOTE: if there is an incrementor it should only execute after the body the first time
        if (inc != null) {
            body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(inc)));
        }

        // NOTE: if condition doesn't exist (null) then this should act as basically a while true loop (infinite)
        if (cond == null){
            cond = new Expr.Literal(true);
        }
        body = new Stmt.While(cond, body);

        // NOTE: if there is an initializer it runs once before the loop and shouldnt happen agaijn 
        if (init != null) {
            body = new Stmt.Block(Arrays.asList(init, body));
        }

        return body;
    }

    private Stmt whileStatement(){
        consume(LEFT_PAREN, "Expected '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after while condition.");
        Stmt body = statement();
        return new Stmt.While(condition, body);
    }

    // helper cuz both blockStatement and function need to parse list of statements and I dont wanna write it twice :>
    private Stmt block() {
        List<Stmt> statements = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()){
            statements.add(declaration());
        }
        consume(RIGHT_BRACE, "Expected '}' to end block.");
        return new Stmt.Block(statements);
    }

    private Stmt expressionStatement(){
        Expr expr = expression();
        if(match(SEMICOLON)){
            return new Stmt.Expression(expr);
        }
        throw error(peek(), "Expected ';'");
    }

    private Stmt printStatement(){
        Expr expr = expression();
        if(match(SEMICOLON)){
            return new Stmt.Print(expr);
        }
        throw error(peek(), "Expected ';'");
    }

    
    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = logic_or();
        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();
            //check if the thing on the left is acc a variable
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }
            // error if something like 1 + 2 = 3;
            error(equals, "Invalid assignment target.");
        }
        // if there was no equal just return the expression that was parsed
        return expr;
    }

    private Expr logic_or() {
        Expr expr = logic_and();
        while (match(OR)) {
            Token operator = previous();
            Expr right = logic_and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr logic_and() {
        Expr expr = equality();
        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
        while(match(BANG_EQUAL, EQUAL_EQUAL)){
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(PLUS, MINUS)){
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)){
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
    }

    private Expr call(){
        Expr expr = primary();

        while(true){
            if(match(LEFT_PAREN)){
                expr = finishCall(expr);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expect ')' after arguments.");
        return new Expr.Call(callee, paren, arguments);
    }

    private Expr primary() {
        if (match(IDENTIFIER)){
            Expr var = new Expr.Variable(previous());
            return var;
        }
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        // grouping
        if (match(LEFT_PAREN)) {
            Token leftParen = previous();
            Expr expr = expression();
            Token rightParen = consume(RIGHT_PAREN, "Expected a ')' after grouping.");
            return new Expr.Grouping(leftParen, expr, rightParen);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match (TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private Token consume(TokenType type, String message) {
        if(check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        OurPL.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
        while(!isAtEnd()) {
            if (previous().type == SEMICOLON) return;
            switch(peek().type) {
                case STRUCT:
                case FOR:
                case FUN:
                case IF:
                case PRINT:
                case RETURN:
                case VAR:
                case WHILE:
                    return;
            }
            
            advance();
        }
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return type == EOF;
        return peek().type == type;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}