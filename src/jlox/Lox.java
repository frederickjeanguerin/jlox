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
        Scanner scanner = new Scanner(source, STDIO);
        List<Token> tokens = scanner.scanTokens();

        var ast = new Parser(tokens, STDIO).parse();
        if (STDIO.report()) return;

        interpreter.interpret(ast);
        interpreter.stdio.report();
    }

    // ------ Test helpers ------

    public record TestResult(String ast, String errors){}

    public static TestResult testParser(String source) {
        var error = new Stdio();
        Scanner scanner = new Scanner(source, error);
        List<Token> tokens = scanner.scanTokens();
        var ast = new Parser(tokens, error).parse();
        var astPrint = ast == null ? "" : new AstPrinter().print(ast);
        return new TestResult(astPrint, error.stderr());
    }
}

