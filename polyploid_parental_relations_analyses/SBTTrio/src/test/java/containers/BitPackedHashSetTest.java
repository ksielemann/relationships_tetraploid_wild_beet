package containers;

import containers.bit_packed_hash_set.BitPackedHashSet;
import containers.bit_packed_hash_set.BitPackedHashSetFactory;
import hash_functions.MurmurHash32Finalizer;
import hash_functions.MurmurHash64Finalizer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BitPackedHashSetTest {

    @Test
    void createHashSetMurmur32() {
        BitPackedHashSet hashSet = new BitPackedHashSet(14);

        assertThat(hashSet.getKmerSize()).isEqualTo(14);
        assertThat(hashSet.getHashFunction()).isInstanceOf(MurmurHash32Finalizer.class);
        assertThat(hashSet.getNumElements()).isEqualTo(0);
        assertThat(hashSet.getTable()).isNull();
    }

    @Test
    void createHashSetMurmurHash64() {
        BitPackedHashSet hashSet = new BitPackedHashSet(21);

        assertThat(hashSet.getKmerSize()).isEqualTo(21);
        assertThat(hashSet.getHashFunction()).isInstanceOf(MurmurHash64Finalizer.class);
        assertThat(hashSet.getNumElements()).isEqualTo(0);
        assertThat(hashSet.getTable()).isNull();
    }

    @Test
    void createHashTableWithSize() {
        BitPackedHashSet hashSet = new BitPackedHashSet(21, 10);

        assertThat(hashSet.getKmerSize()).isEqualTo(21);
        assertThat(hashSet.getHashFunction()).isInstanceOf(MurmurHash64Finalizer.class);
        assertThat(hashSet.getNumElements()).isEqualTo(0);
        assertThat(hashSet.getTable()).isNotNull();
        assertThat(hashSet.getTable().size()).isEqualTo(32);
    }

    @Test
    void build() {
        BitPackedHashSet hashSet = new BitPackedHashSet(4);
        hashSet.build(TestUtil.PATTERNS);

        assertThat(hashSet.getNumElements()).isEqualTo(TestUtil.PATTERNS.length);
        assertThat(hashSet.getTable()).isNotNull();
        assertThat(hashSet.getTable().size()).isEqualTo(32);

        long[] expected = Arrays.copyOf(TestUtil.PATTERNS, TestUtil.PATTERNS.length + 1);
        expected[expected.length - 1] = 256;

        long[] values = new long[32];
        for (int i = 0; i < 32; i++) {
            values[i] = hashSet.getTable().get(i);
        }

        assertThat(values).containsOnly(expected);
    }

    @Test
    void search() {
        TestUtil.search(new BitPackedHashSetFactory());
    }

    @Test
    void add() {
        BitPackedHashSet hashSet = new BitPackedHashSet(4);
        hashSet.build(TestUtil.PATTERNS);

        assertThat(hashSet.add(57L)).isFalse();
        assertThat(hashSet.search(173L)).isFalse();
        assertThat(hashSet.add(173L)).isTrue();
        assertThat(hashSet.search(173L)).isTrue();
    }

    @Test
    void rehash() {
        BitPackedHashSet hashSet = new BitPackedHashSet(4, 2);
        assertThat(hashSet.getTable().size()).isEqualTo(4);

        // corresponds to ["GATA", "TATC", "ACTT", "CTAT", "TTGA"]
        long[] patterns = new long[] {0b10001100L, 0b11001101L, 0b11111L, 0b1110011L, 0b11111000L};

        for (long pattern: patterns) {
            hashSet.add(pattern);
        }

        assertThat(hashSet.getTable().size()).isEqualTo(16);
        assertThat(hashSet.getNumElements()).isEqualTo(5);
        for (long pattern: patterns) {
            assertThat(hashSet.search(pattern)).isTrue();
        }
    }

    @Test
    void iterate() {
        BitPackedHashSet hashSet = new BitPackedHashSet(4);
        hashSet.build(TestUtil.PATTERNS);

        List<Long> content = new ArrayList<>(TestUtil.PATTERNS.length);
        for (long pattern : hashSet) {
            content.add(pattern);
        }

        assertThat(content.size()).isEqualTo(TestUtil.PATTERNS.length);
        for (long pattern : TestUtil.PATTERNS) {
            assertThat(content).contains(pattern);
        }
    }

}
