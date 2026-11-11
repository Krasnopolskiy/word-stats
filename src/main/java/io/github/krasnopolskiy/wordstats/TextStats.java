package io.github.krasnopolskiy.wordstats;

import java.util.List;

public record TextStats(long lines, long words, long characters, List<WordCount> topWords) {
    public TextStats {
        topWords = List.copyOf(topWords);
    }
}
