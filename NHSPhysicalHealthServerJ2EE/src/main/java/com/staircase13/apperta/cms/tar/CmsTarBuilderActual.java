package com.staircase13.apperta.cms.tar;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class CmsTarBuilderActual implements CmsTarBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmsTarBuilderActual.class);

    private final Path targetFile;
    private final Path workingDir;
    private final List<Path> pages;

    public CmsTarBuilderActual(Path targetFile) throws IOException {
        this.targetFile = targetFile;

        pages = new ArrayList<>();

        workingDir = targetFile.getParent().resolve("working");
        if(!Files.exists(workingDir)) {
            Files.createDirectory(workingDir);
        }
        LOGGER.info("Working directory is '{}'",workingDir);
    }

    public void addPage(String name, String content) throws IOException {
        Path page = workingDir.resolve(name);
        Files.write(page, content.getBytes());
        LOGGER.info("Have created file '{}' for page '{}' of size '{}'", page, name, Files.size(page));
        pages.add(page);
    }

    public Optional<Path> commit(LocalDateTime modified) throws IOException {
        Path tarGzip = workingDir.resolve(targetFile.getFileName());

        LOGGER.info("Creating TAR in temporary file '{}'", tarGzip);

        try (OutputStream fileOutputStream = Files.newOutputStream(tarGzip);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
             GzipCompressorOutputStream gzipOutputStream = new GzipCompressorOutputStream(bufferedOutputStream);
             TarArchiveOutputStream tarArchiveOutputStream = new TarArchiveOutputStream(gzipOutputStream)) {

            for (Path page : pages) {
                addPageToTar(tarArchiveOutputStream, page);
            }

        }

        LOGGER.info("Moving TAR from temporary location '{}' to '{}' with last modified '{}'", tarGzip,targetFile, modified);
        Files.move(tarGzip, targetFile, REPLACE_EXISTING);
        Files.setLastModifiedTime(targetFile, FileTime.from(modified.atZone(ZoneId.systemDefault()).toInstant()));

        LOGGER.debug("Removing working directory '{}'",workingDir);
        FileUtils.deleteDirectory(workingDir.toFile());

        return Optional.of(targetFile);
    }

    private void addPageToTar(TarArchiveOutputStream tarArchiveOutputStream, Path page) throws IOException {
        LOGGER.info("Adding page file '{}'", page.getFileName());

        TarArchiveEntry tarEntry = new TarArchiveEntry(page.toFile(), page.getFileName().toString());
        tarEntry.setSize(Files.size(page));

        tarArchiveOutputStream.putArchiveEntry(tarEntry);
        try (InputStream pageFile = Files.newInputStream(page)) {
            IOUtils.copy(pageFile, tarArchiveOutputStream);
        }
        tarArchiveOutputStream.closeArchiveEntry();
    }

    public void rollback() throws IOException {
        LOGGER.info("Rolling back TAR creation. Removing working directory '{}'",workingDir);
        FileUtils.deleteDirectory(workingDir.toFile());
    }
}
