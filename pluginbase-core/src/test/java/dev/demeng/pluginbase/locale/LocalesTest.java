package dev.demeng.pluginbase.locale;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class LocalesTest {

  @Test
  void get_englishByCode_returnsEnglishLocale() {
    assertThat(Locales.get("en")).isEqualTo(Locale.ENGLISH);
  }

  @Test
  void get_frenchByCode_returnsFrenchLocale() {
    assertThat(Locales.get("fr")).isEqualTo(Locale.FRENCH);
  }

  @Test
  void get_unknownCode_returnsNull() {
    assertThat(Locales.get("nonexistent")).isNull();
  }

  @Test
  void getLocales_containsAllDeclaredConstants() throws Exception {
    List<Locale> declaredLocales = new ArrayList<>();
    for (Field field : Locales.class.getDeclaredFields()) {
      if (field.getType() == Locale.class
          && Modifier.isPublic(field.getModifiers())
          && Modifier.isStatic(field.getModifiers())) {
        declaredLocales.add((Locale) field.get(null));
      }
    }
    Iterable<Locale> registered = Locales.getLocales();
    for (Locale declared : declaredLocales) {
      assertThat(registered).as("Missing locale: " + declared).contains(declared);
    }
  }

  @Test
  void get_standardJdkLocales_mapCorrectly() {
    assertThat(Locales.get("de")).isEqualTo(Locale.GERMAN);
    assertThat(Locales.get("ja")).isEqualTo(Locale.JAPANESE);
    assertThat(Locales.get("it")).isEqualTo(Locale.ITALIAN);
    assertThat(Locales.get("ko")).isEqualTo(Locale.KOREAN);
    assertThat(Locales.get("zh")).isEqualTo(Locale.CHINESE);
  }

  @Test
  void get_languageCountryCode_returnsExactMatch() {
    assertThat(Locales.get("zh_CN")).isEqualTo(Locales.SIMPLIFIED_CHINESE);
    assertThat(Locales.get("zh_TW")).isEqualTo(Locales.TRADITIONAL_CHINESE);
  }

  @Test
  void get_minecraftLowercaseFormat_resolvesCorrectly() {
    assertThat(Locales.get("zh_cn")).isEqualTo(Locales.SIMPLIFIED_CHINESE);
    assertThat(Locales.get("zh_tw")).isEqualTo(Locales.TRADITIONAL_CHINESE);
  }

  @Test
  void get_languageCountryCode_fallsBackToBaseLanguage() {
    assertThat(Locales.get("en_gb")).isEqualTo(Locales.ENGLISH);
    assertThat(Locales.get("en_us")).isEqualTo(Locales.ENGLISH);
    assertThat(Locales.get("pt_br")).isEqualTo(Locales.PORTUGUESE);
    assertThat(Locales.get("es_mx")).isEqualTo(Locales.SPANISH);
  }

  @Test
  void get_caseInsensitive_resolvesCorrectly() {
    assertThat(Locales.get("FR")).isEqualTo(Locales.FRENCH);
    assertThat(Locales.get("De")).isEqualTo(Locales.GERMAN);
  }
}
