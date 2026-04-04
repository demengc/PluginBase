package dev.demeng.pluginbase.random;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RandomStringTest {

  @Test
  void generate_returnsCorrectLength() {
    String result = RandomString.generate(10, RandomString.ALPHANUMERIC_MIXED);
    assertThat(result).hasSize(10);
  }

  @Test
  void generate_zeroLength_returnsEmpty() {
    String result = RandomString.generate(0, RandomString.ALPHANUMERIC_MIXED);
    assertThat(result).isEmpty();
  }

  @Test
  void generate_onlyUpperAlpha_containsOnlyUppercase() {
    String result = RandomString.generate(100, RandomString.ALPHABET_UPPER);
    assertThat(result).matches("[A-Z]+");
  }

  @Test
  void generate_onlyLowerAlpha_containsOnlyLowercase() {
    String result = RandomString.generate(100, RandomString.ALPHABET_LOWER);
    assertThat(result).matches("[a-z]+");
  }

  @Test
  void generate_onlyNumbers_containsOnlyDigits() {
    String result = RandomString.generate(100, RandomString.NUMBERS);
    assertThat(result).matches("[0-9]+");
  }

  @Test
  void generate_alphanumericUpper_containsOnlyExpectedChars() {
    String result = RandomString.generate(100, RandomString.ALPHANUMERIC_UPPER);
    assertThat(result).matches("[A-Z0-9]+");
  }

  @Test
  void generate_alphanumericLower_containsOnlyExpectedChars() {
    String result = RandomString.generate(100, RandomString.ALPHANUMERIC_LOWER);
    assertThat(result).matches("[a-z0-9]+");
  }

  @Test
  void generate_producesVaryingOutput() {
    String a = RandomString.generate(20, RandomString.ALPHANUMERIC_MIXED);
    String b = RandomString.generate(20, RandomString.ALPHANUMERIC_MIXED);
    assertThat(a).isNotEqualTo(b);
  }

  @Test
  void charArrays_haveExpectedLengths() {
    assertThat(RandomString.ALPHABET_UPPER).hasSize(26);
    assertThat(RandomString.ALPHABET_LOWER).hasSize(26);
    assertThat(RandomString.NUMBERS).hasSize(10);
    assertThat(RandomString.ALPHANUMERIC_UPPER).hasSize(36);
    assertThat(RandomString.ALPHANUMERIC_LOWER).hasSize(36);
    assertThat(RandomString.ALPHANUMERIC_MIXED).hasSize(62);
  }
}
