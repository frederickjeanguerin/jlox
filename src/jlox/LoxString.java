package jlox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoxString extends LoxClass {

    private static LoxString _instance = null;

    public LoxString(String name, List<LoxClass> superclasses, Map<String, LoxCallable> classMethods, Stmt.Class stmt) {
        super(name, superclasses, classMethods, stmt, null);
    }

    public static LoxString instance() {
        if (_instance == null) {
            String name = "String";
            List<LoxClass> superclasses = new ArrayList<>();
            Map<String, LoxCallable> classMethods = new HashMap<>();

            var stmt = (Stmt.Class) Parser.Parse("class String {}", -1).get(0);
            _instance = new LoxString(name, superclasses, classMethods, stmt);
        }
        return _instance;
    }

}
