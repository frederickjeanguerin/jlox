package jlox;

import java.util.*;
import static jlox.LoxNative.*;

public class LoxString extends LoxClass {

    private static final List<LoxNative> functions = List.of(
            F2("charAt", str -> index -> "" + stringArg(str, 1).charAt(intArg(index, 2))),
            F2("compareTo", str1 -> str2 -> (double)stringArg(str1, 1).compareTo(stringArg(str2, 2))),
            F2("compareToIgnoreCase", str1 -> str2 -> (double)stringArg(str1, 1).compareToIgnoreCase(stringArg(str2, 2))),
            F2("concat", str1 -> str2 -> stringArg(str1, 1).concat(stringArg(str2, 2))),
            F2("contains", str1 -> str2 -> stringArg(str1, 1).contains(stringArg(str2, 2))),
            F2("endsWith", str1 -> str2 -> stringArg(str1, 1).endsWith(stringArg(str2, 2))),
            F2("equalsIgnoreCase", str1 -> str2 -> stringArg(str1, 1).equalsIgnoreCase(stringArg(str2, 2))),
            F2("format1", format -> obj -> stringArg(format, 1).formatted(obj)),
            F3("format2", format -> obj1 -> obj2 -> stringArg(format, 1).formatted(obj1, obj2)),
            F4("format3", format -> obj1 -> obj2 -> obj3 -> stringArg(format, 1).formatted(obj1, obj2, obj3)),
            P1("hashCode", str -> (double)stringArg(str, 1).hashCode()),
            F2("indent", str -> n -> stringArg(str, 1).indent(intArg(n, 2))),
            F2("indexOf", str -> substring -> (double)stringArg(str, 1).indexOf(stringArg(substring, 2))),
            F3("indexFrom", str -> substring -> fromIndex -> (double)stringArg(str, 1).indexOf(stringArg(substring, 2), intArg(fromIndex, 3))),
            F1("intern", str -> stringArg(str, 1).intern()),
            P1("isBlank", str -> stringArg(str, 1).isBlank()),
            P1("isEmpty", str -> stringArg(str, 1).isEmpty()),
            F2("lastIndexOf", str -> substring -> (double)stringArg(str, 1).lastIndexOf(stringArg(substring, 2))),
            F3("lastIndexFrom", str -> substring -> fromIndex -> (double)stringArg(str, 1).lastIndexOf(stringArg(substring, 2), intArg(fromIndex, 3))),
            P1("length", str -> stringArg(str, 1).length()),
            F2("matches", str -> regex -> stringArg(str, 1).matches(stringArg(regex, 2))),
            F2("repeat", str -> n -> stringArg(str, 1).repeat(intArg(n, 2))),
            F3("replace", str -> target -> replacement -> stringArg(str, 1).replace(stringArg(target, 2), stringArg(replacement, 3))),
            F3("replaceAll", str -> target -> replacement -> stringArg(str, 1).replaceAll(stringArg(target, 2), stringArg(replacement, 3))),
            F3("replaceFirst", str -> target -> replacement -> stringArg(str, 1).replaceFirst(stringArg(target, 2), stringArg(replacement, 3))),
            F2("startsWith", str1 -> str2 -> stringArg(str1, 1).startsWith(stringArg(str2, 2))),
            F3("startsFrom", str -> substring -> fromIndex -> stringArg(str, 1).startsWith(stringArg(substring, 2), intArg(fromIndex, 3))),
            F1("strip", str -> stringArg(str, 1).strip()),
            F1("stripIndent", str -> stringArg(str, 1).stripIndent()),
            F1("stripLeading", str -> stringArg(str, 1).stripLeading()),
            F1("stripTrailing", str -> stringArg(str, 1).stripTrailing()),
            F2("right", str -> start -> stringArg(str, 1).substring(intArg(start, 2))),
            F2("left", str -> stop -> stringArg(str, 1).substring(0, intArg(stop, 2))),
            F3("substring", str -> start -> stop -> stringArg(str, 1).substring(intArg(start, 2), intArg(stop, 3))),
            F1("toLower", str -> stringArg(str, 1).toLowerCase()),
            F1("toUpper", str -> stringArg(str, 1).toUpperCase()),
            F1("trim", str -> stringArg(str, 1).trim())
            );

    public static final LoxString instance;  // Singleton

    static  {
        String name = "String";
        List<LoxClass> superclasses = Collections.emptyList();
        var stmt = (Stmt.Class) Parser.Parse("class String {}", -1).get(0);
        Map<String, LoxCallable> classMethods = new HashMap<>();
        for (var function : functions) {
            classMethods.put(function.name, function);
        }
        instance = new LoxString(name, superclasses, classMethods, stmt);
    }

    private LoxString(String name, List<LoxClass> superclasses, Map<String, LoxCallable> classMethods, Stmt.Class stmt) {
        super(name, superclasses, classMethods, stmt, null);
    }
}
