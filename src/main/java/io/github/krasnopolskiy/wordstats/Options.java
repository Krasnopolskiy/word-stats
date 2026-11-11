package io.github.krasnopolskiy.wordstats;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public record Options(Path file, int top) {
    public static final int DEFAULT_TOP = 10;
    public static final int MAX_TOP = 1000;
    private static final String TOP_FLAG = "--top";

    public static Options parse(String[] args) {
        Path file = null;
        int top = DEFAULT_TOP;
        int index = 0;
        while (index < args.length) {
            String arg = args[index];
            index++;
            if (arg.equals(TOP_FLAG)) {
                if (index == args.length) {
                    throw new UsageException(TOP_FLAG + " needs a number");
                }
                top = parseTop(args[index]);
                index++;
                continue;
            }
            if (arg.startsWith("-")) {
                throw new UsageException("Unknown option: " + arg);
            }
            if (file != null) {
                throw new UsageException("Give one file, got a second one: " + arg);
            }
            file = parsePath(arg);
        }
        if (file == null) {
            throw new UsageException("No file given");
        }
        return new Options(file, top);
    }

    private static int parseTop(String value) {
        int top;
        try {
            top = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new UsageException(TOP_FLAG + " needs a whole number, got: " + value);
        }
        if (top < 1 || top > MAX_TOP) {
            throw new UsageException(TOP_FLAG + " must be from 1 to " + MAX_TOP + ", got: " + top);
        }
        return top;
    }

    private static Path parsePath(String value) {
        try {
            return Path.of(value);
        } catch (InvalidPathException e) {
            throw new UsageException("Not a valid path: " + value);
        }
    }
}
