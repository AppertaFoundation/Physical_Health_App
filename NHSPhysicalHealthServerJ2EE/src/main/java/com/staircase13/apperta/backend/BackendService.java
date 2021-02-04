package com.staircase13.apperta.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class BackendService {

    private static final Logger LOG = LoggerFactory.getLogger(BackendService.class);

    private final String backendUrl;

    private final RestTemplate backendRestTemplate;

    @Autowired
    public BackendService(@Value("${apperta.backend.url}") String backendUrl, RestTemplate backendRestTemplate) {
        this.backendUrl = backendUrl;
        this.backendRestTemplate = backendRestTemplate;
    }

    @Cacheable("backendUserInfo")
    public Map<String,String> getUserInfo(String username) {
        LOG.debug("Getting backend user info for user '{}'",username);
        return backendRestTemplate.getForObject(backendUrl + "/user?username={username}", Map.class, username);
    }

}
