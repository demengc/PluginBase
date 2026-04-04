package dev.demeng.pluginbase.cooldown;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CooldownTest {

  @Test
  void of_storesDurationInMillis() {
    Cooldown cd = Cooldown.of(2, TimeUnit.SECONDS);
    assertThat(cd.getDuration()).isEqualTo(2000);
  }

  @Test
  void ofTicks_convertsToMillis() {
    Cooldown cd = Cooldown.ofTicks(20);
    assertThat(cd.getDuration()).isEqualTo(1000);
  }

  @Test
  void testSilently_beforeAnyTest_returnsTrue() {
    Cooldown cd = Cooldown.of(1, TimeUnit.HOURS);
    assertThat(cd.testSilently()).isTrue();
  }

  @Test
  void test_resetsTimer() {
    Cooldown cd = Cooldown.of(1, TimeUnit.HOURS);
    assertThat(cd.test()).isTrue();
    assertThat(cd.getLastTested()).isPresent();
  }

  @Test
  void test_whileCooldownActive_returnsFalse() {
    Cooldown cd = Cooldown.of(1, TimeUnit.HOURS);
    cd.test();
    assertThat(cd.test()).isFalse();
  }

  @Test
  void test_afterCooldownExpires_returnsTrue() {
    Cooldown cd = Cooldown.of(1, TimeUnit.MILLISECONDS);
    cd.test();
    try {
      Thread.sleep(5);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    assertThat(cd.test()).isTrue();
  }

  @Test
  void remainingMillis_neverTested_returnsZero() {
    Cooldown cd = Cooldown.of(5, TimeUnit.SECONDS);
    assertThat(cd.remainingMillis()).isZero();
  }

  @Test
  void remainingMillis_afterTest_returnsPositive() {
    Cooldown cd = Cooldown.of(5, TimeUnit.SECONDS);
    cd.test();
    assertThat(cd.remainingMillis()).isPositive();
  }

  @Test
  void remainingTime_convertsUnits() {
    Cooldown cd = Cooldown.of(10, TimeUnit.SECONDS);
    cd.test();
    assertThat(cd.remainingTime(TimeUnit.SECONDS)).isBetween(9L, 10L);
  }

  @Test
  void reset_updatesLastTested() {
    Cooldown cd = Cooldown.of(1, TimeUnit.SECONDS);
    assertThat(cd.getLastTested()).isEmpty();
    cd.reset();
    assertThat(cd.getLastTested()).isPresent();
  }

  @Test
  void getLastTested_neverTested_isEmpty() {
    Cooldown cd = Cooldown.of(1, TimeUnit.SECONDS);
    assertThat(cd.getLastTested()).isEqualTo(OptionalLong.empty());
  }

  @Test
  void setLastTested_zeroOrNegative_clearsValue() {
    Cooldown cd = Cooldown.of(1, TimeUnit.SECONDS);
    cd.reset();
    cd.setLastTested(0);
    assertThat(cd.getLastTested()).isEmpty();
    cd.reset();
    cd.setLastTested(-1);
    assertThat(cd.getLastTested()).isEmpty();
  }

  @Test
  void copy_createsIndependentInstance() {
    Cooldown original = Cooldown.of(5, TimeUnit.SECONDS);
    original.test();
    Cooldown copy = original.copy();
    assertThat(copy.getDuration()).isEqualTo(original.getDuration());
    assertThat(copy.getLastTested()).isEmpty();
  }
}
