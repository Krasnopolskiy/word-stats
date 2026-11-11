package io.github.krasnopolskiy.wordstats;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Set;

public class Main {

    static final int EXIT_OK = 0;
    static final int EXIT_READ_ERROR = 1;
    static final int EXIT_USAGE = 2;
    static final String USAGE = "Usage: java -jar word-stats.jar <file> [--top N]";
    private static final Set<String> HELP_FLAGS = Set.of("-h", "--help");

    private Main() {}

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length == 1 && HELP_FLAGS.contains(args[0])) {
            out.println(USAGE);
            out.println("Prints line, word and character counts of a UTF-8 text file and its most frequent words.");
            out.println(
                "--top N    how many words to show, 1 to " + Options.MAX_TOP + ", default " + Options.DEFAULT_TOP
            );
            return EXIT_OK;
        }
        Options options;
        try {
            options = Options.parse(args);
        } catch (UsageException e) {
            err.println(e.getMessage());
            err.println(USAGE);
            return EXIT_USAGE;
        }
        String text;
        try {
            text = Files.readString(options.file());
        } catch (NoSuchFileException e) {
            err.println("File not found: " + options.file());
            return EXIT_READ_ERROR;
        } catch (MalformedInputException e) {
            err.println("Not a UTF-8 text file: " + options.file());
            return EXIT_READ_ERROR;
        } catch (IOException e) {
            err.println("Cannot read " + options.file() + ": " + e.getMessage());
            return EXIT_READ_ERROR;
        }
        TextStats stats = new TextAnalyzer().analyze(text, options.top());
        out.print(new StatsFormatter().format(options.file().toString(), stats));
        return EXIT_OK;
    }
}
