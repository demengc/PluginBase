package dev.demeng.pluginbase.cooldown;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CooldownMapTest {

  @Test
  void create_returnsNewMapWithBase() {
    Cooldown base = Cooldown.of(5, TimeUnit.SECONDS);
    CooldownMap<String> map = CooldownMap.create(base);
    assertThat(map.getBase()).isSameAs(base);
  }

  @Test
  void get_autoPopulatesCooldown() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(5, TimeUnit.SECONDS));
    Cooldown cd = map.get("player1");
    assertThat(cd).isNotNull();
    assertThat(cd.getDuration()).isEqualTo(5000);
  }

  @Test
  void get_returnsSameInstanceForSameKey() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(5, TimeUnit.SECONDS));
    Cooldown first = map.get("key");
    Cooldown second = map.get("key");
    assertThat(first).isSameAs(second);
  }

  @Test
  void test_delegatesToCooldown() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(1, TimeUnit.HOURS));
    assertThat(map.test("key")).isTrue();
    assertThat(map.test("key")).isFalse();
  }

  @Test
  void test_independentPerKey() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(1, TimeUnit.HOURS));
    assertThat(map.test("a")).isTrue();
    assertThat(map.test("b")).isTrue();
    assertThat(map.test("a")).isFalse();
  }

  @Test
  void put_rejectsWrongDuration() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(5, TimeUnit.SECONDS));
    Cooldown wrong = Cooldown.of(10, TimeUnit.SECONDS);
    assertThatThrownBy(() -> map.put("key", wrong))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void put_acceptsMatchingDuration() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(5, TimeUnit.SECONDS));
    Cooldown matching = Cooldown.of(5, TimeUnit.SECONDS);
    matching.reset();
    map.put("key", matching);
    assertThat(map.get("key").getLastTested()).isPresent();
  }

  @Test
  void getAll_reflectsEntries() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(5, TimeUnit.SECONDS));
    map.get("a");
    map.get("b");
    assertThat(map.getAll()).containsKeys("a", "b");
  }

  @Test
  void remainingMillis_delegatesToCooldown() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(5, TimeUnit.SECONDS));
    map.test("key");
    assertThat(map.remainingMillis("key")).isPositive();
  }

  @Test
  void reset_clearsCooldownForKey() {
    CooldownMap<String> map = CooldownMap.create(Cooldown.of(1, TimeUnit.HOURS));
    map.test("key");
    assertThat(map.testSilently("key")).isFalse();
    map.reset("key");
    assertThat(map.testSilently("key")).isFalse();
  }
}
