package dev.demeng.pluginbase.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Function;
import org.junit.jupiter.api.Test;

class BasePluginDetectionTest {

  @Test
  void detectPaper_returnsTrue_whenServerBuildInfoClassResolves() {
    Function<String, Class<?>> lookup =
        name -> name.equals("io.papermc.paper.ServerBuildInfo") ? Object.class : null;

    assertThat(BasePlugin.detectPaper(lookup)).isTrue();
  }

  @Test
  void detectPaper_returnsFalse_whenServerBuildInfoMissing() {
    Function<String, Class<?>> lookup = name -> null;

    assertThat(BasePlugin.detectPaper(lookup)).isFalse();
  }

  @Test
  void detectPaper_isFalse_whenLookupReturnsForUnrelatedClass() {
    Function<String, Class<?>> lookup =
        name -> name.equals("java.lang.String") ? String.class : null;

    assertThat(BasePlugin.detectPaper(lookup)).isFalse();
  }
}
