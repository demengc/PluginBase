package dev.demeng.pluginbase.bucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class CycleTest {

  @Test
  void current_initialPosition_returnsFirstElement() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    assertThat(cycle.current()).isEqualTo("a");
    assertThat(cycle.cursor()).isZero();
  }

  @Test
  void next_advancesCursorAndReturnsElement() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    assertThat(cycle.next()).isEqualTo("b");
    assertThat(cycle.cursor()).isEqualTo(1);
  }

  @Test
  void next_wrapsFromLastToFirst() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    cycle.setCursor(2);
    assertThat(cycle.next()).isEqualTo("a");
    assertThat(cycle.cursor()).isZero();
  }

  @Test
  void previous_retreatsCursorAndReturnsElement() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    cycle.setCursor(2);
    assertThat(cycle.previous()).isEqualTo("b");
    assertThat(cycle.cursor()).isEqualTo(1);
  }

  @Test
  void previous_wrapsFromFirstToLast() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    assertThat(cycle.previous()).isEqualTo("c");
    assertThat(cycle.cursor()).isEqualTo(2);
  }

  @Test
  void setCursor_validIndex_updatesCursor() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    cycle.setCursor(1);
    assertThat(cycle.current()).isEqualTo("b");
  }

  @Test
  void setCursor_invalidIndex_throwsIndexOutOfBounds() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    assertThatThrownBy(() -> cycle.setCursor(3)).isInstanceOf(IndexOutOfBoundsException.class);
    assertThatThrownBy(() -> cycle.setCursor(-1)).isInstanceOf(IndexOutOfBoundsException.class);
  }

  @Test
  void peekNext_doesNotAdvanceCursor() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    assertThat(cycle.peekNext()).isEqualTo("b");
    assertThat(cycle.cursor()).isZero();
  }

  @Test
  void peekPrevious_doesNotRetreatCursor() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    cycle.setCursor(1);
    assertThat(cycle.peekPrevious()).isEqualTo("a");
    assertThat(cycle.cursor()).isEqualTo(1);
  }

  @Test
  void copy_createsIndependentInstanceWithCursorResetToZero() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    cycle.setCursor(2);
    Cycle<String> copy = cycle.copy();
    assertThat(copy.cursor()).isZero();
    assertThat(copy.getBacking()).isEqualTo(cycle.getBacking());
    copy.next();
    assertThat(cycle.cursor()).isEqualTo(2);
  }

  @Test
  void getBacking_returnsImmutableList() {
    Cycle<String> cycle = Cycle.of(List.of("a", "b", "c"));
    assertThatThrownBy(() -> cycle.getBacking().add("d"))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void of_emptyList_throwsIllegalArgument() {
    assertThatThrownBy(() -> Cycle.of(List.of())).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void of_nullList_throwsIllegalArgument() {
    assertThatThrownBy(() -> Cycle.of(null)).isInstanceOf(IllegalArgumentException.class);
  }
}
