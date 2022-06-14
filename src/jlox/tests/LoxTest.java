package jlox.tests;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        var stdio = Lox.run(input, Lox.RunPhase.AST);
        assertEquals(expectedAst, stdio.stdout().replace('\n', ' ').stripTrailing(), description);
        assertEquals("", stdio.stderr());
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "LoxTest_SyntaxError.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testSyntaxError(String description, String input, String expectedErrors) {
        input = transform(input);
        var result = Lox.run(input, Lox.RunPhase.AST);
        for (String expectedError : expectedErrors.split(", ")) {
            assertLinesMatch(List.of("(?is).*" + expectedError + ".*"), List.of(result.stderr()), description);
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
        var result = Lox.run(input);
        assertEquals(expectedResult, result.stdout().replace('\n', ' ').stripTrailing(), description);
        assertEquals("", result.stderr());
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "LoxTest_RuntimeError.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testRuntimeError(String description, String input, String expectedErrors) {
        input = transform(input);
        var result = Lox.run(input);
        for (String expectedError : expectedErrors.split(", ")) {
            if (expectedError.startsWith("*")) {
                assertLinesMatch(
                        List.of("(?is).*" + expectedError.substring(1) + ".*"),
                        List.of(result.stdout()), description);
            } else {
                assertLinesMatch(List.of("(?is).*" + expectedError + ".*"), List.of(result.stderr()), description);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "chap8-block-and-scope",
            "chap9-fibonacci",
            "chap10-fibonacci-recursive"
    })
    void testSnapShot(String fileName) throws IOException {
        Path sourcePath = Path.of("src/jlox/tests/programs/" + fileName + ".lox");
        Path targetPath = Path.of("src/jlox/tests/snapshots/" + fileName + ".txt");
        var source = Files.readString(sourcePath);
        var result = Lox.run(source);
        var given = result.stdout() + "\n\n" + result.stderr();
        if (Files.exists(targetPath)){
            var expected = Files.readString(targetPath);
            assertLinesMatch(List.of(expected.split("\n")), List.of(given.split("\n")));
        } else {
            Files.writeString(targetPath, given);
            //noinspection ConstantConditions
            Assumptions.assumeTrue(false, "new snapshot created");
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