package jlox;

import java.util.List;

public class Analyzer {

    public static void analyse(List<Stmt> ast, Stdio stdio, Environment globalSymbols) {
        var walker = new Walker(
                List.of(
                    new WalkReturn(),
                    new WalkSymbol(globalSymbols)
                ),
                stdio);
        int depthBefore = globalSymbols.depth();
        walker.walk(ast);
        assert depthBefore == globalSymbols.depth();
    }
}
