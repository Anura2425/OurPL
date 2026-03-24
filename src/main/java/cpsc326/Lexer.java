package cpsc326;

import java.security.cert.CertPathValidatorException.BasicReason;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cpsc326.TokenType.*;

class Lexer {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;
    private static final Map<String, TokenType> keywords;

    Lexer(String source) {
        this.source = source;
    }

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("or", OR);
        keywords.put("struct", STRUCT);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("true", TRUE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("this", THIS);
        keywords.put("while", WHILE);
        keywords.put("var", VAR);        
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

     private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isAlphaNumeric(char c) {
        return c == '_' || isAlpha(c) || isDigit(c);
    }

    private void addToken(TokenType type) {
        addToken(type,null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            OurPL.error(line, "Unterminated string.");
            return;
        }
        advance();
        String str = source.substring(start + 1, current - 1);
        addToken(STRING, str);
    }

    private void number() {
        // advance through digits
        while(isDigit(peek())){advance();}

        // check if numbers exist after a dot upon encountering a dot
        if (peek() == '.' && isDigit((peekNext()))){
            //consume the dot and continue advancing through the decimals now
            advance();
            while (isDigit(peek())){advance();}
        }

        String str = source.substring(start, current);
        addToken(NUMBER, Double.parseDouble(str));
    }

    private void identifier() {
        while(isAlphaNumeric(peek())){advance();}

        String str = source.substring(start, current);
        TokenType type = keywords.get(str);

        if (type == null){
            type = IDENTIFIER;
        }

        addToken(type);
    }

    private void scanToken() {
        char character = advance();
        switch (character) {
            // single char tokens
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '+':
                addToken(PLUS);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '*':
                addToken(STAR);
                break;
            case '/':
                addToken(SLASH);
                break;
            case ';':
                addToken(SEMICOLON);
                break;

            // multi char/single char conflict tokens
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;


            // comments
            case '#':
                while (peek() != '\n' && !isAtEnd()){advance();}
                break;

            // ignore whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            // new lines
            case '\n':
                line++;
                break;

            // strings
            case '"':
                string();
                break;

            default:
                if (isDigit(character)){
                    number();
                } else if (isAlpha(character)){
                    identifier();
                } else {
                    // idk if this is needed but other characters like @ $ etc arent handled rn otherwise?
                    OurPL.error(line, "Unexpected character.");
                }
                break;
        } 
    }
}
