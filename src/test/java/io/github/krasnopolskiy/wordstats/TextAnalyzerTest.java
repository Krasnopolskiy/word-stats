package io.github.krasnopolskiy.wordstats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextAnalyzerTest {

    private final TextAnalyzer analyzer = new TextAnalyzer();

    @Test
    void countsLinesWordsAndCharacters() {
        TextStats stats = analyzer.analyze("one two\nthree\n", 10);

        assertEquals(2, stats.lines());
        assertEquals(3, stats.words());
        assertEquals(14, stats.characters());
    }

    @Test
    void ignoresCaseAndPunctuation() {
        TextStats stats = analyzer.analyze("Cat, cat! CAT? dog.", 10);

        assertEquals(List.of(new WordCount("cat", 3), new WordCount("dog", 1)), stats.topWords());
    }

    @Test
    void countsCyrillicWordsAndCharacters() {
        TextStats stats = analyzer.analyze("Мир, мир и май", 10);

        assertEquals(4, stats.words());
        assertEquals(14, stats.characters());
        assertEquals(new WordCount("мир", 2), stats.topWords().getFirst());
    }

    @Test
    void keepsApostrophesAndHyphensInsideWords() {
        TextStats stats = analyzer.analyze("don't stop - well-known 'quote'", 10);

        assertEquals(
            List.of("don't", "quote", "stop", "well-known"),
            stats.topWords().stream().map(WordCount::word).toList()
        );
    }

    @Test
    void ordersTopWordsByCountThenAlphabetically() {
        TextStats stats = analyzer.analyze("b a c b a b", 10);

        assertEquals(List.of(new WordCount("b", 3), new WordCount("a", 2), new WordCount("c", 1)), stats.topWords());
    }

    @Test
    void limitsTopWords() {
        TextStats stats = analyzer.analyze("a b c d", 2);

        assertEquals(4, stats.words());
        assertEquals(2, stats.topWords().size());
    }

    @Test
    void emptyTextGivesZeros() {
        TextStats stats = analyzer.analyze("", 10);

        assertEquals(new TextStats(0, 0, 0, List.of()), stats);
    }

    @Test
    void rejectsNegativeTop() {
        assertThrows(IllegalArgumentException.class, () -> analyzer.analyze("a", -1));
    }
}
