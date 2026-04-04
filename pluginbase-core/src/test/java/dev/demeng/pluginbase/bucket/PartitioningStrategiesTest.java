package dev.demeng.pluginbase.bucket;

import static org.assertj.core.api.Assertions.assertThat;

import dev.demeng.pluginbase.bucket.factory.BucketFactory;
import dev.demeng.pluginbase.bucket.partitioning.PartitioningStrategies;
import dev.demeng.pluginbase.bucket.partitioning.PartitioningStrategy;
import org.junit.jupiter.api.Test;

class PartitioningStrategiesTest {

  @Test
  void random_alwaysReturnsValidIndex() {
    PartitioningStrategy<String> strategy = PartitioningStrategies.random();
    Bucket<String> bucket = BucketFactory.newHashSetBucket(4, strategy);
    for (int i = 0; i < 200; i++) {
      int index = strategy.allocate("item" + i, bucket);
      assertThat(index).isBetween(0, 3);
    }
  }

  @Test
  void lowestSize_allocatesToEmptyPartitionFirst() {
    PartitioningStrategy<String> lowestSize = PartitioningStrategies.lowestSize();
    PartitioningStrategy<String> cycle = PartitioningStrategies.nextInCycle();
    Bucket<String> bucket = BucketFactory.newHashSetBucket(3, cycle);
    bucket.add("a");
    int index = lowestSize.allocate("b", bucket);
    assertThat(bucket.getPartition(index)).isEmpty();
  }

  @Test
  void lowestSize_allocatesToSmallestPartition() {
    PartitioningStrategy<String> lowestSize = PartitioningStrategies.lowestSize();
    PartitioningStrategy<String> cycle = PartitioningStrategies.nextInCycle();
    Bucket<String> bucket = BucketFactory.newHashSetBucket(3, cycle);
    bucket.add("a");
    bucket.add("b");
    bucket.add("c");
    bucket.add("d");
    bucket.add("e");
    BucketPartition<String> smallest = null;
    for (BucketPartition<String> p : bucket.getPartitions()) {
      if (smallest == null || p.size() < smallest.size()) {
        smallest = p;
      }
    }
    int index = lowestSize.allocate("next", bucket);
    assertThat(bucket.getPartition(index).size()).isEqualTo(smallest.size());
  }

  @Test
  void nextInCycle_advancesSequentiallyWithWrapping() {
    PartitioningStrategy<String> strategy = PartitioningStrategies.nextInCycle();
    Bucket<String> bucket = BucketFactory.newHashSetBucket(3, strategy);
    int first = strategy.allocate("a", bucket);
    int second = strategy.allocate("b", bucket);
    int third = strategy.allocate("c", bucket);
    int fourth = strategy.allocate("d", bucket);
    assertThat(second).isNotEqualTo(first);
    assertThat(third).isNotEqualTo(second);
    assertThat(fourth).isEqualTo(first);
  }

  @Test
  void previousInCycle_retreatsSequentiallyWithWrapping() {
    PartitioningStrategy<String> strategy = PartitioningStrategies.previousInCycle();
    Bucket<String> bucket = BucketFactory.newHashSetBucket(3, strategy);
    int first = strategy.allocate("a", bucket);
    int second = strategy.allocate("b", bucket);
    int third = strategy.allocate("c", bucket);
    int fourth = strategy.allocate("d", bucket);
    assertThat(second).isNotEqualTo(first);
    assertThat(third).isNotEqualTo(second);
    assertThat(fourth).isEqualTo(first);
  }

  @Test
  void allStrategies_workWithSinglePartition() {
    for (PartitioningStrategy<String> strategy :
        new PartitioningStrategy[] {
          PartitioningStrategies.random(),
          PartitioningStrategies.lowestSize(),
          PartitioningStrategies.nextInCycle(),
          PartitioningStrategies.previousInCycle()
        }) {
      Bucket<String> bucket = BucketFactory.newHashSetBucket(1, strategy);
      assertThat(strategy.allocate("x", bucket)).isZero();
    }
  }
}
