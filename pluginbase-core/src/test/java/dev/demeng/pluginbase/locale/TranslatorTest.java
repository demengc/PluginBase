package dev.demeng.pluginbase.locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TranslatorTest {

  private Translator translator;

  @BeforeEach
  void setUp() {
    translator = Translator.create();
    translator.clear();
  }

  private LocaleReader mockReader(Locale locale, String key, String value) {
    LocaleReader reader = mock(LocaleReader.class);
    when(reader.getLocale()).thenReturn(locale);
    when(reader.containsKey(key)).thenReturn(true);
    when(reader.get(key)).thenReturn(value);
    return reader;
  }

  @Test
  void get_returnsValueFromRegisteredReader() {
    translator.add(mockReader(Locale.ENGLISH, "greeting", "Hello"));
    assertThat(translator.get("greeting")).isEqualTo("Hello");
  }

  @Test
  void get_withSpecificLocale_fallsBackToDefaultLocale() {
    translator.add(mockReader(Locale.ENGLISH, "greeting", "Hello"));
    assertThat(translator.get("greeting", Locale.FRENCH)).isEqualTo("Hello");
  }

  @Test
  void get_keyNotFoundAnywhere_returnsKey() {
    assertThat(translator.get("missing.key")).isEqualTo("missing.key");
  }

  @Test
  void get_laterAddedReader_takesPriority() {
    translator.add(mockReader(Locale.ENGLISH, "greeting", "Hello"));
    translator.add(mockReader(Locale.ENGLISH, "greeting", "Hi"));
    assertThat(translator.get("greeting")).isEqualTo("Hi");
  }

  @Test
  void containsKey_currentLocale_returnsTrue() {
    translator.add(mockReader(Locale.ENGLISH, "greeting", "Hello"));
    assertThat(translator.containsKey("greeting")).isTrue();
  }

  @Test
  void containsKey_missingKey_returnsFalse() {
    assertThat(translator.containsKey("nonexistent")).isFalse();
  }

  @Test
  void containsKey_specificLocale_checksOnlyThatLocale() {
    translator.add(mockReader(Locale.FRENCH, "bonjour", "Bonjour"));
    assertThat(translator.containsKey("bonjour", Locale.FRENCH)).isTrue();
    assertThat(translator.containsKey("bonjour", Locale.GERMAN)).isFalse();
  }

  @Test
  void setLocale_changesDefaultLookupLocale() {
    translator.add(mockReader(Locale.FRENCH, "greeting", "Bonjour"));
    assertThat(translator.get("greeting")).isEqualTo("greeting");
    translator.setLocale(Locale.FRENCH);
    assertThat(translator.get("greeting")).isEqualTo("Bonjour");
  }

  @Test
  void clear_removesAllReaders() {
    translator.add(mockReader(Locale.ENGLISH, "greeting", "Hello"));
    translator.clear();
    assertThat(translator.get("greeting")).isEqualTo("greeting");
  }

  @Test
  void defaultLocale_isEnglish() {
    assertThat(translator.getLocale()).isEqualTo(Locale.ENGLISH);
  }
}
