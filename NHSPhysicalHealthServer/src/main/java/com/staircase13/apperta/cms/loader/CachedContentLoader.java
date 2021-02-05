package com.staircase13.apperta.cms.loader;

import com.staircase13.apperta.cms.CannotParseApiContentException;
import com.staircase13.apperta.cms.NhsApiContent;
import com.staircase13.apperta.cms.NhsApiContentFactory;
import com.staircase13.apperta.cms.entities.CmsCache;
import com.staircase13.apperta.cms.repository.CmsCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static com.staircase13.apperta.cms.loader.CachedContentLoader.State.BeforeRefreshThreshold;

@Service
class CachedContentLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CachedContentLoader.class);

    private final CmsCacheRepository cmsCacheRepository;
    private final Duration refreshDuration;
    private final Duration retentionDuration;
    private final Clock clock;
    private final NhsApiContentFactory nhsApiContentFactory;

    @Autowired
    public CachedContentLoader(CmsCacheRepository cmsCacheRepository,
                               @Value("${apperta.cms.cache.duration.until.refresh}") Duration refreshDuration,
                               @Value("${apperta.cms.cache.duration.until.expiry}") Duration retentionDuration,
                               Clock clock,
                               NhsApiContentFactory nhsApiContentFactory) {
        this.cmsCacheRepository = cmsCacheRepository;
        this.refreshDuration = refreshDuration;
        this.retentionDuration = retentionDuration;
        this.clock = clock;
        this.nhsApiContentFactory = nhsApiContentFactory;
    }

    public enum State {
        BeforeRefreshThreshold,
        BeforeRetentionThreshold
    }

    public Optional<NhsApiContent> getApiContent(NhsContentKey key, State acceptableState) throws CannotParseApiContentException {

        Optional<CmsCache> cacheOptional = cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(key.getNhsApi(),key.getPrimaryEntityName(),key.getSecondaryEntityName());

        if (!cacheOptional.isPresent()) {
            LOGGER.info("No entry in cache for '{}'",key);
            return Optional.empty();
        }

        CmsCache cache = cacheOptional.get();

        LocalDateTime now = LocalDateTime.now(clock);

        LOGGER.debug("Current date time is '{}'",now);

        LocalDateTime cacheRetentionThreshold = now.minus(BeforeRefreshThreshold == acceptableState ? refreshDuration : retentionDuration);

        LOGGER.debug("Will use cache entry if it was loaded after '{}'",cacheRetentionThreshold);

        if (cacheRetentionThreshold.isAfter(cache.getLoaded())) {
            LOGGER.debug("Cache loaded datetime '{}' is before '{}', cannot use",cache.getLoaded(),cacheRetentionThreshold);
            return Optional.empty();
        }

        return Optional.of(nhsApiContentFactory.forKeyAndContent(key, cache.getContent()));
    }

    public void updateCache(NhsApiContent nhsApiContent) {
        NhsContentKey contentKey = nhsApiContent.getKey();

        LOGGER.info("Update cache with content for '{}'",contentKey);

        Optional<CmsCache> existingCacheEntryOptional = cmsCacheRepository.getByApiAndPrimaryEntityNameAndSecondaryEntityName(
                contentKey.getNhsApi(),
                contentKey.getPrimaryEntityName(),
                contentKey.getSecondaryEntityName()
        );

        if(existingCacheEntryOptional.isPresent()) {

            LOGGER.debug("Updating existing entry");

            CmsCache existingCacheEntry = existingCacheEntryOptional.get();
            existingCacheEntry.setLoaded(LocalDateTime.now(clock));
            existingCacheEntry.setContent(nhsApiContent.getRawJson());

            cmsCacheRepository.save(existingCacheEntry);
        } else {

            LOGGER.debug("Adding new entry");

            CmsCache newCacheEntry = CmsCache.builder()
                    .api(nhsApiContent.getKey().getNhsApi())
                    .content(nhsApiContent.getRawJson())
                    .loaded(LocalDateTime.now(clock))
                    .primaryEntityName(nhsApiContent.getKey().getPrimaryEntityName())
                    .secondaryEntityName(nhsApiContent.getKey().getSecondaryEntityName())
                    .build();

            cmsCacheRepository.save(newCacheEntry);
        }
    }
}
