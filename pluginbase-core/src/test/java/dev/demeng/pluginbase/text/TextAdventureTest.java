package dev.demeng.pluginbase.text;

import static org.assertj.core.api.Assertions.assertThat;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

class TextAdventureTest {

  @Test
  void miniMessage_returnsSameInstanceAcrossCalls() {
    assertThat(Text.miniMessage()).isSameAs(Text.miniMessage());
  }

  @Test
  void parseMini_returnsEmptyComponent_whenInputIsNull() {
    assertThat(Text.parseMini(null)).isEqualTo(Component.empty());
  }

  @Test
  void parseMini_parsesMiniMessageMarkup() {
    Component result = Text.parseMini("<red>hello");
    String legacy = Text.legacySerialize(result);
    assertThat(legacy).contains("hello");
    assertThat(legacy).startsWith("§c"); // §c == red in legacy section format
  }

  @Test
  void legacySerialize_roundTripsPlainText() {
    Component plain = Component.text("hello");
    assertThat(Text.legacySerialize(plain)).isEqualTo("hello");
  }
}
