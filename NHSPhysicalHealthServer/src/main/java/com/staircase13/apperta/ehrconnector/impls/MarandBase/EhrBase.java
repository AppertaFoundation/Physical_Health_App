package com.staircase13.apperta.ehrconnector.impls.MarandBase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class EhrBase {

    private static final Logger LOG = LoggerFactory.getLogger(EhrBase.class);

    /**
     * Utility method to get the final segment of the path, which contains the id
     * @param response
     * @return
     */
    protected long getIdFromHref(EhrBaseResponse response) {
        long resultId = -1;
        try {
            String href = response.getMeta().getHref();
            if (href != null) {
                URI createdUri = new URI(href);
                String[] segments = createdUri.getPath().split("/");
                if (segments.length > 0) {
                    String idStr = segments[segments.length - 1];
                    resultId = Long.parseLong(idStr);
                }
            }
        } catch (URISyntaxException use) {
            LOG.info("Failed to parse id from ehr url " + response.getMeta().getHref(), use);
        }
        return resultId;
    }

    protected String getStringIdFromHref(EhrBaseResponse response) {
        String idStr = null;
        try {
            String href = response.getMeta().getHref();
            if (href != null) {
                URI createdUri = new URI(href);
                String[] segments = createdUri.getPath().split("/");
                if (segments.length > 0) {
                    idStr = segments[segments.length - 1];
                }
            }
        }catch(URISyntaxException use){
            LOG.info("Failed to parse id from ehr url " + response.getMeta().getHref(), use);
        }
        return idStr;
    }

    protected String getStringIdFromNextHref(EhrBaseResponse response) {
        String idStr = null;
        try {
            String href = response.getMeta().getNextHref();
            if (href != null) {
                URI createdUri = new URI(href);
                String[] segments = createdUri.getPath().split("/");
                if (segments.length > 0) {
                    idStr = segments[segments.length - 1];
                }
            }
        }catch(URISyntaxException use){
            LOG.info("Failed to parse id from ehr url " + response.getMeta().getHref(), use);
        }
        return idStr;
    }
}
