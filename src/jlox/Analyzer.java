package jlox;

import java.util.List;

public class Analyzer {

    public static void analyse(List<Stmt> ast, Stdio stdio) {
        var walker = new Walker(List.of(
                new WalkReturn()),
                stdio);
        walker.walk(ast);
    }
}
