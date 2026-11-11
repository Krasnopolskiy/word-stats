package io.github.krasnopolskiy.wordstats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MainTest {

    private static final byte[] LATIN1_BYTES = { (byte) 0xE9, (byte) 0xE8 };

    @TempDir
    private Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String... args) {
        return Main.run(
            args,
            new PrintStream(out, true, StandardCharsets.UTF_8),
            new PrintStream(err, true, StandardCharsets.UTF_8)
        );
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private String errors() {
        return err.toString(StandardCharsets.UTF_8);
    }

    @Test
    void printsStatsOfAFile() throws IOException {
        Path file = Files.writeString(dir.resolve("text.txt"), "the cat and the hat\nthe end\n");

        int code = run(file.toString(), "--top", "2");

        assertEquals(Main.EXIT_OK, code);
        String expected = String.join(
            "\n",
            "File: " + file,
            "Lines: 2",
            "Words: 7",
            "Characters: 28",
            "Top 2 words:",
            "  1. the  3",
            "  2. and  1",
            ""
        );
        assertEquals(expected, output());
    }

    @Test
    void reportsAFileWithoutWords() throws IOException {
        Path file = Files.writeString(dir.resolve("blank.txt"), "\n...\n");

        assertEquals(Main.EXIT_OK, run(file.toString()));
        assertTrue(output().endsWith("No words found.\n"));
    }

    @Test
    void missingFileExitsWithReadError() {
        Path file = dir.resolve("missing.txt");

        assertEquals(Main.EXIT_READ_ERROR, run(file.toString()));
        assertEquals("File not found: " + file + System.lineSeparator(), errors());
    }

    @Test
    void nonUtf8FileExitsWithReadError() throws IOException {
        Path file = Files.write(dir.resolve("latin1.txt"), LATIN1_BYTES);

        assertEquals(Main.EXIT_READ_ERROR, run(file.toString()));
        assertTrue(errors().startsWith("Not a UTF-8 text file"));
    }

    @Test
    void directoryExitsWithReadError() {
        assertEquals(Main.EXIT_READ_ERROR, run(dir.toString()));
        assertTrue(errors().startsWith("Cannot read"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "0", "1001", "ten", "-5" })
    void badTopExitsWithUsage(String top) {
        assertEquals(Main.EXIT_USAGE, run("file.txt", "--top", top));
        assertTrue(errors().contains(Main.USAGE));
    }

    @Test
    void topWithoutValueExitsWithUsage() {
        assertEquals(Main.EXIT_USAGE, run("file.txt", "--top"));
        assertTrue(errors().startsWith("--top needs a number"));
    }

    @Test
    void noArgumentsExitWithUsage() {
        assertEquals(Main.EXIT_USAGE, run());
        assertTrue(errors().startsWith("No file given"));
    }

    @Test
    void unknownOptionExitsWithUsage() {
        assertEquals(Main.EXIT_USAGE, run("--verbose", "file.txt"));
        assertTrue(errors().startsWith("Unknown option: --verbose"));
    }

    @Test
    void secondFileExitsWithUsage() {
        assertEquals(Main.EXIT_USAGE, run("a.txt", "b.txt"));
        assertTrue(errors().startsWith("Give one file"));
    }

    @Test
    void helpPrintsUsage() {
        assertEquals(Main.EXIT_OK, run("--help"));
        assertTrue(output().startsWith(Main.USAGE));
    }
}
