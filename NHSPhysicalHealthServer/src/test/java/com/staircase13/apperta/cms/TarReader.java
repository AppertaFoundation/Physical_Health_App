package com.staircase13.apperta.cms;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TarReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(TarReader.class);

    public static Map<String,String> extractFilesFromTarGz(Path tarGz) throws IOException {
        Map<String,String> fileContents = new HashMap<>();

        LOGGER.debug("Reading " + tarGz);

        try (InputStream fileInputStream = Files.newInputStream(tarGz);
             GzipCompressorInputStream decompressStream = new GzipCompressorInputStream(fileInputStream);
             TarArchiveInputStream tarIn = new TarArchiveInputStream(decompressStream)) {
            byte[] b = new byte[1024];

            TarArchiveEntry tarEntry;
            while ((tarEntry = tarIn.getNextTarEntry()) != null) {

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                int r;
                while ((r = tarIn.read(b)) != -1) {
                    byteArrayOutputStream.write(b, 0, r);
                }

                String fileName = tarEntry.getName();
                String content = new String(byteArrayOutputStream.toByteArray());

                LOGGER.debug("File '{}' content is '{}'",fileName,content);

                fileContents.put(fileName, content);
            }
        }

        return fileContents;
    }
}
