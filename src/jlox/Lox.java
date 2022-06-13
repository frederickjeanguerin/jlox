package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Stdio STDIO = new Stdio();
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
        if (STDIO.hasError() || interpreter.stdio.hasError())  System.exit(65);
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);

        for(;;){
            STDIO.reset();
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) break; // EOF (ctrl+D)
            run(line);
        }
    }

    private static void run(String source) {
        boolean astOnly = false;
        boolean walkOnly = false;

        if (source.startsWith("#ast")) {
            source = source.substring(4);
            astOnly = true;
        }
        if (source.startsWith("#walk")) {
            source = source.substring(5);
            walkOnly = true;
        }
        Scanner scanner = new Scanner(source, STDIO);
        List<Token> tokens = scanner.scanTokens();

        var ast = new Parser(tokens, STDIO).parse();
        if (astOnly) {
            System.out.println(new AstPrinter().print(ast));
        }
        if (STDIO.report() || astOnly) return;

        Analyzer.analyse(ast, STDIO);
        if (walkOnly && !STDIO.hasError()) {
            System.out.println("Analysis: OK");
        }
        if (STDIO.report() || walkOnly) return;

        interpreter.interpret(ast);
        interpreter.stdio.report();
    }

    // ------ Test helpers ------

    public record TestParserResult(String ast, String errors){}
    public static TestParserResult testParser(String source) {
        var error = new Stdio();
        Scanner scanner = new Scanner(source, error);
        List<Token> tokens = scanner.scanTokens();
        var ast = new Parser(tokens, error).parse();
        var astPrint = ast == null ? "" : new AstPrinter().print(ast);
        return new TestParserResult(astPrint, error.stderr());
    }

    public record TestInterpreterResult(String prints, String errors){}
    public static TestInterpreterResult testInterpreter(String source) {
        var error = new Stdio();
        Scanner scanner = new Scanner(source, error);
        List<Token> tokens = scanner.scanTokens();
        var ast = new Parser(tokens, error).parse();
        if (error.hasError()) {
            var astPrint = ast == null ? "(ast is null)" : new AstPrinter().print(ast);
            return new TestInterpreterResult(astPrint, error.stderr());
        }
        interpreter.reset();
        interpreter.interpret(ast);
        return new TestInterpreterResult(interpreter.stdio.stdout(), interpreter.stdio.stderr());
    }

}

