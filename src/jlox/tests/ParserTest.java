package jlox.tests;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {

    @ParameterizedTest
    @CsvFileSource(
            resources = "ParserTest_Ast.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testAst(String description, String input, String expectedAst) {
        input = treatInput(input);
        var result = jlox.Parser.test(input);
        assertEquals(expectedAst, result.ast(), description);
        assertEquals("", result.errors());
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "ParserTest_Error.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testError(String description, String input, String expectedErrors) {
        input = treatInput(input);
        var result = jlox.Parser.test(input);
        for (String expectedError : expectedErrors.split(", ")) {
            assertLinesMatch(List.of("(?is).*" + expectedError + ".*"), List.of(result.errors()), description);
        }
    }

    private static String treatInput(String input) {
        return input
                // string needs to be doubly quoted, because simply quoted have special treatment in csv file
                .replace("\"\"", "\"")
                // convert \n to new line
                .replace("\\n", "\n");
    }
}