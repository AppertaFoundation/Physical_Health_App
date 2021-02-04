package com.staircase13.apperta.cms.tar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class CmsTarBuilderFactory {
    private final Path targetFile;

    @Autowired
    public CmsTarBuilderFactory(@Value("${apperta.cms.tar.local.file}") Path targetFile) {
        this.targetFile = targetFile;
    }

    public CmsTarBuilder newTarBuilder() throws IOException {
        return new CmsTarBuilderActual(targetFile);
    }

    public CmsTarBuilder newTemporaryTarBuilder() throws IOException {
        Path tempDir = Files.createTempDirectory("cms-temp-tar");
        Path tempFile = Files.createTempFile(tempDir, "cms", ".tar.gz");
        return new CmsTarBuilderActual(tempFile);
    }

    public CmsTarBuilder noOpTarBuilder() {
        return new CmsTarBuilderNoOp();
    }
}
