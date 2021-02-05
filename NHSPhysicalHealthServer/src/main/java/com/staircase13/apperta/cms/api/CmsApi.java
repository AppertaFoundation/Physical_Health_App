package com.staircase13.apperta.cms.api;

import com.staircase13.apperta.api.errors.ApiAuthError;
import com.staircase13.apperta.api.errors.ErrorConstants;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/cms")
public class CmsApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsApi.class);

    private final Path targetFile;

    public CmsApi(@Value("${apperta.cms.tar.local.file}") Path targetFile) {
        this.targetFile = targetFile;
    }

    @ApiOperation(value = "downloadCmsTar",notes = "Download a TAR of CMS Content. Provides and observes last-modified/etag headers to avoid unnecessary downloads. Requires a Client Credentials Token")
    @ApiResponses({
            @ApiResponse(code = 503, message = "If a CMS TAR isn't currently available. Try again after a delay"),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('ROLE_TRUSTED_CLIENT')")
    @RequestMapping(value = "/tar", method = RequestMethod.GET)
    public void downloadCmsTar(WebRequest webRequest, HttpServletResponse response) throws IOException {
        if(!Files.exists(targetFile)) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            return;
        }

        long tarLastModified = getTarLastModifiedEpochMillis();

        if(webRequest.checkNotModified(Long.toString(tarLastModified),tarLastModified)) {
            LOGGER.debug("TAR has not been modified since last retrieved, will not return");
            return;
        }

        downloadTar(targetFile, response);
    }

    public static void downloadTar(Path tar, HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType("application/gzip");
        httpServletResponse.setHeader("Content-Disposition", String.format("inline; filename=\"cms.tar.gz\""));

        httpServletResponse.setContentLength((int)Files.size(tar));

        InputStream inputStream = new BufferedInputStream(Files.newInputStream(tar));

        IOUtils.copy(inputStream, httpServletResponse.getOutputStream());
    }

    @ApiOperation(value = "cmsTarLastModified",notes = "Provides the Last Modified Date/Time of the TAR as an Epoch Milli Timestamp. Requires a Client Credentials Token")
    @ApiResponses({
            @ApiResponse(code = 503, message = "If a CMS TAR isn't currently available. Try again after a delay"),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('ROLE_TRUSTED_CLIENT')")
    @RequestMapping(value = "/tar/lastModified", method = RequestMethod.GET)
    public Long getLastModified(HttpServletResponse response) throws IOException {
        if(!Files.exists(targetFile)) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            return null;
        }

        return getTarLastModifiedEpochMillis();
    }

    private Long getTarLastModifiedEpochMillis() throws IOException {
        return Files.getLastModifiedTime(targetFile).toMillis();
    }

}