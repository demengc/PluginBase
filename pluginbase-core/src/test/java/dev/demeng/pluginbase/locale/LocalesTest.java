package dev.demeng.pluginbase.locale;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class LocalesTest {

  @Test
  void exactLanguageMatch() {
    assertThat(Locales.get("en")).isEqualTo(Locales.ENGLISH);
    assertThat(Locales.get("fr")).isEqualTo(Locales.FRENCH);
    assertThat(Locales.get("de")).isEqualTo(Locales.GERMAN);
  }

  @Test
  void exactLanguageCountryMatch() {
    assertThat(Locales.get("zh_CN")).isEqualTo(Locales.SIMPLIFIED_CHINESE);
    assertThat(Locales.get("zh_TW")).isEqualTo(Locales.TRADITIONAL_CHINESE);
  }

  @Test
  void minecraftLowercaseFormat() {
    assertThat(Locales.get("zh_cn")).isEqualTo(Locales.SIMPLIFIED_CHINESE);
    assertThat(Locales.get("zh_tw")).isEqualTo(Locales.TRADITIONAL_CHINESE);
  }

  @Test
  void hierarchicalFallbackToBaseLanguage() {
    assertThat(Locales.get("en_gb")).isEqualTo(Locales.ENGLISH);
    assertThat(Locales.get("en_us")).isEqualTo(Locales.ENGLISH);
    assertThat(Locales.get("pt_br")).isEqualTo(Locales.PORTUGUESE);
    assertThat(Locales.get("es_mx")).isEqualTo(Locales.SPANISH);
  }

  @Test
  void caseInsensitiveLanguageOnly() {
    assertThat(Locales.get("FR")).isEqualTo(Locales.FRENCH);
    assertThat(Locales.get("De")).isEqualTo(Locales.GERMAN);
  }

  @Test
  void unknownLocaleReturnsNull() {
    assertThat(Locales.get("xx")).isNull();
    assertThat(Locales.get("xx_YY")).isNull();
  }

  @Test
  void getLocalesIsNotEmpty() {
    int count = 0;
    for (final Locale ignored : Locales.getLocales()) {
      count++;
    }
    assertThat(count).isGreaterThan(20);
  }
}
