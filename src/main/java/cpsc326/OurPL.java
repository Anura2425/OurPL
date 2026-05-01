package cpsc326;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class OurPL {
    
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: OurPl [script]");
            System.exit(64);
        }
        else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }
    
    public static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
    }

    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    public static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if(line == null) {
                break;
            }
            run(line);
            hadError = false;
        }
    }

    // run using ` mvn exec:java -Dexec.args="examples/p3ex01.opl" `
    public static void run(String source) {
        // Lexer
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();

        // for (Token token : tokens){
        //     System.out.println(token);
        // }
        
        // Parser
        Parser parser = new Parser(tokens);
        List<Stmt> stmts = parser.parse();

        // TODO: comment this out to get rid of printing what stmts the parser creates, just for checking rn
        // System.out.println((stmts));
        // if (expr != null){
        //     System.out.println("\nASTPrinter Output:");
        //     System.out.println(new ASTPrinter().print(expr));
        // }

        Interpreter interpreter = new Interpreter();
        interpreter.interpret(stmts);
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        hadError = true;
        System.err.println(message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    // get rid of this later, mvn getting mad at me because interpreter file was having error because this function didn't exist 😡😡😡😡😡😡😡
    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadError = true;
        hadRuntimeError = true;
    }
}
