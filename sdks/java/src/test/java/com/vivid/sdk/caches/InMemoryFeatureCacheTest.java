package com.vivid.sdk.caches;

import com.vivid.clients.api.Feature;
import com.vivid.sdk.FeatureCache;
import com.vivid.sdk.Subscription;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryFeatureCacheTest {

    @Test
    void testCapacityRetention() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().maxCapacity(1).build();
        Feature feature1 = feature("1");
        Feature feature2 = feature("2");

        // Act
        assertNull(cache.set(feature1));
        assertNull(cache.set(feature2));

        // Assert
        assertEquals(1, cache.getAll().size());
        assertEquals(feature2, cache.getAll().getFirst());
    }

    @Test
    void getReturnsCachedFeatureById() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        Feature feature = feature("feature-1");

        // Act
        cache.set(feature);

        // Assert
        assertEquals(feature, cache.get("feature-1"));
    }

    @Test
    void getReturnsNullForMissingFeatureWithDefaultMissHandler() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        // Act
        Feature result = cache.get("missing-feature");

        // Assert
        assertNull(result);
    }

    @Test
    void getUsesCustomMissHandlerAndCachesResult() {
        // Arrange
        AtomicInteger missCounter = new AtomicInteger();
        Feature resolvedFeature = feature("resolved-feature");

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .missHandler(key -> {
                    missCounter.incrementAndGet();
                    return resolvedFeature;
                })
                .build();

        // Act
        Feature firstResult = cache.get("unknown-key");
        Feature secondResult = cache.get("unknown-key");

        // Assert
        assertEquals(resolvedFeature, firstResult);
        assertEquals(resolvedFeature, secondResult);
        assertEquals(1, missCounter.get());
    }

    @Test
    void getRegistersMissKeyAsAlias() {
        // Arrange
        Feature resolvedFeature = feature("resolved-feature");

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .missHandler(key -> resolvedFeature)
                .build();

        // Act
        Feature firstResult = cache.get("external-key");
        Feature secondResult = cache.get("external-key");

        // Assert
        assertEquals(resolvedFeature, firstResult);
        assertEquals(resolvedFeature, secondResult);
    }

    @Test
    void setAliasAllowsLookupByAlias() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        Feature feature = feature("feature-id", "feature-name", "feature-key");

        // Act
        cache.set(feature);
        cache.setAlias("my-alias", feature);

        // Assert
        assertEquals(feature, cache.get("my-alias"));
    }

    @Test
    void invalidateRemovesFeatureById() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        Feature feature = feature("feature-1");

        cache.set(feature);

        // Act
        Feature removed = cache.invalidate("feature-1");

        // Assert
        assertEquals(feature, removed);
        assertNull(cache.get("feature-1"));
        assertTrue(cache.getAll().isEmpty());
    }

    @Test
    void invalidateRemovesFeatureByAlias() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        Feature feature = feature("feature-1");

        cache.set(feature);
        cache.setAlias("alias-1", feature);

        // Act
        Feature removed = cache.invalidate("alias-1");

        // Assert
        assertEquals(feature, removed);
        assertNull(cache.get("feature-1"));
        assertTrue(cache.getAll().isEmpty());
    }

    @Test
    void invalidateReturnsNullWhenFeatureDoesNotExist() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        // Act
        Feature removed = cache.invalidate("missing");

        // Assert
        assertNull(removed);
    }

    @Test
    void invalidateAllRemovesAllFeatures() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        cache.set(feature("feature-1"));
        cache.set(feature("feature-2"));
        cache.set(feature("feature-3"));

        // Act
        cache.invalidate();

        // Assert
        assertTrue(cache.getAll().isEmpty());
    }

    @Test
    void setReturnsPreviousFeatureWhenIncomingFeatureIsNewer() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature oldFeature = feature("feature-1", Instant.parse("2024-01-01T00:00:00Z"));
        Feature newFeature = feature("feature-1", Instant.parse("2024-01-02T00:00:00Z"));

        // Act
        Feature firstPrevious = cache.set(oldFeature);
        Feature secondPrevious = cache.set(newFeature);

        // Assert
        assertNull(firstPrevious);
        assertEquals(oldFeature, secondPrevious);
        assertEquals(newFeature, cache.get("feature-1"));
    }

    @Test
    void setDoesNotReplaceExistingFeatureWhenIncomingFeatureIsOlder() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature newerFeature = feature("feature-1", Instant.parse("2024-01-02T00:00:00Z"));
        Feature olderFeature = feature("feature-1", Instant.parse("2024-01-01T00:00:00Z"));

        // Act
        cache.set(newerFeature);
        Feature previous = cache.set(olderFeature);

        // Assert
        assertNull(previous);
        assertEquals(newerFeature, cache.get("feature-1"));
    }

    @Test
    void setDoesNotReplaceExistingFeatureWhenIncomingFeatureHasSameTimestamp() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Instant timestamp = Instant.parse("2024-01-01T00:00:00Z");
        Feature firstFeature = feature("feature-1", "name-1", "key-1", timestamp);
        Feature secondFeature = feature("feature-1", "name-2", "key-2", timestamp);

        // Act
        cache.set(firstFeature);
        Feature previous = cache.set(secondFeature);

        // Assert
        assertNull(previous);
        assertEquals(firstFeature, cache.get("feature-1"));
    }

    @Test
    void setAllStoresAllFeatures() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature feature1 = feature("feature-1");
        Feature feature2 = feature("feature-2");
        Feature feature3 = feature("feature-3");

        // Act
        cache.setAll(List.of(feature1, feature2, feature3));

        // Assert
        assertEquals(3, cache.getAll().size());
        assertEquals(feature1, cache.get("feature-1"));
        assertEquals(feature2, cache.get("feature-2"));
        assertEquals(feature3, cache.get("feature-3"));
    }

    @Test
    void setAllReplacesCacheContentAndRemovesEntriesNotIncludedInNewList() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature existingFeature = feature("existing-feature");
        Feature retainedFeature = feature("retained-feature", Instant.parse("2024-01-01T00:00:00Z"));
        Feature updatedRetainedFeature = feature("retained-feature", Instant.parse("2024-01-02T00:00:00Z"));
        Feature newFeature = feature("new-feature");

        cache.setAll(List.of(existingFeature, retainedFeature));

        // Act
        cache.setAll(List.of(updatedRetainedFeature, newFeature));

        // Assert
        assertNull(cache.get("existing-feature"));
        assertEquals(updatedRetainedFeature, cache.get("retained-feature"));
        assertEquals(newFeature, cache.get("new-feature"));
        assertEquals(2, cache.getAll().size());
    }

    @Test
    void setAllRespectsMaxCapacity() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .maxCapacity(2)
                .build();

        Feature feature1 = feature("feature-1");
        Feature feature2 = feature("feature-2");
        Feature feature3 = feature("feature-3");

        // Act
        cache.setAll(List.of(feature1, feature2, feature3));

        // Assert
        assertEquals(2, cache.getAll().size());
    }

    @Test
    void getAllReturnsUnmodifiableList() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        cache.set(feature("feature-1"));

        // Act
        List<Feature> features = cache.getAll();

        // Assert
        assertThrows(UnsupportedOperationException.class, () -> features.add(feature("feature-2")));
    }

    @Test
    void disabledCacheDoesNotStoreOrReturnValues() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .enabled(false)
                .build();

        Feature feature = feature("feature-1");

        // Act
        Feature previous = cache.set(feature);

        // Assert
        assertNull(previous);
        assertNull(cache.get("feature-1"));
        assertTrue(cache.getAll().isEmpty());
        assertNull(cache.invalidate("feature-1"));
    }

    @Test
    void cacheCanBeDisabledAndEnabledAgain() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        Feature feature1 = feature("feature-1");
        Feature feature2 = feature("feature-2");

        cache.set(feature1);

        // Act
        cache.enable(false);
        Feature disabledGetResult = cache.get("feature-1");
        Feature disabledSetResult = cache.set(feature2);

        cache.enable(true);

        // Assert
        assertNull(disabledGetResult);
        assertNull(disabledSetResult);
        assertEquals(feature1, cache.get("feature-1"));
        assertNull(cache.get("feature-2"));
    }

    @Test
    void builderRejectsMaxCapacityZero() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> InMemoryFeatureCache.builder().maxCapacity(0)
        );
    }

    @Test
    void builderRejectsNegativeMaxCapacity() {
        // Act & Assert
        assertThrows(
                IllegalArgumentException.class,
                () -> InMemoryFeatureCache.builder().maxCapacity(-1)
        );
    }

    @Test
    void builderRejectsNullMissHandler() {
        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> InMemoryFeatureCache.builder().missHandler(null)
        );
    }

    @Test
    void builderRejectsNullWriteLock() {
        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> InMemoryFeatureCache.builder().writeLock(null)
        );
    }

    @Test
    void builderRejectsNullRetentionTime() {
        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> InMemoryFeatureCache.builder().retentionTime(null)
        );
    }

    @Test
    void builderRejectsNullClock() {
        // Act & Assert
        assertThrows(
                NullPointerException.class,
                () -> InMemoryFeatureCache.builder().clock(null)
        );
    }

    @Test
    void capacityEvictsLeastRecentlyAccessedFeature() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .maxCapacity(2)
                .clock(clock)
                .build();

        Feature feature1 = feature("feature-1");
        Feature feature2 = feature("feature-2");
        Feature feature3 = feature("feature-3");

        cache.set(feature1);

        clock.advance(Duration.ofSeconds(1));
        cache.set(feature2);

        clock.advance(Duration.ofSeconds(1));
        assertEquals(feature1, cache.get("feature-1"));

        clock.advance(Duration.ofSeconds(1));

        // Act
        cache.set(feature3);

        // Assert
        assertEquals(feature1, cache.get("feature-1"));
        assertNull(cache.get("feature-2"));
        assertEquals(feature3, cache.get("feature-3"));
        assertEquals(2, cache.getAll().size());
    }

    @Test
    void capacityEvictionNotifiesSubscribersAboutRemovedFeature() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .maxCapacity(1)
                .build();

        Feature feature1 = feature("feature-1");
        Feature feature2 = feature("feature-2");
        RecordingCallback callback = new RecordingCallback();

        cache.subscribe("feature-1", callback);

        // Act
        cache.set(feature1);
        cache.set(feature2);

        // Assert
        assertEquals(List.of(feature1), callback.removedFeatures);
    }

    @Test
    void retentionTimeEvictsExpiredEntryOnGetAll() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .retentionTime(Duration.ofMinutes(5))
                .clock(clock)
                .build();

        Feature feature = feature("feature-1");
        cache.set(feature);

        // Act
        clock.advance(Duration.ofMinutes(6));

        // Assert
        assertTrue(cache.getAll().isEmpty());
    }

    @Test
    void retentionTimeEvictsExpiredEntryOnGet() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .retentionTime(Duration.ofMinutes(5))
                .clock(clock)
                .build();

        Feature feature = feature("feature-1");
        cache.set(feature);

        // Act
        clock.advance(Duration.ofMinutes(6));
        Feature result = cache.get("feature-1");

        // Assert
        assertNull(result);
        assertTrue(cache.getAll().isEmpty());
    }

    @Test
    void retentionTimeDoesNotExpireEntryBeforeDurationElapsed() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .retentionTime(Duration.ofMinutes(5))
                .clock(clock)
                .build();

        Feature feature = feature("feature-1");
        cache.set(feature);

        // Act
        clock.advance(Duration.ofMinutes(4));

        // Assert
        assertEquals(feature, cache.get("feature-1"));
        assertEquals(1, cache.getAll().size());
    }

    @Test
    void retentionTimeExpiresByCreationTimeAndNotByLastAccessTime() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .retentionTime(Duration.ofMinutes(5))
                .clock(clock)
                .build();

        Feature feature = feature("feature-1");
        cache.set(feature);

        clock.advance(Duration.ofMinutes(4));
        assertEquals(feature, cache.get("feature-1"));

        // Act
        clock.advance(Duration.ofMinutes(2));

        // Assert
        assertNull(cache.get("feature-1"));
    }

    @Test
    void zeroRetentionDisablesTimeBasedEviction() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .retentionTime(Duration.ZERO)
                .clock(clock)
                .build();

        Feature feature = feature("feature-1");
        cache.set(feature);

        // Act
        clock.advance(Duration.ofDays(365));

        // Assert
        assertEquals(feature, cache.get("feature-1"));
    }

    @Test
    void negativeRetentionDisablesTimeBasedEviction() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .retentionTime(Duration.ofSeconds(-1))
                .clock(clock)
                .build();

        Feature feature = feature("feature-1");
        cache.set(feature);

        // Act
        clock.advance(Duration.ofDays(365));

        // Assert
        assertEquals(feature, cache.get("feature-1"));
    }

    @Test
    void expiredEntryCanBeReplacedByIncomingFeatureEvenWhenIncomingTimestampIsOlder() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .retentionTime(Duration.ofMinutes(5))
                .clock(clock)
                .build();

        Feature originalFeature = feature("feature-1", Instant.parse("2024-01-10T00:00:00Z"));
        Feature replacementFeature = feature("feature-1", Instant.parse("2024-01-01T00:00:00Z"));

        cache.set(originalFeature);

        // Act
        clock.advance(Duration.ofMinutes(6));
        Feature previous = cache.set(replacementFeature);

        // Assert
        assertNull(previous);
        assertEquals(replacementFeature, cache.get("feature-1"));
    }

    @Test
    void retentionEvictionNotifiesSubscribersAboutRemovedFeature() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .retentionTime(Duration.ofMinutes(5))
                .clock(clock)
                .build();

        Feature feature = feature("feature-1");
        RecordingCallback callback = new RecordingCallback();

        cache.subscribe("feature-1", callback);
        cache.set(feature);

        // Act
        clock.advance(Duration.ofMinutes(6));
        cache.getAll();

        // Assert
        assertEquals(List.of(feature), callback.removedFeatures);
    }

    @Test
    void setNotifiesSubscribersWhenFeatureIsInserted() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        Feature feature = feature("feature-1");
        RecordingCallback callback = new RecordingCallback();

        cache.subscribe("feature-1", callback);

        // Act
        cache.set(feature);

        // Assert
        assertEquals(List.of(feature), callback.nextFeatures);
        assertTrue(callback.removedFeatures.isEmpty());
    }

    @Test
    void setNotifiesSubscribersWhenFeatureIsUpdatedWithNewerTimestamp() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature oldFeature = feature("feature-1", Instant.parse("2024-01-01T00:00:00Z"));
        Feature newFeature = feature("feature-1", Instant.parse("2024-01-02T00:00:00Z"));
        RecordingCallback callback = new RecordingCallback();

        cache.subscribe("feature-1", callback);

        // Act
        cache.set(oldFeature);
        cache.set(newFeature);

        // Assert
        assertEquals(List.of(oldFeature, newFeature), callback.nextFeatures);
    }

    @Test
    void setDoesNotNotifySubscribersWhenIncomingFeatureIsOlder() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature newerFeature = feature("feature-1", Instant.parse("2024-01-02T00:00:00Z"));
        Feature olderFeature = feature("feature-1", Instant.parse("2024-01-01T00:00:00Z"));
        RecordingCallback callback = new RecordingCallback();

        cache.subscribe("feature-1", callback);

        // Act
        cache.set(newerFeature);
        cache.set(olderFeature);

        // Assert
        assertEquals(List.of(newerFeature), callback.nextFeatures);
    }

    @Test
    void invalidateNotifiesSubscribersAboutRemovedFeature() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        Feature feature = feature("feature-1");
        RecordingCallback callback = new RecordingCallback();

        cache.subscribe("feature-1", callback);
        cache.set(feature);

        // Act
        cache.invalidate("feature-1");

        // Assert
        assertEquals(List.of(feature), callback.removedFeatures);
    }

    @Test
    void invalidateAllNotifiesSubscribersAboutRemovedFeatures() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature feature1 = feature("feature-1");
        Feature feature2 = feature("feature-2");
        RecordingCallback callback1 = new RecordingCallback();
        RecordingCallback callback2 = new RecordingCallback();

        cache.subscribe("feature-1", callback1);
        cache.subscribe("feature-2", callback2);

        cache.set(feature1);
        cache.set(feature2);

        // Act
        cache.invalidate();

        // Assert
        assertEquals(List.of(feature1), callback1.removedFeatures);
        assertEquals(List.of(feature2), callback2.removedFeatures);
    }

    @Test
    void subscriptionCancelStopsFurtherNotifications() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature feature1 = feature("feature-1", Instant.parse("2024-01-01T00:00:00Z"));
        Feature feature2 = feature("feature-1", Instant.parse("2024-01-02T00:00:00Z"));
        RecordingCallback callback = new RecordingCallback();

        Subscription subscription = cache.subscribe("feature-1", callback);

        // Act
        cache.set(feature1);
        subscription.cancel();
        cache.set(feature2);
        cache.invalidate("feature-1");

        // Assert
        assertEquals(List.of(feature1), callback.nextFeatures);
        assertTrue(callback.removedFeatures.isEmpty());
    }

    @Test
    void multipleSubscribersForSameFeatureAreNotified() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature feature = feature("feature-1");
        RecordingCallback callback1 = new RecordingCallback();
        RecordingCallback callback2 = new RecordingCallback();

        cache.subscribe("feature-1", callback1);
        cache.subscribe("feature-1", callback2);

        // Act
        cache.set(feature);

        // Assert
        assertEquals(List.of(feature), callback1.nextFeatures);
        assertEquals(List.of(feature), callback2.nextFeatures);
    }

    @Test
    void subscribersAreMatchedByFeatureName() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();

        Feature feature = feature("feature-id", "subscribed-name", "feature-key");
        RecordingCallback matchingCallback = new RecordingCallback();
        RecordingCallback nonMatchingCallback = new RecordingCallback();

        cache.subscribe("subscribed-name", matchingCallback);
        cache.subscribe("feature-id", nonMatchingCallback);

        // Act
        cache.set(feature);

        // Assert
        assertEquals(List.of(feature), matchingCallback.nextFeatures);
        assertTrue(nonMatchingCallback.nextFeatures.isEmpty());
    }

    @Test
    void setAliasDoesNothingWhenCacheIsDisabled() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder().build();
        Feature feature = feature("feature-1");

        cache.set(feature);
        cache.enable(false);

        // Act
        cache.setAlias("alias-1", feature);
        cache.enable(true);

        // Assert
        assertNull(cache.get("alias-1"));
        assertEquals(feature, cache.get("feature-1"));
    }

    @Test
    void disabledCacheDoesNotNotifySubscribers() {
        // Arrange
        InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
                .enabled(false)
                .build();

        Feature feature = feature("feature-1");
        RecordingCallback callback = new RecordingCallback();

        cache.subscribe("feature-1", callback);

        // Act
        cache.set(feature);
        cache.invalidate("feature-1");

        // Assert
        assertTrue(callback.nextFeatures.isEmpty());
        assertTrue(callback.removedFeatures.isEmpty());
    }

    @Test
    void constructorWithCapacityAndRetentionCreatesWorkingCache() {
        // Arrange
        InMemoryFeatureCache cache = new InMemoryFeatureCache(1, Duration.ZERO);
        Feature feature1 = feature("feature-1");
        Feature feature2 = feature("feature-2");

        // Act
        cache.set(feature1);
        cache.set(feature2);

        // Assert
        assertEquals(1, cache.getAll().size());
        assertEquals(feature2, cache.getAll().getFirst());
    }

    @Test
    void fullConstructorUsesProvidedClockForRetention() {
        // Arrange
        MutableClock clock = new MutableClock(Instant.parse("2024-01-01T00:00:00Z"));

        InMemoryFeatureCache cache = new InMemoryFeatureCache(
                true,
                key -> Feature.empty(),
                new Object(),
                10,
                Duration.ofMinutes(5),
                clock
        );

        Feature feature = feature("feature-1");
        cache.set(feature);

        // Act
        clock.advance(Duration.ofMinutes(6));

        // Assert
        assertNull(cache.get("feature-1"));
    }

    private static Instant defaultInstant = Instant.parse("2024-01-01T00:00:00Z");

    private static Feature feature(String id) {
        return feature(id, id, id, defaultInstant);
    }

    private static Feature feature(String id, Instant timestamp) {
        return feature(id, id, id, timestamp);
    }

    private static Feature feature(String id, String name, String key) {
        return feature(id, name, key, defaultInstant);
    }

    private static Feature feature(String id, String name, String key, Instant timestamp) {
        return Feature.builder()
                .id(id)
                .name(name)
                .key(key)
                .timestamp(timestamp)
                .build();
    }

    private static final class RecordingCallback implements FeatureCache.Callback {

        private final List<Feature> nextFeatures = new ArrayList<>();
        private final List<Feature> removedFeatures = new ArrayList<>();

        @Override
        public void onNext(Feature feature) {
            nextFeatures.add(feature);
        }

        @Override
        public void onRemove(Feature feature) {
            removedFeatures.add(feature);
        }
    }

    private static final class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zone;

        private MutableClock(Instant instant) {
            this(instant, ZoneOffset.UTC);
        }

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
