package jlox.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.err.println("Usage: generate_ast [output directory]");
            System.exit(64);
        }
        String outputDir = args.length == 1 ? args[0] : "src/jlox";
        // printPWD();
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary     : Expr left, Token operator, Expr right",
                "Grouping   : Expr expression",
                "Literal    : Object value",
                "Ternary    : Expr left, Token leftOp, Expr middle, Token rightOp, Expr right",
                "Unary      : Token operator, Expr right",
                "Variable   : Token name",
                null
                ));
        defineAst(outputDir, "Stmt", Arrays.asList(
                "Expression : Expr expression",
                "Last       : Expr expression",
                "Print      : Expr expression",
                "Var        : Token name, Expr initializer",
                null
        ));
    }

    @SuppressWarnings("unused")
    private static void printPWD() {
        var currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();
        System.out.println("Current absolute path is: " + s);
    }

    @SuppressWarnings("SameParameterValue") // baseName
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + '/' + baseName + ".java";
        try (var writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writer.print(
                """
                // Automatically generated by jlox.tool.GenerateAst
                
                package jlox;
                
                abstract class %s {
                """.formatted(baseName)
            );

            defineVisitor(writer, baseName, types);

            for (String type: types) {
                if (type == null) continue;
                String className = type.split(":")[0].trim();
                String fields = type.split(":")[1].trim();
                defineType(writer, baseName, className, fields);
            }

            // Class end
            writer.print(
                """
                  
                  abstract <R> R visit(Visitor<R> visitor);
                }
                """
            );
        }


    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        // Class start
        writer.println(
            "\n  static class %s extends %s {".formatted(className, baseName));

        // Fields
        for (String field: fields.split(", ")){
            writer.println("    final %s;".formatted(field));
        }

        // Constructor start
        writer.println(
            "\n    %s ( %s ) {".formatted(className, fields));
        // > Initialise field
        for ( String field: fields.split(", ")) {
            String name = field.split(" ")[1];
            writer.println(
                "      this.%s = %s;".formatted(name, name));
        }
        // > Constructor end
        writer.println("    }");

        // Visit override method
        writer.print(
            """
                
                @Override
                <R> R visit(Visitor<R> visitor) {
                  return visitor.visit%s%s(this);
                }
            """.formatted(className, baseName)
        );

        // Class end
        writer.println("  }");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        // Interface start
        writer.println("  interface Visitor<R> {");

        // Methods
        for (String type: types) {
            if (type == null) continue;
            String className = type.split(":")[0].trim();
            writer.println("    R visit%s%s(%s %s);"
                .formatted(className, baseName, className, baseName.toLowerCase()));
        }

        // Interface end
        writer.println("  }");
    }

}
