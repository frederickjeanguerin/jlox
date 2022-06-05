package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    private static final Error error = new Error();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
        if (error.hadError())  System.exit(65);
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    private static void runPrompt() throws IOException {
        var input = new InputStreamReader(System.in);
        var reader = new BufferedReader(input);

        for(;;){
            System.out.println();
            System.out.print("> ");
            var line = reader.readLine();
            if (line == null) break; // EOF (ctrl+D)
            if (!line.isBlank()) {
                error.reset();
                run(line);
            }
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source, error);
        List<Token> tokens = scanner.scanTokens();

        Expr ast = new Parser(tokens, error).parse();

        error.report();

        if (ast != null)
            System.out.println(new AstPrinter().print(ast));
        else
            System.out.println();

    }
}

