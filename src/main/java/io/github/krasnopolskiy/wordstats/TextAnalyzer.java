package io.github.krasnopolskiy.wordstats;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextAnalyzer {

    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+(?:['’-][\\p{L}\\p{N}]+)*");
    private static final Comparator<WordCount> BY_FREQUENCY = Comparator.comparingLong(WordCount::count)
        .reversed()
        .thenComparing(WordCount::word);

    public TextStats analyze(String text, int top) {
        if (top < 0) {
            throw new IllegalArgumentException("top must not be negative: " + top);
        }
        Map<String, Long> counts = WORD.matcher(text)
            .results()
            .map(match -> match.group().toLowerCase(Locale.ROOT))
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        long words = counts.values().stream().mapToLong(Long::longValue).sum();
        List<WordCount> topWords = counts
            .entrySet()
            .stream()
            .map(entry -> new WordCount(entry.getKey(), entry.getValue()))
            .sorted(BY_FREQUENCY)
            .limit(top)
            .toList();
        return new TextStats(text.lines().count(), words, text.codePointCount(0, text.length()), topWords);
    }
}
