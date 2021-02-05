package com.staircase13.apperta.cms.loader;

import com.staircase13.apperta.cms.CannotParseApiContentException;
import com.staircase13.apperta.cms.LoadLog;
import com.staircase13.apperta.cms.NhsApiContent;
import com.staircase13.apperta.cms.config.CmsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.staircase13.apperta.cms.entities.NhsApi.CONDITIONS;
import static com.staircase13.apperta.cms.loader.CachedContentLoader.State.BeforeRefreshThreshold;
import static com.staircase13.apperta.cms.loader.CachedContentLoader.State.BeforeRetentionThreshold;

@Service
public class CmsContentLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsContentLoader.class);

    private final CachedContentLoader cachedContentLoader;

    private final ApiLoader conditionsApiLoader;

    private final ApiLoader liveWellApiLoader;

    @Autowired
    public CmsContentLoader(CachedContentLoader cachedContentLoader,
                            @Qualifier(CmsConfiguration.API_LOADER_CONDITIONS)
                            ApiLoader conditionsApiLoader,
                            @Qualifier(CmsConfiguration.API_LOADER_LIVEWELL)
                            ApiLoader liveWellApiLoader) {
        this.cachedContentLoader = cachedContentLoader;
        this.conditionsApiLoader = conditionsApiLoader;
        this.liveWellApiLoader = liveWellApiLoader;
    }

    public Optional<NhsApiContent> getApiContent(NhsContentKey key, LoadLog loadLog) {

        LOGGER.info("Get API Content for key '{}'",key);

        Optional<NhsApiContent> cachedContent = loadFromCacheBeforeRefreshThreshold(key, loadLog);
        if(cachedContent.isPresent()) {
            return cachedContent;
        }

        Optional<NhsApiContent> apiContent = loadFromApi(key, loadLog);
        if (apiContent.isPresent()) {
            cachedContentLoader.updateCache(apiContent.get());
            loadLog.addInfoLog("Loaded '%s' from the API and added to the cache",key);
            return apiContent;
        }

        loadLog.addErrorLog("Loading '%s' from the API failed. Will check cache to see if content is available and hasn't hit retention threshold",key);

        return loadFromCacheBeforeRetentionThreshold(key, loadLog);
    }

    private Optional<NhsApiContent> loadFromCacheBeforeRefreshThreshold(NhsContentKey key, LoadLog loadLog) {

        try {
            Optional<NhsApiContent> cachedContent = cachedContentLoader.getApiContent(key, BeforeRefreshThreshold);

            if(cachedContent.isPresent()) {
                loadLog.addInfoLog("Loaded '%s' from cache",key);
                return Optional.of(cachedContent.get());
            } else {
                loadLog.addInfoLog("Cached content for '%s' doesn't exist or has breached refresh threshold. Will refresh",key);
            }

        } catch (CannotParseApiContentException e) {
            loadLog.addErrorLog("Cached content for '%s' is invalid/couldn't be parsed. Will refresh",key);
        }
        return Optional.empty();

    }

    private Optional<NhsApiContent> loadFromApi(NhsContentKey key, LoadLog loadLog) {

        try {
            Optional<NhsApiContent> content =  getApiLoader(key).getApiContent(key);

            if(!content.isPresent()) {
                loadLog.addErrorLog("Content with key '%s' couldn't be retrieved from the API due to 404. Is it correct?",key);
            }

            return content;
        } catch (CannotParseApiContentException e) {
            loadLog.addErrorLog("Content retrieved from API for '%s' is invalid/couldn't be parsed",key);
        } catch(NhsContentRetrievalException e) {
            loadLog.addErrorLog("Content retrieval from API for '%s' failed with message '%s'",key,e.getMessage());
        }
        return Optional.empty();

    }

    private ApiLoader getApiLoader(NhsContentKey key) {
        return CONDITIONS == key.getNhsApi() ? conditionsApiLoader : liveWellApiLoader;
    }

    private Optional<NhsApiContent> loadFromCacheBeforeRetentionThreshold(NhsContentKey key, LoadLog loadLog) {

        try {
            Optional<NhsApiContent> cachedContent = cachedContentLoader.getApiContent(key, BeforeRetentionThreshold);

            if(cachedContent.isPresent()) {
                loadLog.addInfoLog("Loaded '%s' from cache, despite it needing a refresh",key);
                return Optional.of(cachedContent.get());
            } else {
                loadLog.addErrorLog("Cached content for '%s' not available or has breached retention threshold",key);
            }
        } catch (CannotParseApiContentException e) {
            loadLog.addErrorLog("Cached content for '%s' is invalid/couldn't be parsed",key);
        }
        return Optional.empty();

    }

}
