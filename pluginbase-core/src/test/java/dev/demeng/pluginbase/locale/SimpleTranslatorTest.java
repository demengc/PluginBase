package dev.demeng.pluginbase.locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SimpleTranslatorTest {

  private SimpleTranslator translator;

  @BeforeEach
  void setUp() {
    translator = new SimpleTranslator();
  }

  @Test
  void getMissingKeyReturnsKey() {
    assertThat(translator.get("missing.key")).isEqualTo("missing.key");
  }

  @Test
  void getReturnsValueFromRegisteredReader() {
    final LocaleReader reader = mockReader(Locale.ENGLISH, "greeting", "Hello");
    translator.add(reader);

    assertThat(translator.get("greeting")).isEqualTo("Hello");
  }

  @Test
  void getFallsBackToDefaultLocale() {
    final LocaleReader englishReader = mockReader(Locale.ENGLISH, "greeting", "Hello");
    translator.add(englishReader);

    assertThat(translator.get("greeting", Locale.FRENCH)).isEqualTo("Hello");
  }

  @Test
  void getUsesRequestedLocaleWhenAvailable() {
    final LocaleReader englishReader = mockReader(Locale.ENGLISH, "greeting", "Hello");
    final LocaleReader frenchReader = mockReader(Locale.FRENCH, "greeting", "Bonjour");
    translator.add(englishReader);
    translator.add(frenchReader);

    assertThat(translator.get("greeting", Locale.FRENCH)).isEqualTo("Bonjour");
  }

  @Test
  void laterAddedReaderTakesPriority() {
    final LocaleReader first = mockReader(Locale.ENGLISH, "greeting", "Hello");
    final LocaleReader second = mockReader(Locale.ENGLISH, "greeting", "Hi");
    translator.add(first);
    translator.add(second);

    assertThat(translator.get("greeting")).isEqualTo("Hi");
  }

  @Test
  void containsKeyForDefaultLocale() {
    final LocaleReader reader = mockReader(Locale.ENGLISH, "greeting", "Hello");
    translator.add(reader);

    assertThat(translator.containsKey("greeting")).isTrue();
    assertThat(translator.containsKey("missing")).isFalse();
  }

  @Test
  void containsKeyForSpecificLocale() {
    final LocaleReader reader = mockReader(Locale.FRENCH, "greeting", "Bonjour");
    translator.add(reader);

    assertThat(translator.containsKey("greeting", Locale.FRENCH)).isTrue();
    assertThat(translator.containsKey("greeting", Locale.GERMAN)).isFalse();
  }

  @Test
  void setLocaleChangesDefaultLookup() {
    final LocaleReader frenchReader = mockReader(Locale.FRENCH, "greeting", "Bonjour");
    translator.add(frenchReader);

    assertThat(translator.get("greeting")).isEqualTo("greeting");

    translator.setLocale(Locale.FRENCH);
    assertThat(translator.get("greeting")).isEqualTo("Bonjour");
  }

  @Test
  void clearRemovesAllBundles() {
    final LocaleReader reader = mockReader(Locale.ENGLISH, "greeting", "Hello");
    translator.add(reader);
    assertThat(translator.get("greeting")).isEqualTo("Hello");

    translator.clear();
    assertThat(translator.get("greeting")).isEqualTo("greeting");
  }

  @Test
  void getArrayReturnsArrayFromReader() {
    final LocaleReader reader =
        mockArrayReader(Locale.ENGLISH, "lore", new String[] {"Line 1", "Line 2", "Line 3"});
    translator.add(reader);

    assertThat(translator.getArray("lore")).containsExactly("Line 1", "Line 2", "Line 3");
  }

  @Test
  void getArrayFallsBackToDefaultLocale() {
    final LocaleReader reader =
        mockArrayReader(Locale.ENGLISH, "lore", new String[] {"Line A", "Line B"});
    translator.add(reader);

    assertThat(translator.getArray("lore", Locale.FRENCH)).containsExactly("Line A", "Line B");
  }

  @Test
  void getArrayReturnsSingleKeyWhenMissing() {
    assertThat(translator.getArray("missing.lore")).containsExactly("missing.lore");
  }

  @Test
  void concurrentAddAndGetDoesNotThrow() throws Exception {
    final int threadCount = 20;
    final CyclicBarrier barrier = new CyclicBarrier(threadCount);
    final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
    final List<Future<?>> futures = new ArrayList<>();

    for (int i = 0; i < threadCount; i++) {
      final int index = i;
      futures.add(
          executor.submit(
              () -> {
                try {
                  barrier.await(5, TimeUnit.SECONDS);
                } catch (final Exception e) {
                  throw new RuntimeException(e);
                }

                if (index % 2 == 0) {
                  final Locale locale = new Locale("lang" + index);
                  final LocaleReader reader = mockReader(locale, "key" + index, "value" + index);
                  translator.add(reader);
                } else {
                  translator.get("key" + index);
                  translator.get("key" + index, Locale.ENGLISH);
                  translator.containsKey("key" + index);
                }
              }));
    }

    for (final Future<?> future : futures) {
      future.get(10, TimeUnit.SECONDS);
    }

    executor.shutdown();
    assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
  }

  private static LocaleReader mockReader(
      final Locale locale, final String key, final String value) {
    final LocaleReader reader = mock(LocaleReader.class);
    when(reader.getLocale()).thenReturn(locale);
    when(reader.containsKey(key)).thenReturn(true);
    when(reader.get(key)).thenReturn(value);
    return reader;
  }

  private static LocaleReader mockArrayReader(
      final Locale locale, final String key, final String[] value) {
    final LocaleReader reader = mock(LocaleReader.class);
    when(reader.getLocale()).thenReturn(locale);
    when(reader.containsKey(key)).thenReturn(true);
    when(reader.getArray(key)).thenReturn(value);
    return reader;
  }
}
