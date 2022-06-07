package jlox.tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import jlox.Lox;

class LoxTest {

    @ParameterizedTest
    @CsvFileSource(
            resources = "LoxTest_Ast.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testAst(String description, String input, String expectedAst) {
        input = transform(input);
        expectedAst = transform(expectedAst);
        var result = Lox.testParser(input);
        assertEquals(expectedAst, result.ast(), description);
        assertEquals("", result.errors());
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "LoxTest_SyntaxError.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testError(String description, String input, String expectedErrors) {
        input = transform(input);
        var result = Lox.testParser(input);
        for (String expectedError : expectedErrors.split(", ")) {
            assertLinesMatch(List.of("(?is).*" + expectedError + ".*"), List.of(result.errors()), description);
        }
    }

    private static String transform(String input) {
        return input
                // string needs to be doubly quoted, because simply quoted have special treatment in csv file
                .replace("\"\"", "\"")
                // convert \n to new line
                .replace("\\n", "\n");
    }
}