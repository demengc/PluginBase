package dev.demeng.pluginbase.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PairTest {

  @Test
  void of_storesValues() {
    Pair<String, Integer> pair = Pair.of("hello", 42);
    assertThat(pair.getA()).isEqualTo("hello");
    assertThat(pair.getB()).isEqualTo(42);
  }

  @Test
  void of_copyConstructor_createsEqualPair() {
    Pair<String, Integer> original = Pair.of("hello", 42);
    Pair<String, Integer> copy = Pair.of(original);
    assertThat(copy).isEqualTo(original);
    assertThat(copy.getA()).isEqualTo("hello");
    assertThat(copy.getB()).isEqualTo(42);
  }

  @Test
  void equals_samePairs_returnsTrue() {
    Pair<String, Integer> a = Pair.of("x", 1);
    Pair<String, Integer> b = Pair.of("x", 1);
    assertThat(a).isEqualTo(b);
  }

  @Test
  void equals_differentPairs_returnsFalse() {
    Pair<String, Integer> a = Pair.of("x", 1);
    Pair<String, Integer> b = Pair.of("y", 2);
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void hashCode_samePairs_sameHash() {
    Pair<String, Integer> a = Pair.of("x", 1);
    Pair<String, Integer> b = Pair.of("x", 1);
    assertThat(a.hashCode()).isEqualTo(b.hashCode());
  }

  @Test
  void of_allowsNullValues() {
    Pair<String, String> pair = Pair.of(null, null);
    assertThat(pair.getA()).isNull();
    assertThat(pair.getB()).isNull();
  }

  @Test
  void of_mixedTypes() {
    Pair<Integer, Boolean> pair = Pair.of(1, true);
    assertThat(pair.getA()).isEqualTo(1);
    assertThat(pair.getB()).isTrue();
  }
}
