package dev.demeng.pluginbase.bucket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.demeng.pluginbase.bucket.factory.BucketFactory;
import dev.demeng.pluginbase.bucket.partitioning.PartitioningStrategies;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class BucketTest {

  private Bucket<String> createBucket(int partitions) {
    return BucketFactory.newHashSetBucket(partitions, PartitioningStrategies.nextInCycle());
  }

  @Test
  void add_elementExistsInBucketAndPartition() {
    Bucket<String> bucket = createBucket(3);
    bucket.add("a");
    assertThat(bucket).contains("a");
    boolean inPartition = bucket.getPartitions().stream().anyMatch(p -> p.contains("a"));
    assertThat(inPartition).isTrue();
  }

  @Test
  void add_null_throwsNullPointerException() {
    Bucket<String> bucket = createBucket(3);
    assertThatThrownBy(() -> bucket.add(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void add_duplicate_returnsFalse() {
    Bucket<String> bucket = createBucket(3);
    assertThat(bucket.add("a")).isTrue();
    assertThat(bucket.add("a")).isFalse();
    assertThat(bucket).hasSize(1);
  }

  @Test
  void remove_removesFromBucketAndPartition() {
    Bucket<String> bucket = createBucket(3);
    bucket.add("a");
    assertThat(bucket.remove("a")).isTrue();
    assertThat(bucket).doesNotContain("a");
    for (BucketPartition<String> p : bucket.getPartitions()) {
      assertThat(p).doesNotContain("a");
    }
  }

  @Test
  void remove_nonexistent_returnsFalse() {
    Bucket<String> bucket = createBucket(3);
    assertThat(bucket.remove("x")).isFalse();
  }

  @Test
  void clear_emptiesBucketAndAllPartitions() {
    Bucket<String> bucket = createBucket(3);
    bucket.add("a");
    bucket.add("b");
    bucket.add("c");
    bucket.clear();
    assertThat(bucket).isEmpty();
    for (BucketPartition<String> p : bucket.getPartitions()) {
      assertThat(p).isEmpty();
    }
  }

  @Test
  void bucketIterator_remove_propagatesToPartitions() {
    Bucket<String> bucket = createBucket(3);
    bucket.add("a");
    bucket.add("b");
    Iterator<String> it = bucket.iterator();
    String first = it.next();
    it.remove();
    assertThat(bucket).doesNotContain(first);
    for (BucketPartition<String> p : bucket.getPartitions()) {
      assertThat(p).doesNotContain(first);
    }
  }

  @Test
  void partitionRemove_propagatesToBucketContent() {
    Bucket<String> bucket = createBucket(1);
    bucket.add("a");
    BucketPartition<String> partition = bucket.getPartition(0);
    assertThat(partition.remove("a")).isTrue();
    assertThat(bucket).doesNotContain("a");
    assertThat(bucket).isEmpty();
  }

  @Test
  void partitionClear_removesOnlyThatPartitionsElementsFromBucket() {
    Bucket<String> bucket = createBucket(2);
    bucket.add("a");
    bucket.add("b");
    int totalBefore = bucket.size();
    BucketPartition<String> first = bucket.getPartition(0);
    int firstSize = first.size();
    first.clear();
    assertThat(first).isEmpty();
    assertThat(bucket).hasSize(totalBefore - firstSize);
  }

  @Test
  void partitionAdd_throwsUnsupportedOperationException() {
    Bucket<String> bucket = createBucket(3);
    BucketPartition<String> partition = bucket.getPartition(0);
    assertThatThrownBy(() -> partition.add("x")).isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void getPartitionCount_matchesConstructionSize() {
    assertThat(createBucket(5).getPartitionCount()).isEqualTo(5);
  }

  @Test
  void getPartition_invalidIndex_throwsIndexOutOfBounds() {
    Bucket<String> bucket = createBucket(3);
    assertThatThrownBy(() -> bucket.getPartition(3)).isInstanceOf(IndexOutOfBoundsException.class);
    assertThatThrownBy(() -> bucket.getPartition(-1)).isInstanceOf(IndexOutOfBoundsException.class);
  }
}
