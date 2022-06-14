package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else {
            Stdio stdio =  (args.length == 1)
                ? runFile(args[0])
                : runPrompt();
            if (stdio.hasError()) System.exit(65);
        }
    }

    private static Stdio runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        return run(new String(bytes, Charset.defaultCharset())).report();
    }

    private static Stdio runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);
        Stdio lastStdio = null;
        for(;;){
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) break; // EOF (ctrl+D)
            lastStdio = run(line).report();
        }
        return lastStdio;
    }

    public static Stdio run(String source) {
        return run(source, RunPhase.INTERPRET);
    }

    public enum RunPhase { AST, WALK, INTERPRET}

    public static Stdio run(String source, RunPhase phase) {

        if (source.startsWith("#ast")) {
            source = source.substring(4);
            phase = RunPhase.AST;
        }
        if (source.startsWith("#walk")) {
            source = source.substring(5);
            phase = RunPhase.WALK;
        }
        boolean astOnly = phase == RunPhase.AST;
        boolean walkOnly = phase == RunPhase.WALK;

        Stdio stdio = new Stdio();
        Scanner scanner = new Scanner(source, stdio);
        List<Token> tokens = scanner.scanTokens();

        var ast = new Parser(tokens, stdio).parse();
        if (astOnly && ast != null) {
            stdio.print(new AstPrinter().print(ast));
        }
        if (astOnly || stdio.hasError()) return stdio;

        Analyzer.analyse(ast, stdio);
        if (walkOnly && !stdio.hasError()) {
            stdio.print("Analysis: OK");
        }
        if (stdio.hasError() || walkOnly) return stdio;

        interpreter.interpret(ast, stdio);
        return stdio;
    }
}

