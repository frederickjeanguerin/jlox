package jlox;

interface WalkerData {
    Stdio stdio();
    int scopeDepth();
    Stmt parentStmt(int depth);
}
