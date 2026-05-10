package com.vivid.sdk.caches;

import com.vivid.clients.api.Feature;
import com.vivid.sdk.FeatureCache;
import com.vivid.sdk.Subscription;
import jakarta.annotation.Nullable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * In-memory implementation of {@link FeatureCache}.
 *
 * <p>
 * This cache stores feature states locally in memory and is intended for fast, thread-safe access
 * to feature flag values. Entries are stored in a {@link ConcurrentHashMap}, while write operations
 * that may affect multiple internal structures are guarded by a shared write lock.
 * </p>
 *
 * <h2>Aliases</h2>
 * <p>
 * Features are stored internally by their unique feature id. Additional lookup keys, such as a
 * feature name, key, or any externally known identifier, can be registered via
 * {@link #setAlias(String, Feature)}. When an alias is registered, lookups using that alias are
 * translated to the feature id before accessing the internal cache.
 * </p>
 *
 * <h2>Miss handling</h2>
 * <p>
 * If a requested feature is not present in the cache, the configured miss handler is called.
 * The default miss handler returns {@link Feature#empty()}. The returned feature is inserted into
 * the cache and the requested key is registered as alias for that feature.
 * </p>
 *
 * <h2>Update semantics</h2>
 * <p>
 * Calls to {@link #set(Feature)} and {@link #setAll(List)} update existing entries only if the
 * incoming feature is newer than the currently cached feature. Newness is determined by comparing
 * {@link Feature#timestamp()} values.
 * </p>
 *
 * <h2>Maximum capacity</h2>
 * <p>
 * The cache can be configured with a maximum capacity. If inserting or updating entries causes the
 * cache size to exceed this limit, the cache lazily removes entries until the configured capacity
 * is satisfied again.
 * </p>
 *
 * <p>
 * Capacity-based eviction removes the least recently accessed entries first. Access time is updated
 * when an entry is successfully read through {@link #get(String)} or when an existing entry is kept
 * during an update because the incoming feature was not newer.
 * </p>
 *
 * <h2>Retention time</h2>
 * <p>
 * The cache can also be configured with a retention time. A cached entry expires once the configured
 * duration has elapsed since the entry was written to the cache. Expiration is based on the entry's
 * creation time, not on the last access time.
 * </p>
 *
 * <p>
 * A retention time of {@link Duration#ZERO} or a negative duration disables time-based eviction.
 * This is also the default behavior.
 * </p>
 *
 * <h2>Lazy eviction</h2>
 * <p>
 * This cache does not start background threads and does not eagerly clean itself on a schedule.
 * Both retention-based eviction and capacity-based eviction are evaluated lazily during normal cache
 * operations such as {@link #get(String)}, {@link #set(Feature)}, {@link #setAll(List)}, and
 * {@link #getAll()}.
 * </p>
 *
 * <p>
 * This keeps the implementation lightweight and avoids background resource usage. It also means that
 * expired entries may remain in memory until the cache is accessed again.
 * </p>
 *
 * <h2>Subscriptions</h2>
 * <p>
 * Subscribers can be registered for a feature key via {@link #subscribe(String, FeatureCache.Callback)}.
 * Subscribers are notified when a feature is updated through {@link FeatureCache.Callback#onNext(Feature)}
 * or removed through {@link FeatureCache.Callback#onRemove(Feature)}.
 * </p>
 *
 * <p>
 * Entries removed due to invalidation, retention-time expiration, or capacity overflow also trigger
 * remove notifications.
 * </p>
 *
 * <h2>Thread safety</h2>
 * <p>
 * The cache uses concurrent data structures and synchronizes compound write operations with a shared
 * write lock. The implementation is safe for concurrent access by multiple threads.
 * </p>
 *
 * <h2>Builder usage</h2>
 * <p>
 * The preferred way to create customized instances is the {@link Builder}:
 * </p>
 *
 * <pre>{@code
 * InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
 *         .enabled(true)
 *         .maxCapacity(1_000)
 *         .retentionTime(Duration.ofMinutes(30))
 *         .build();
 * }</pre>
 *
 * <p>
 * A custom miss handler can be supplied as well:
 * </p>
 *
 * <pre>{@code
 * InMemoryFeatureCache cache = InMemoryFeatureCache.builder()
 *         .missHandler(key -> Feature.empty())
 *         .maxCapacity(500)
 *         .retentionTime(Duration.ofHours(1))
 *         .build();
 * }</pre>
 *
 * @see FeatureCache
 * @see Feature
 */
public class InMemoryFeatureCache implements FeatureCache {

    private volatile boolean enabled;
    private final Function<String, Feature> missHandler;
    private final int maxCapacity;
    private final Duration retentionTime;
    private final Clock clock;
    protected final Object writeLock;

    private final ConcurrentHashMap<String, List<FeatureCache.Callback>> subscriptions = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, String> translations = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<String, CacheEntry> content = new ConcurrentHashMap<>();

    /**
     * Creates an enabled cache with unlimited capacity and no time-based retention.
     *
     * <p>
     * The default miss handler returns {@link Feature#empty()}.
     * </p>
     */
    public InMemoryFeatureCache() {
        this(builder());
    }

    /**
     * Creates a cache with the given enabled state, unlimited capacity and no time-based retention.
     *
     * @param enabled whether the cache should initially be enabled
     */
    public InMemoryFeatureCache(boolean enabled) {
        this(builder().enabled(enabled));
    }

    /**
     * Creates an enabled cache with unlimited capacity, no time-based retention and the given miss handler.
     *
     * @param missHandler function used when a requested feature is missing from the cache
     */
    public InMemoryFeatureCache(Function<String, Feature> missHandler) {
        this(builder().missHandler(missHandler));
    }

    /**
     * Creates a cache with the given enabled state, unlimited capacity, no time-based retention
     * and the given miss handler.
     *
     * @param enabled whether the cache should initially be enabled
     * @param missHandler function used when a requested feature is missing from the cache
     */
    public InMemoryFeatureCache(boolean enabled, Function<String, Feature> missHandler) {
        this(builder()
                .enabled(enabled)
                .missHandler(missHandler));
    }

    /**
     * Creates a cache with custom capacity and retention settings.
     *
     * @param maxCapacity maximum number of entries to keep in memory
     * @param retentionTime duration after which entries expire; {@link Duration#ZERO} disables time-based eviction
     */
    public InMemoryFeatureCache(int maxCapacity, Duration retentionTime) {
        this(builder()
                .maxCapacity(maxCapacity)
                .retentionTime(retentionTime));
    }

    /**
     * Creates a cache with custom enabled state, capacity and retention settings.
     *
     * @param enabled whether the cache should initially be enabled
     * @param maxCapacity maximum number of entries to keep in memory
     * @param retentionTime duration after which entries expire; {@link Duration#ZERO} disables time-based eviction
     */
    public InMemoryFeatureCache(boolean enabled, int maxCapacity, Duration retentionTime) {
        this(builder()
                .enabled(enabled)
                .maxCapacity(maxCapacity)
                .retentionTime(retentionTime));
    }

    /**
     * Creates a cache with custom enabled state, miss handler, capacity and retention settings.
     *
     * @param enabled whether the cache should initially be enabled
     * @param missHandler function used when a requested feature is missing from the cache
     * @param maxCapacity maximum number of entries to keep in memory
     * @param retentionTime duration after which entries expire; {@link Duration#ZERO} disables time-based eviction
     */
    public InMemoryFeatureCache(
            boolean enabled,
            Function<String, Feature> missHandler,
            int maxCapacity,
            Duration retentionTime
    ) {
        this(builder()
                .enabled(enabled)
                .missHandler(missHandler)
                .maxCapacity(maxCapacity)
                .retentionTime(retentionTime));
    }

    /**
     * Creates a cache with full control over all internal configuration options.
     *
     * <p>
     * This constructor is mainly useful for tests or advanced integration scenarios. In most cases,
     * prefer {@link #builder()}.
     * </p>
     *
     * @param enabled whether the cache should initially be enabled
     * @param missHandler function used when a requested feature is missing from the cache
     * @param writeLock lock used to synchronize compound write operations
     * @param maxCapacity maximum number of entries to keep in memory
     * @param retentionTime duration after which entries expire; {@link Duration#ZERO} disables time-based eviction
     * @param clock clock used for retention and access-time calculations
     */
    public InMemoryFeatureCache(
            boolean enabled,
            Function<String, Feature> missHandler,
            Object writeLock,
            int maxCapacity,
            Duration retentionTime,
            Clock clock
    ) {
        this(builder()
                .enabled(enabled)
                .missHandler(missHandler)
                .writeLock(writeLock)
                .maxCapacity(maxCapacity)
                .retentionTime(retentionTime)
                .clock(clock));
    }

    private InMemoryFeatureCache(Builder builder) {
        if (builder.maxCapacity < 1) {
            throw new IllegalArgumentException("maxCapacity must be greater than zero");
        }

        this.enabled = builder.enabled;
        this.missHandler = Objects.requireNonNull(builder.missHandler, "missHandler must not be null");
        this.writeLock = Objects.requireNonNull(builder.writeLock, "writeLock must not be null");
        this.maxCapacity = builder.maxCapacity;
        this.retentionTime = Objects.requireNonNull(builder.retentionTime, "retentionTime must not be null");
        this.clock = Objects.requireNonNull(builder.clock, "clock must not be null");
    }

    /**
     * Creates a new builder for {@link InMemoryFeatureCache}.
     *
     * @return a builder initialized with the same defaults as {@link #InMemoryFeatureCache()}
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for {@link InMemoryFeatureCache}.
     *
     * <p>
     * Defaults:
     * </p>
     *
     * <ul>
     *     <li>{@code enabled}: {@code true}</li>
     *     <li>{@code missHandler}: returns {@link Feature#empty()}</li>
     *     <li>{@code writeLock}: new {@link Object}</li>
     *     <li>{@code maxCapacity}: {@link Integer#MAX_VALUE}</li>
     *     <li>{@code retentionTime}: {@link Duration#ZERO}</li>
     *     <li>{@code clock}: {@link Clock#systemUTC()}</li>
     * </ul>
     */
    public static final class Builder {

        private boolean enabled = true;
        private Function<String, Feature> missHandler = key -> Feature.empty();
        private Object writeLock = new Object();
        private int maxCapacity = Integer.MAX_VALUE;
        private Duration retentionTime = Duration.ZERO;
        private Clock clock = Clock.systemUTC();

        private Builder() {
        }

        /**
         * Sets whether the cache should initially be enabled.
         *
         * <p>
         * If disabled, all cache operations behave as no-ops and return empty results.
         * </p>
         *
         * @param enabled initial enabled state
         * @return this builder
         */
        public Builder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Sets the function used when a requested feature is missing from the cache.
         *
         * <p>
         * The returned feature is inserted into the cache and the requested lookup key is registered
         * as alias for that feature. The default handler returns {@link Feature#empty()}.
         * </p>
         *
         * @param missHandler function used to resolve cache misses
         * @return this builder
         */
        public Builder missHandler(Function<String, Feature> missHandler) {
            this.missHandler = Objects.requireNonNull(missHandler, "missHandler must not be null");
            return this;
        }

        /**
         * Sets the write lock used for compound write operations.
         *
         * <p>
         * This option is mainly useful for tests or advanced scenarios where multiple cache instances
         * should coordinate writes using the same lock.
         * </p>
         *
         * @param writeLock lock object used for synchronization
         * @return this builder
         */
        public Builder writeLock(Object writeLock) {
            this.writeLock = Objects.requireNonNull(writeLock, "writeLock must not be null");
            return this;
        }

        /**
         * Sets the maximum number of feature entries kept in memory.
         *
         * <p>
         * If the cache grows beyond this number, entries are evicted lazily during cache operations.
         * The least recently accessed entries are removed first.
         * </p>
         *
         * @param maxCapacity maximum number of entries; must be greater than zero
         * @return this builder
         */
        public Builder maxCapacity(int maxCapacity) {
            if (maxCapacity < 1) {
                throw new IllegalArgumentException("maxCapacity must be greater than zero");
            }

            this.maxCapacity = maxCapacity;
            return this;
        }

        /**
         * Sets the retention time for cached entries.
         *
         * <p>
         * An entry expires once this duration has elapsed since it was written to the cache.
         * Expired entries are removed lazily during cache operations.
         * </p>
         *
         * <p>
         * {@link Duration#ZERO} or a negative duration disables time-based eviction.
         * </p>
         *
         * @param retentionTime retention duration; {@link Duration#ZERO} disables time-based eviction
         * @return this builder
         */
        public Builder retentionTime(Duration retentionTime) {
            this.retentionTime = Objects.requireNonNull(retentionTime, "retentionTime must not be null");
            return this;
        }

        /**
         * Sets the clock used for retention and access-time calculations.
         *
         * <p>
         * This is especially useful in tests where time needs to be controlled deterministically.
         * </p>
         *
         * @param clock clock used by the cache
         * @return this builder
         */
        public Builder clock(Clock clock) {
            this.clock = Objects.requireNonNull(clock, "clock must not be null");
            return this;
        }

        /**
         * Builds a new {@link InMemoryFeatureCache} instance.
         *
         * @return configured cache instance
         */
        public InMemoryFeatureCache build() {
            return new InMemoryFeatureCache(this);
        }
    }

    /**
     * Translates the given lookup key to the internal cache key.
     *
     * <p>
     * If an alias exists, the alias target is returned. Otherwise, the original key is returned.
     * </p>
     *
     * @param key lookup key or alias
     * @return internal cache key
     */
    protected String translate(String key) {
        return translations.getOrDefault(key, key);
    }

    /**
     * Registers an alternative lookup key for the given feature.
     *
     * @param alias alternative lookup key
     * @param feature feature the alias should resolve to
     */
    @Override
    public void setAlias(String alias, Feature feature) {
        if (!enabled) {
            return;
        }
        translations.put(alias, feature.id());
    }

    /**
     * Returns the feature for the given key.
     *
     * <p>
     * The key may either be the feature id itself or an alias registered through
     * {@link #setAlias(String, Feature)}. Expired entries are removed lazily before lookup.
     * </p>
     *
     * <p>
     * If the feature is missing, the configured miss handler is invoked. Its result is inserted into
     * the cache and the requested key is registered as an alias for that result.
     * </p>
     *
     * @param key feature id or alias
     * @return cached feature, miss-handler result, or {@code null} if the cache is disabled or the resolved feature is empty
     */
    @Override
    public Feature get(String key) {
        if (!enabled) {
            return null;
        }

        evictExpiredEntries();

        String translatedKey = translate(key);
        CacheEntry result = content.get(translatedKey);

        if (result != null) {
            if (isExpired(result)) {
                Feature removed = content.remove(translatedKey) != null ? result.feature() : null;
                if (removed != null) {
                    notifySubscribersAboutRemove(removed);
                }
                return null;
            }

            content.computeIfPresent(translatedKey, (ignored, entry) -> entry.touch(clock.instant()));
            return result.feature().nullIfEmpty();
        }

        Feature match = missHandler.apply(key);
        set(match);
        translations.put(key, match.id());

        return match.nullIfEmpty();
    }

    /**
     * Removes a feature from the cache.
     *
     * <p>
     * The key may either be the feature id or an alias. Subscribers are notified through
     * {@link FeatureCache.Callback#onRemove(Feature)} if an entry was removed.
     * </p>
     *
     * @param key feature id or alias
     * @return removed feature or {@code null} if no entry existed or the cache is disabled
     */
    @Override
    public Feature invalidate(String key) {
        if (!enabled) {
            return null;
        }

        CacheEntry removed = content.remove(translate(key));
        if (removed != null) {
            notifySubscribersAboutRemove(removed.feature());
            return removed.feature();
        }

        return null;
    }

    /**
     * Removes all features from the cache.
     *
     * <p>
     * Subscribers are notified for every removed feature.
     * </p>
     */
    @Override
    public void invalidate() {
        if (enabled) {
            new ArrayList<>(content.keySet()).forEach(this::invalidate);
        }
    }

    /**
     * Enables or disables this cache.
     *
     * <p>
     * If disabled, cache operations are treated as no-ops.
     * </p>
     *
     * @param state new enabled state
     */
    @Override
    public void enable(boolean state) {
        this.enabled = state;
    }

    /**
     * Updates the cache if the incoming feature is newer than the existing cached feature.
     *
     * @param feature incoming feature
     * @return previous and updated feature information
     */
    protected UpdateResult updateIfNewer(Feature feature) {
        if (!enabled) {
            return new UpdateResult(null, null);
        }

        final Feature[] previous = new Feature[1];
        final Feature[] updated = new Feature[1];
        Instant now = clock.instant();

        content.compute(feature.id(), (key, existing) -> {
            if (existing == null || isExpired(existing) || feature.timestamp().isAfter(existing.feature().timestamp())) {
                previous[0] = existing == null ? null : existing.feature();
                updated[0] = feature;
                return new CacheEntry(feature, now, now);
            }

            updated[0] = null;
            return existing.touch(now);
        });

        return new UpdateResult(previous[0], updated[0]);
    }

    /**
     * Stores a feature in the cache if it is newer than the currently cached value.
     *
     * <p>
     * Expired entries are evicted lazily before the write. Capacity constraints are evaluated after
     * the write. Subscribers are notified if the feature was updated.
     * </p>
     *
     * @param feature feature to store
     * @return previous cached feature or {@code null}
     */
    @Override
    @Nullable
    public Feature set(Feature feature) {
        if (!enabled) {
            return null;
        }

        synchronized (writeLock) {
            evictExpiredEntries();

            UpdateResult result = updateIfNewer(feature);

            if (result.updatedFeature() != null) {
                notifySubscribersAboutNext(result.updatedFeature());
            }

            evictOverflowEntries().forEach(this::notifySubscribersAboutRemove);

            return result.previousFeature();
        }
    }

    /**
     * Stores multiple features in the cache.
     *
     * <p>
     * Existing entries that are not part of the given list are removed, matching the original
     * replace-all behavior. Expired entries are evicted lazily before applying the new list.
     * Capacity constraints are evaluated after all updates have been applied.
     * </p>
     *
     * @param features features to store
     */
    @Override
    public void setAll(List<Feature> features) {
        if (!enabled) {
            return;
        }

        List<String> newKeys = features.stream()
                .map(Feature::id)
                .toList();

        List<Feature> updatedForNotification = new ArrayList<>();
        List<Feature> removedForNotification = new ArrayList<>();

        synchronized (writeLock) {
            evictExpiredEntries();

            for (Feature incoming : features) {
                UpdateResult result = updateIfNewer(incoming);
                if (result.updatedFeature() != null) {
                    updatedForNotification.add(result.updatedFeature());
                }
            }

            content.forEach((key, entry) -> {
                if (!newKeys.contains(key)) {
                    CacheEntry removed = content.remove(key);
                    if (removed != null) {
                        removedForNotification.add(removed.feature());
                    }
                }
            });

            removedForNotification.addAll(evictOverflowEntries());
        }

        updatedForNotification.forEach(this::notifySubscribersAboutNext);
        removedForNotification.forEach(this::notifySubscribersAboutRemove);
    }

    /**
     * Returns all non-empty features currently stored in the cache.
     *
     * <p>
     * Expired entries are removed lazily before the result is created.
     * </p>
     *
     * @return immutable list of cached non-empty features, or an empty list if the cache is disabled
     */
    @Override
    public List<Feature> getAll() {
        if (!enabled) {
            return Collections.emptyList();
        }

        evictExpiredEntries();

        return Collections.unmodifiableList(
                content.values()
                        .stream()
                        .map(CacheEntry::feature)
                        .filter(feature -> !feature.isEmpty())
                        .toList()
        );
    }

    /**
     * Removes entries whose retention time has elapsed.
     *
     * <p>
     * This method is intentionally called from public cache operations instead of a background
     * cleanup task. This is what makes eviction lazy.
     * </p>
     */
    protected void evictExpiredEntries() {
        if (retentionTime.isZero() || retentionTime.isNegative()) {
            return;
        }

        Instant now = clock.instant();
        List<Feature> removedFeatures = new ArrayList<>();

        synchronized (writeLock) {
            content.forEach((key, entry) -> {
                if (isExpired(entry, now)) {
                    CacheEntry removed = content.remove(key);
                    if (removed != null) {
                        removedFeatures.add(removed.feature());
                    }
                }
            });
        }

        removedFeatures.forEach(this::notifySubscribersAboutRemove);
    }

    /**
     * Removes least-recently-accessed entries until the configured maximum capacity is satisfied.
     *
     * @return removed features
     */
    protected List<Feature> evictOverflowEntries() {
        if (content.size() <= maxCapacity) {
            return Collections.emptyList();
        }

        List<Feature> removedFeatures = new ArrayList<>();

        List<String> keysToRemove = content.entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> entry.getValue().lastAccessedAt()))
                .limit((long) content.size() - maxCapacity)
                .map(Map.Entry::getKey)
                .toList();

        for (String key : keysToRemove) {
            CacheEntry removed = content.remove(key);
            if (removed != null) {
                removedFeatures.add(removed.feature());
            }
        }

        return removedFeatures;
    }

    /**
     * Checks whether an entry is expired at the current time.
     *
     * @param entry cache entry
     * @return {@code true} if the entry is expired
     */
    protected boolean isExpired(CacheEntry entry) {
        return isExpired(entry, clock.instant());
    }

    /**
     * Checks whether an entry is expired at the given time.
     *
     * @param entry cache entry
     * @param now current time
     * @return {@code true} if the entry is expired
     */
    protected boolean isExpired(CacheEntry entry, Instant now) {
        if (retentionTime.isZero() || retentionTime.isNegative()) {
            return false;
        }

        return entry.createdAt().plus(retentionTime).isBefore(now);
    }

    /**
     * Notifies subscribers that a feature was updated.
     *
     * @param feature updated feature
     */
    protected void notifySubscribersAboutNext(Feature feature) {
        if (!enabled) {
            return;
        }

        List<FeatureCache.Callback> callbacks = subscriptions.get(feature.name());
        if (callbacks != null) {
            callbacks.forEach(callback -> callback.onNext(feature));
        }
    }

    /**
     * Notifies subscribers that a feature was removed.
     *
     * @param feature removed feature
     */
    protected void notifySubscribersAboutRemove(Feature feature) {
        if (!enabled) {
            return;
        }

        List<FeatureCache.Callback> callbacks = subscriptions.get(feature.name());
        if (callbacks != null) {
            callbacks.forEach(callback -> callback.onRemove(feature));
        }
    }

    /**
     * Subscribes to updates for the given key.
     *
     * @param key feature key
     * @param callback callback invoked for updates and removals
     * @return subscription that can be used to cancel the callback registration
     */
    @Override
    public Subscription subscribe(String key, FeatureCache.Callback callback) {
        subscriptions.computeIfAbsent(key, ignored -> new ArrayList<>()).add(callback);
        return new SubscriptionImpl(callback, key);
    }

    private class SubscriptionImpl implements Subscription {

        private final FeatureCache.Callback callback;
        private final String key;

        private SubscriptionImpl(FeatureCache.Callback callback, String key) {
            this.callback = callback;
            this.key = key;
        }

        @Override
        public void cancel() {
            subscriptions.computeIfPresent(key, (ignored, list) -> {
                list.remove(callback);
                return list.isEmpty() ? null : list;
            });
        }
    }

    /**
     * Internal cache entry containing the stored feature and cache metadata.
     *
     * @param feature cached feature
     * @param createdAt time at which the entry was written
     * @param lastAccessedAt time at which the entry was last accessed
     */
    protected record CacheEntry(
            Feature feature,
            Instant createdAt,
            Instant lastAccessedAt
    ) {

        /**
         * Creates a copy of this entry with updated access time.
         *
         * @param lastAccessedAt new last access time
         * @return updated cache entry
         */
        private CacheEntry touch(Instant lastAccessedAt) {
            return new CacheEntry(feature, createdAt, lastAccessedAt);
        }
    }

    /**
     * Result of a conditional cache update.
     *
     * @param previousFeature previous cached feature, if any
     * @param updatedFeature new cached feature if the cache was updated, otherwise {@code null}
     */
    protected record UpdateResult(
            Feature previousFeature,
            Feature updatedFeature
    ) {
    }
}