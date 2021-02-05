package com.staircase13.apperta.cms;

import com.staircase13.apperta.cms.loader.NhsContentKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NhsApiContentFactory {

    private final String linkPrefix;

    public NhsApiContentFactory(@Value("${apperta.cms.link.prefix}") String linkPrefix) {
        this.linkPrefix = linkPrefix;
    }

    public NhsApiContent forKeyAndContent(NhsContentKey key,
                                          String rawJson) throws CannotParseApiContentException {
        return new NhsApiContent(key,rawJson,linkPrefix);
    }
}
