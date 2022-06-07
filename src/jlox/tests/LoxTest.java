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
    void testSyntaxError(String description, String input, String expectedErrors) {
        input = transform(input);
        var result = Lox.testParser(input);
        for (String expectedError : expectedErrors.split(", ")) {
            assertLinesMatch(List.of("(?is).*" + expectedError + ".*"), List.of(result.errors()), description);
        }
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "LoxTest_Interpret.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testInterpreter(String description, String input, String expectedResult) {
        input = transform(input);
        expectedResult = transform(expectedResult);
        var result = Lox.testInterpreter(input);
        assertEquals(expectedResult, result.prints().stripTrailing(), description);
        assertEquals("", result.errors());
    }

    private static String transform(String input) {
        return input
                // string needs to be doubly quoted, because simply quoted have special treatment in csv file
                .replace("\"\"", "\"")
                // convert \n to new line
                .replace("\\n", "\n");
    }
}