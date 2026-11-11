package io.github.krasnopolskiy.wordstats;

import java.util.List;

public class StatsFormatter {

    private static final int MIN_WORD_WIDTH = 4;

    public String format(String source, TextStats stats) {
        StringBuilder out = new StringBuilder();
        out.append("File: ").append(source).append('\n');
        out.append("Lines: ").append(stats.lines()).append('\n');
        out.append("Words: ").append(stats.words()).append('\n');
        out.append("Characters: ").append(stats.characters()).append('\n');
        List<WordCount> topWords = stats.topWords();
        if (topWords.isEmpty()) {
            out.append("No words found.\n");
            return out.toString();
        }
        int width = topWords
            .stream()
            .mapToInt(word -> word.word().length())
            .max()
            .orElse(0);
        String row = "%3d. %-" + Math.max(width, MIN_WORD_WIDTH) + "s %d\n";
        out.append("Top ").append(topWords.size()).append(" words:\n");
        for (int i = 0; i < topWords.size(); i++) {
            WordCount word = topWords.get(i);
            out.append(String.format(row, i + 1, word.word(), word.count()));
        }
        return out.toString();
    }
}
