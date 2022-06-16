package jlox;

import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.DefaultParser;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.widget.AutopairWidgets;
import org.jline.widget.AutosuggestionWidgets;

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
        // var input = new InputStreamReader(System.in);
        //var reader = new BufferedReader(input);

        var reader = getReader();

        Stdio lastStdio = null;
        Environment globalSymbols = new Environment();
        for(;;){
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) break; // EOF (ctrl+D)
            lastStdio = run(line, RunPhase.INTERPRET_MORE, globalSymbols).report();
        }
        return lastStdio;
    }

    private static LineReader getReader() throws IOException {
        try (Terminal terminal = TerminalBuilder.terminal()) {
            var completer = new StringsCompleter("#ast", "#walk");

            DefaultParser parser = new DefaultParser();
            parser.setEofOnUnclosedBracket(DefaultParser.Bracket.CURLY, DefaultParser.Bracket.ROUND, DefaultParser.Bracket.SQUARE);

            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .parser(parser)
                    .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
                    .variable(LineReader.INDENTATION, 2)   // indentation size
                    .option(LineReader.Option.INSERT_BRACKET, true)   // insert closing bracket automatically
                    .build();
            AutosuggestionWidgets autosuggestionWidgets = new AutosuggestionWidgets(reader);
            autosuggestionWidgets.enable();
            AutopairWidgets autopairWidgets = new AutopairWidgets(reader);
            autopairWidgets.enable();
            return reader;
        }
    }

    public enum RunPhase { AST, WALK, INTERPRET, INTERPRET_MORE }

    public static Stdio parse(String source) { return run(source, RunPhase.AST, null); }
    public static Stdio run(String source) { return run(source, RunPhase.INTERPRET, null); }

    public static Stdio run(String source, RunPhase phase, Environment globalSymbols) {

        if (globalSymbols == null)
            globalSymbols = new Environment();

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

        Analyzer.analyse(ast, stdio, globalSymbols);
        if (walkOnly && !stdio.hasError()) {
            stdio.print("Analysis: OK");
        }
        if (stdio.hasError() || walkOnly) return stdio;

        if (phase != RunPhase.INTERPRET_MORE) interpreter.reset();
        interpreter.interpret(ast, stdio);
        return stdio;
    }
}

