package jlox.tests;

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

    private static String transform(String input) {
        return input
                // string needs to be doubly quoted, because simply quoted have special treatment in csv file
                .replace("\"\"", "\"")
                // convert \n to new line
                .replace("\\n", "\n");
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "LoxTest_Ast.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testAst(String description, String input, String expectedAst) {
        input = transform(input);
        var expected
                = expectedAst.equals("=") ? input
                : expectedAst.equals("=r") ? input.replace(" ", "")
                : transform(expectedAst);
        var stdio = Lox.parse(input);
        var given = stdio.stdout().replace('\n', ' ').stripTrailing();
        if (expectedAst.equals("=r")) {
            given = given.replace(" ", "");
        }
        assertEquals(expected, given, description);
        assertEquals("", stdio.stderr());
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "LoxTest_SyntaxError.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testSyntaxError(String description, String input, String expectedErrors) {
        input = transform(input);
        var result = Lox.parse(input);
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
        assertEquals(
                expectedResult,
                result.stdout().replace('\n', ' ').stripTrailing(),
                description + "\n\n >> stderr: %s\n".formatted(result.stderr()));
        assertEquals("", result.stderr());
    }

    @ParameterizedTest
    @CsvFileSource(
            resources = "LoxTest_Advanced.csv",
            numLinesToSkip = 1,
            delimiter = '¤')
    void testRuntimeError(String description, String input, String expectedErrors) {
        input = transform(input);
        var result = Lox.run(input);
        var stdout = result.stdout().trim();
        var stderr = result.stderr().trim();
        description += "\n >> stdout: %s'%s'\n".formatted(
                stdout.contains("\n") ? "\n" : "",
                stdout);
        description += " >> stderr: %s%s\n".formatted(
                stderr.contains("\n") ? "\n" : "",
                stderr);
        for (String expectedError : expectedErrors.split(", ")) {
            expectedError = transform(expectedError);
            if (expectedError.startsWith("=")) {
                assertLinesMatch(
                        List.of(expectedError.substring(1)),
                        List.of(result.stdout().stripTrailing()),
                        description);
            } else if (expectedError.startsWith("*")) {
                assertLinesMatch(
                        List.of("(?is).*" + expectedError.substring(1) + ".*"),
                        List.of(result.stdout()),
                        description);
            } else if (expectedError.startsWith("/E")) {
                assertEquals(
                        expectedError,
                        "/E" + result.getErrorCount(),
                        description);
            } else if (expectedError.startsWith("/P")) {
                assertEquals(
                        expectedError,
                        "/P" + result.getPrintCount(),
                        description);
            } else if (expectedError.startsWith("/W")) {
                assertEquals(
                        expectedError,
                        "/W" + result.getWarningCount(),
                        description);
            } else if (expectedError.startsWith("/")) {
                assertEquals(
                        expectedError,
                        "/" + result.getPrintCount() + result.getWarningCount() + result.getErrorCount(),
                        description);
            } else {
                assertLinesMatch(
                        List.of("(?is).*" + expectedError + ".*"),
                        List.of(result.stderr()),
                        description);
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "chap8-block-and-scope",
            "chap9-fibonacci",
            "chap10-fibonacci-recursive",
            "chap10-lambda-closure",
            "chap12-class-self",
            "chap13-superclass-methods",
            "chap13-super-semantics",
            "chap13-super-constructors",
            "chap13-multiple-inheritance",
            "chap13-diamond-of-death-ok",
            "chap13-diamond-of-death-bad",
            "chap12-properties",
            "chap12-class-methods",
            "chap4-string-with-escapes",
            "chap9-continue-for"
    })
    void testSnapShot(String fileName) throws IOException {
        Path sourcePath = Path.of("src/jlox/tests/programs/" + fileName + ".lox");
        Path targetPath = Path.of("src/jlox/tests/snapshots/" + fileName + ".txt");
        var source = Files.readString(sourcePath);
        var result = Lox.run(source);
        var given = result.stdout() + "\n\n" + result.stderr();
        var sourceIsDraft = source.matches("(?is)^.*//.*draft.*");
        if (Files.exists(targetPath) && !sourceIsDraft) {
            var expected = Files.readString(targetPath);
            if (!expected.matches("(?is)^//.*draft.*")) {
                assertLinesMatch(List.of(expected.split("\n")), List.of(given.split("\n")));
                return;
            }
        }
        if (sourceIsDraft) {
            given = "// draft mode \n" + given;
        }
        Files.writeString(targetPath, given);
        //noinspection ConstantConditions
        Assumptions.assumeTrue(false, "new snapshot created");
    }
}
