package cpsc326;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Hw4FunctionTest {

    @BeforeEach
    void resetFlags() {
        OurPL.hadError = false;
        OurPL.hadRuntimeError = false;
    }

    private List<Token> lex(String source) {
        return new Lexer(source).scanTokens();
    }

    private List<Stmt> parse(String source) {
        OurPL.hadError = false;
        return new Parser(lex(source)).parse();
    }

    private ParseOutcome parseWithCapturedErr(String source) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setErr(new PrintStream(err));
        try {
            return new ParseOutcome(parse(source), err.toString().trim());
        } finally {
            System.setErr(originalErr);
        }
    }

    private EvalOutcome interpret(String source) {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        try {
            new Interpreter().interpret(parse(source));
            return new EvalOutcome(out.toString().trim(), err.toString().trim());
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    private void assertCleanRun(EvalOutcome out) {
        assertFalse(OurPL.hadError);
        assertFalse(OurPL.hadRuntimeError);
        assertTrue(out.stderr.isEmpty());
    }

    private void assertRuntimeFailure(EvalOutcome out) {
        assertFalse(OurPL.hadError);
        assertTrue(OurPL.hadRuntimeError);
        assertFalse(out.stderr.isBlank());
        assertFalse(out.stderr.isEmpty());
    }

    private void assertParseFailure(ParseOutcome out) {
        assertTrue(OurPL.hadError);
        assertFalse(out.stderr.isBlank());
        assertFalse(out.stderr.contains("Exception"));
    }

    @Test
    void parsesFunctionDeclarationWithParamsAndReturnBody() {
        List<Stmt> statements = parse("fun add(a, b) { return a + b; }");

        assertFalse(OurPL.hadError);
        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof Stmt.Function);

        Stmt.Function function = (Stmt.Function) statements.get(0);
        assertEquals("add", function.name.lexeme);
        assertEquals(2, function.params.size());
        assertEquals("a", function.params.get(0).lexeme);
        assertEquals("b", function.params.get(1).lexeme);
        assertEquals(1, function.body.size());
        assertTrue(function.body.get(0) instanceof Stmt.Return);

        Stmt.Return returnStmt = (Stmt.Return) function.body.get(0);
        assertTrue(returnStmt.value instanceof Expr.Binary);
        assertEquals("return", returnStmt.keyword.lexeme);

        Expr.Binary sum = (Expr.Binary) returnStmt.value;
        assertEquals(TokenType.PLUS, sum.operator.type);
        assertTrue(sum.left instanceof Expr.Variable);
        assertEquals("a", ((Expr.Variable) sum.left).name.lexeme);
        assertTrue(sum.right instanceof Expr.Variable);
        assertEquals("b", ((Expr.Variable) sum.right).name.lexeme);
    }

    @Test
    void parsesCallExpressionWithMultipleArguments() {
        List<Stmt> statements = parse("add(1, 2 * 3, \"x\");");

        assertFalse(OurPL.hadError);
        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof Stmt.Expression);

        Expr expression = ((Stmt.Expression) statements.get(0)).expression;
        assertTrue(expression instanceof Expr.Call);

        Expr.Call call = (Expr.Call) expression;
        assertTrue(call.callee instanceof Expr.Variable);
        assertEquals("add", ((Expr.Variable) call.callee).name.lexeme);
        assertEquals(3, call.arguments.size());
        assertTrue(call.arguments.get(0) instanceof Expr.Literal);
        assertEquals(1.0, ((Expr.Literal) call.arguments.get(0)).value);
        assertTrue(call.arguments.get(1) instanceof Expr.Binary);
        assertTrue(call.arguments.get(2) instanceof Expr.Literal);
        assertEquals("x", ((Expr.Literal) call.arguments.get(2)).value);

        Expr.Binary product = (Expr.Binary) call.arguments.get(1);
        assertEquals(TokenType.STAR, product.operator.type);
    }

    @Test
    void parsesFunctionDeclarationWithNoParameters() {
        List<Stmt> statements = parse("fun hello() { print \"hi\"; }");

        assertFalse(OurPL.hadError);
        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof Stmt.Function);

        Stmt.Function function = (Stmt.Function) statements.get(0);
        assertEquals("hello", function.name.lexeme);
        assertTrue(function.params.isEmpty());
        assertEquals(1, function.body.size());
        assertTrue(function.body.get(0) instanceof Stmt.Print);

        Expr printValue = ((Stmt.Print) function.body.get(0)).expression;
        assertTrue(printValue instanceof Expr.Literal);
        assertEquals("hi", ((Expr.Literal) printValue).value);
    }

    @Test
    void parsesCallExpressionWithNoArguments() {
        List<Stmt> statements = parse("hello();");

        assertFalse(OurPL.hadError);
        assertEquals(1, statements.size());

        Expr expression = ((Stmt.Expression) statements.get(0)).expression;
        assertTrue(expression instanceof Expr.Call);

        Expr.Call call = (Expr.Call) expression;
        assertTrue(call.callee instanceof Expr.Variable);
        assertEquals("hello", ((Expr.Variable) call.callee).name.lexeme);
        assertTrue(call.arguments.isEmpty());
        assertEquals(TokenType.RIGHT_PAREN, call.paren.type);
    }

    @Test
    void parsesReturnWithoutValueAsNullReturnValue() {
        List<Stmt> statements = parse("fun nothing() { return; }");

        assertFalse(OurPL.hadError);
        Stmt.Function function = (Stmt.Function) statements.get(0);
        assertEquals("nothing", function.name.lexeme);
        assertEquals(1, function.body.size());
        Stmt.Return returnStmt = (Stmt.Return) function.body.get(0);
        assertEquals("return", returnStmt.keyword.lexeme);
        assertNull(returnStmt.value);
    }

    @Test
    void interpretsUserFunctionReturnValue() {
        EvalOutcome out = interpret("""
            fun add(a, b) {
              return a + b;
            }
            print add(2, 3);
            """);

        assertCleanRun(out);
        assertEquals("5", out.stdout);
    }

    @Test
    void returnWithoutValueProducesNil() {
        EvalOutcome out = interpret("""
            fun nothing() {
              return;
            }
            print nothing();
            """);

        assertCleanRun(out);
        assertEquals("nil", out.stdout);
    }

    @Test
    void functionWithoutExplicitReturnProducesNil() {
        EvalOutcome out = interpret("""
            fun empty() {}
            print empty();
            """);

        assertCleanRun(out);
        assertEquals("nil", out.stdout);
    }

    @Test
    void printFunctionObjectUsesFunctionName() {
        EvalOutcome out = interpret("""
            fun greet() {}
            print greet;
            """);

        assertCleanRun(out);
        assertTrue(out.stdout.contains("greet"));
    }

    @Test
    void returnExitsFunctionBeforeLaterStatements() {
        EvalOutcome out = interpret("""
            fun first() {
              print "before";
              return "done";
              print "after";
            }
            print first();
            """);

        assertCleanRun(out);
        assertEquals("before\ndone", out.stdout);
        assertFalse(out.stdout.contains("after"));
    }

    @Test
    void supportsRecursiveFunctionCalls() {
        EvalOutcome out = interpret("""
            fun fact(n) {
              if (n <= 1) return 1;
              return n * fact(n - 1);
            }
            print fact(5);
            """);

        assertCleanRun(out);
        assertEquals("120", out.stdout);
    }

    @Test
    void functionCanReturnAnotherCallResult() {
        EvalOutcome out = interpret("""
            fun inner() {
              return 7;
            }
            fun outer() {
              return inner();
            }
            print outer();
            """);

        assertCleanRun(out);
        assertEquals("7", out.stdout);
    }

    @Test
    void functionsCanCallEarlierDeclaredFunctions() {
        EvalOutcome out = interpret("""
            fun double(n) {
              return n * 2;
            }
            fun addDouble(a, b) {
              return double(a) + double(b);
            }
            print addDouble(2, 3);
            """);

        assertCleanRun(out);
        assertEquals("10", out.stdout);
    }

    @Test
    void nestedCallsEvaluateArgumentsBeforeOuterCall() {
        EvalOutcome out = interpret("""
            fun add(a, b) {
              return a + b;
            }
            print add(add(1, 2), add(3, 4));
            """);

        assertCleanRun(out);
        assertEquals("10", out.stdout);
    }

    @Test
    void parameterBindingShadowsGlobalVariableInsideFunction() {
        EvalOutcome out = interpret("""
            var value = "global";
            fun echo(value) {
              print value;
            }
            echo("param");
            print value;
            """);

        assertCleanRun(out);
        assertEquals("param\nglobal", out.stdout);
    }

    @Test
    void functionCanAssignGlobalVariable() {
        EvalOutcome out = interpret("""
            var count = 0;
            fun increment() {
              count = count + 1;
            }
            increment();
            increment();
            print count;
            """);

        assertCleanRun(out);
        assertEquals("2", out.stdout);
    }

    @Test
    void functionDeclaredInsideFunctionCanBeCalledInsideThatFunction() {
        EvalOutcome out = interpret("""
            fun outer() {
              fun inner() {
                return "inner";
              }
              return inner();
            }
            print outer();
            """);

        assertCleanRun(out);
        assertEquals("inner", out.stdout);
    }

    @Test
    void functionValueCanBeStoredAndCalledAfterBlock() {
        EvalOutcome out = interpret("""
            var saved;
            {
              fun local() {
                return 3;
              }
              saved = local;
            }
            print saved();
            """);

        assertCleanRun(out);
        assertEquals("3", out.stdout);
    }

    @Test
    void functionCanReturnFunctionAndResultCanBeCalled() {
        EvalOutcome out = interpret("""
            fun make() {
              fun inner() {
                return 8;
              }
              return inner;
            }
            print make()();
            """);

        assertCleanRun(out);
        assertEquals("8", out.stdout);
    }

    @Test
    void functionCanReceiveFunctionArgumentAndCallIt() {
        EvalOutcome out = interpret("""
            fun apply(fn, value) {
              return fn(value);
            }
            fun double(n) {
              return n * 2;
            }
            print apply(double, 4);
            """);

        assertCleanRun(out);
        assertEquals("8", out.stdout);
    }

    @Test
    void argumentExpressionsAreEvaluatedLeftToRightBeforeCall() {
        EvalOutcome out = interpret("""
            var x = 0;
            fun show(a, b) {
              print a;
              print b;
              print x;
            }
            show(x = x + 1, x = x + 1);
            """);

        assertCleanRun(out);
        assertEquals("1\n2\n2", out.stdout);
    }

    @Test
    void earlierArgumentRuntimeErrorStopsLaterArgumentEvaluationAndCall() {
        EvalOutcome out = interpret("""
            var x = 0;
            fun show(a, b) {
              print "body";
            }
            show(missing, x = 1);
            print x;
            """);

        assertRuntimeFailure(out);
        assertTrue(out.stdout.isEmpty());
        assertFalse(out.stderr.isEmpty());
    }

    @Test
    void blockLocalFunctionDoesNotLeakOutsideBlock() {
        EvalOutcome out = interpret("""
            {
              fun local() {
                print 1;
              }
              local();
            }
            local();
            """);

        assertRuntimeFailure(out);
        assertEquals("1", out.stdout);
        assertFalse(out.stderr.isEmpty());
    }

    @Test
    void callBeforeFunctionDeclarationIsRuntimeError() {
        EvalOutcome out = interpret("""
            print later();
            fun later() {
              return 1;
            }
            """);

        assertRuntimeFailure(out);
        assertTrue(out.stdout.isEmpty());
        assertFalse(out.stderr.isEmpty());
    }

    @Test
    void arityErrorDoesNotExecuteFunctionBody() {
        EvalOutcome out = interpret("""
            fun one(a) {
              print "body";
            }
            one();
            """);

        assertRuntimeFailure(out);
        assertTrue(out.stdout.isEmpty());
        assertFalse(out.stderr.contains("body"));
    }

    // @Test
    // void runtimeErrorWhenCallingNonCallableValue() {
    //     EvalOutcome out = interpret("""
    //         var value = 3;
    //         value();
    //         """);

    //     assertRuntimeFailure(out);
    //     assertTrue(out.stdout.isEmpty());
    //     assertFalse(out.stderr.isEmpty());
    // }

    @Test
    void runtimeErrorWhenFunctionReceivesTooFewArguments() {
        EvalOutcome out = interpret("""
            fun one(a) {
              return a;
            }
            one();
            """);

        assertRuntimeFailure(out);
        assertTrue(out.stdout.isEmpty());
        assertFalse(out.stderr.isEmpty());
    }

    @Test
    void runtimeErrorWhenFunctionReceivesTooManyArguments() {
        EvalOutcome out = interpret("""
            fun one(a) {
              return a;
            }
            one(1, 2);
            """);

        assertRuntimeFailure(out);
        assertTrue(out.stdout.isEmpty());
        assertFalse(out.stderr.isEmpty());
    }

    @Test
    void clockIsCallableWithNoArgumentsAndReportsSecondsScale() {
        EvalOutcome out = interpret("print clock() < 10000000000;");

        assertCleanRun(out);
        assertEquals("true", out.stdout);
    }

    @Test
    void clockRejectsArgumentsByArity() {
        EvalOutcome out = interpret("clock(1);");

        assertRuntimeFailure(out);
        assertTrue(out.stdout.isEmpty());
        assertFalse(out.stderr.isEmpty());
    }

    @Test
    void userFunctionCanShadowNativeClock() {
        EvalOutcome out = interpret("""
            fun clock() {
              return 1;
            }
            print clock();
            """);

        assertCleanRun(out);
        assertEquals("1", out.stdout);
    }

    // @Test
    // void variableCanShadowNativeClockAndThenCannotBeCalled() {
    //     EvalOutcome out = interpret("""
    //         var clock = 123;
    //         clock();
    //         """);

    //     assertRuntimeFailure(out);
    //     assertTrue(out.stdout.isEmpty());
    //     assertFalse(out.stderr.isEmpty());
    // }

    @Test
    void exactly255ParametersIsAccepted() {
        String params = commaSeparated("p", 255);
        List<Stmt> statements = parse("fun maxParams(" + params + ") { return nil; }");

        assertFalse(OurPL.hadError);
        assertEquals(1, statements.size());
        assertTrue(statements.get(0) instanceof Stmt.Function);
        Stmt.Function function = (Stmt.Function) statements.get(0);
        assertEquals(255, function.params.size());
        assertEquals("p0", function.params.get(0).lexeme);
        assertEquals("p254", function.params.get(254).lexeme);
        assertEquals(1, function.body.size());
        assertTrue(function.body.get(0) instanceof Stmt.Return);
    }

    @Test
    void moreThan255ParametersReportsErrorButParserStillReturnsStatement() {
        String params = commaSeparated("p", 256);
        ParseOutcome out = parseWithCapturedErr("fun tooMany(" + params + ") { return nil; }");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertEquals(1, out.statements.size());
        assertNotNull(out.statements.get(0));
        assertTrue(out.statements.get(0) instanceof Stmt.Function);
        assertEquals(256, ((Stmt.Function) out.statements.get(0)).params.size());
    }

    @Test
    void exactly255ArgumentsIsAcceptedByParser() {
        String args = commaSeparated("a", 255);
        List<Stmt> statements = parse("call(" + args + ");");

        assertFalse(OurPL.hadError);
        assertEquals(1, statements.size());

        Expr expression = ((Stmt.Expression) statements.get(0)).expression;
        assertTrue(expression instanceof Expr.Call);
        Expr.Call call = (Expr.Call) expression;
        assertEquals(255, call.arguments.size());
        assertTrue(call.arguments.get(0) instanceof Expr.Variable);
        assertEquals("a0", ((Expr.Variable) call.arguments.get(0)).name.lexeme);
        assertTrue(call.arguments.get(254) instanceof Expr.Variable);
        assertEquals("a254", ((Expr.Variable) call.arguments.get(254)).name.lexeme);
    }

    @Test
    void moreThan255ArgumentsReportsErrorButParserStillReturnsCall() {
        String args = commaSeparated("a", 256);
        ParseOutcome out = parseWithCapturedErr("call(" + args + ");");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertEquals(1, out.statements.size());
        assertNotNull(out.statements.get(0));

        Expr expression = ((Stmt.Expression) out.statements.get(0)).expression;
        assertTrue(expression instanceof Expr.Call);
        assertEquals(256, ((Expr.Call) expression).arguments.size());
    }

    @Test
    void missingFunctionNameReportsParseError() {
        ParseOutcome out = parseWithCapturedErr("fun () {}");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertEquals(1, out.statements.size());
        assertNull(out.statements.get(0));
    }

    @Test
    void missingLeftParenAfterFunctionNameReportsParseError() {
        ParseOutcome out = parseWithCapturedErr("fun bad {}");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertEquals(1, out.statements.size());
        assertNull(out.statements.get(0));
    }

    @Test
    void missingParameterNameReportsParseError() {
        ParseOutcome out = parseWithCapturedErr("fun bad(a, ) {}");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertEquals(1, out.statements.size());
        assertNull(out.statements.get(0));
    }

    @Test
    void missingFunctionBodyBraceReportsParseError() {
        ParseOutcome out = parseWithCapturedErr("fun bad() print 1;");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertFalse(out.statements.isEmpty());
    }

    @Test
    void missingRightParenInCallReportsParseError() {
        ParseOutcome out = parseWithCapturedErr("call(1, 2;");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertEquals(1, out.statements.size());
        assertNull(out.statements.get(0));
    }

    @Test
    void trailingCommaInCallReportsParseError() {
        ParseOutcome out = parseWithCapturedErr("call(1, );");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertEquals(1, out.statements.size());
        assertNull(out.statements.get(0));
    }

    @Test
    void missingSemicolonAfterReturnValueReportsParseError() {
        ParseOutcome out = parseWithCapturedErr("fun bad() { return 1 }");

        assertParseFailure(out);
        assertFalse(out.stderr.isEmpty());
        assertEquals(1, out.statements.size());
        assertNull(out.statements.get(0));
    }

    private String commaSeparated(String prefix, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> prefix + i)
            .reduce((left, right) -> left + ", " + right)
            .orElse("");
    }

    private static final class ParseOutcome {
        final List<Stmt> statements;
        final String stderr;

        ParseOutcome(List<Stmt> statements, String stderr) {
            this.statements = statements;
            this.stderr = stderr;
        }
    }

    private static final class EvalOutcome {
        final String stdout;
        final String stderr;

        EvalOutcome(String stdout, String stderr) {
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
