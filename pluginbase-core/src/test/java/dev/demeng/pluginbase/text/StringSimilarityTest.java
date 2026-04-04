package dev.demeng.pluginbase.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.Test;

class StringSimilarityTest {

  @Test
  void levenshteinDistance_identicalStrings_returnsZero() {
    assertThat(StringSimilarity.levenshteinDistance("hello", "hello")).isZero();
  }

  @Test
  void levenshteinDistance_emptyAndNonEmpty_returnsLength() {
    assertThat(StringSimilarity.levenshteinDistance("", "abc")).isEqualTo(3);
    assertThat(StringSimilarity.levenshteinDistance("abc", "")).isEqualTo(3);
  }

  @Test
  void levenshteinDistance_bothEmpty_returnsZero() {
    assertThat(StringSimilarity.levenshteinDistance("", "")).isZero();
  }

  @Test
  void levenshteinDistance_singleSubstitution() {
    assertThat(StringSimilarity.levenshteinDistance("cat", "car")).isEqualTo(1);
  }

  @Test
  void levenshteinDistance_insertion() {
    assertThat(StringSimilarity.levenshteinDistance("cat", "cats")).isEqualTo(1);
  }

  @Test
  void levenshteinDistance_deletion() {
    assertThat(StringSimilarity.levenshteinDistance("cats", "cat")).isEqualTo(1);
  }

  @Test
  void levenshteinDistance_completelyDifferent() {
    assertThat(StringSimilarity.levenshteinDistance("abc", "xyz")).isEqualTo(3);
  }

  @Test
  void levenshteinDistance_caseInsensitive() {
    assertThat(StringSimilarity.levenshteinDistance("Hello", "hello")).isZero();
  }

  @Test
  void similarity_identicalStrings_returnsOne() {
    assertThat(StringSimilarity.similarity("test", "test")).isEqualTo(1.0);
  }

  @Test
  void similarity_completelyDifferent_returnsZero() {
    assertThat(StringSimilarity.similarity("abc", "xyz")).isCloseTo(0.0, within(0.01));
  }

  @Test
  void similarity_bothEmpty_returnsOne() {
    assertThat(StringSimilarity.similarity("", "")).isEqualTo(1.0);
  }

  @Test
  void similarity_partialMatch() {
    double sim = StringSimilarity.similarity("kitten", "sitting");
    assertThat(sim).isBetween(0.0, 1.0);
  }

  @Test
  void isSimilar_aboveThreshold_returnsTrue() {
    assertThat(StringSimilarity.isSimilar("hello", "hallo", 0.5)).isTrue();
  }

  @Test
  void isSimilar_belowThreshold_returnsFalse() {
    assertThat(StringSimilarity.isSimilar("abc", "xyz", 0.9)).isFalse();
  }

  @Test
  void isSimilar_exactThreshold_returnsTrue() {
    double sim = StringSimilarity.similarity("cat", "car");
    assertThat(StringSimilarity.isSimilar("cat", "car", sim)).isTrue();
  }
}
