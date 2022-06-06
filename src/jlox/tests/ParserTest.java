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
        // string needs to be doubly quoted, because simply quoted have special treatment in csv file
        input = input.replace("\"\"", "\"");
        var result = jlox.Parser.test(input);
        assertEquals(expectedAst, result.ast(), description + " (" + input + ")");
        assertEquals("", result.errors());
    }

//    @ParameterizedTest
//    @CsvFileSource(
//            resources = "ParserTest_Ast.csv",
//            numLinesToSkip = 1,
//            delimiter = '¤')
//    void testError(String description, String input, String expectedAst, String expectedError) {
//        input = input.replace("\"\"", "\"");
//        expectedError = expectedError == null ? "" : expectedError;
//        var result = jlox.Parser.test(input);
//        assertEquals(expectedAst, result.ast(), description + " (" + input + ")");
//        assertLinesMatch(List.of(expectedError), List.of(result.errors()), description);
//    }

}