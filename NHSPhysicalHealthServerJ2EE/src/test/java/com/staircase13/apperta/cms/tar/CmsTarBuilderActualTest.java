package com.staircase13.apperta.cms.tar;

import com.staircase13.apperta.cms.TarReader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;

public class CmsTarBuilderActualTest {
    private CmsTarBuilder cmsTarBuilder;
    private Path targetFile;

    @Before
    public void setupBuilder() throws IOException {
        Path workingDir = Files.createTempDirectory("cms");
        targetFile = workingDir.resolve("cms.tar.gz");
        cmsTarBuilder = new CmsTarBuilderActual(targetFile);
    }

    @Test
    public void single_page_tar_content_correct() throws Exception {
        cmsTarBuilder.addPage("my-page.html","This is the complete content");
        cmsTarBuilder.commit(LocalDateTime.now());

        assertThat(Files.exists(targetFile), is(true));

        Map<String,String> files = TarReader.extractFilesFromTarGz(targetFile);
        assertThat(files.size(), is(1));
        assertThat(files, hasEntry("my-page.html","This is the complete content"));
    }

    @Test
    public void multi_page_tar_content_correct() throws Exception {
        cmsTarBuilder.addPage("my-page-1.html","This is the complete content for page 1");
        cmsTarBuilder.addPage("my-page-2.html","This is the complete content for page 2");
        cmsTarBuilder.addPage("my-page-3.html","This is the complete content for page 3");
        cmsTarBuilder.addPage("my-page-4.html","This is the complete content for page 4");
        cmsTarBuilder.addPage("my-page-5.html","This is the complete content for page 5");
        cmsTarBuilder.addPage("my-page-6.html","This is the complete content for page 6");
        cmsTarBuilder.commit(LocalDateTime.now());

        assertThat(Files.exists(targetFile), is(true));

        Map<String,String> files = TarReader.extractFilesFromTarGz(targetFile);
        assertThat(files.size(), is(6));
        assertThat(files, hasEntry("my-page-1.html","This is the complete content for page 1"));
        assertThat(files, hasEntry("my-page-2.html","This is the complete content for page 2"));
        assertThat(files, hasEntry("my-page-3.html","This is the complete content for page 3"));
        assertThat(files, hasEntry("my-page-4.html","This is the complete content for page 4"));
        assertThat(files, hasEntry("my-page-5.html","This is the complete content for page 5"));
        assertThat(files, hasEntry("my-page-6.html","This is the complete content for page 6"));
    }

    @Test
    public void single_page_tar_modified_set() throws IOException {
        LocalDateTime modifiedDate = LocalDateTime.now().minusDays(1);

        cmsTarBuilder.addPage("my-page.html","This is the complete content");
        cmsTarBuilder.commit(modifiedDate);

        FileTime modifiedTime = Files.getLastModifiedTime(targetFile);

        assertThat(LocalDateTime.ofInstant(modifiedTime.toInstant().truncatedTo(ChronoUnit.SECONDS),
                ZoneId.systemDefault()),is(modifiedDate.truncatedTo(ChronoUnit.SECONDS)));
    }

    @Test
    public void working_directory_removed_on_commit() throws IOException {
        cmsTarBuilder.addPage("my-page.html","This is the complete content");
        cmsTarBuilder.commit(LocalDateTime.now());

        long fileCount = getFileCount(targetFile.getParent());
        assertThat("There should only be one file in the target folder after commit",
                fileCount, is(1L));
    }

    @Test
    public void existing_tar_overwritten_on_commit()  throws IOException {
        Files.createFile(targetFile);
        assertThat(Files.size(targetFile),is(0L));

        cmsTarBuilder.addPage("my-page.html","This is the complete content");
        cmsTarBuilder.commit(LocalDateTime.now());

        assertThat(Files.size(targetFile), not(0L));

        Map<String,String> files = TarReader.extractFilesFromTarGz(targetFile);
        assertThat(files.size(), is(1));
        assertThat(files, hasEntry("my-page.html","This is the complete content"));
    }

    @Test
    public void rollback_leaves_existing_tar_unaffected() throws IOException {
        Files.createFile(targetFile);
        assertThat(Files.size(targetFile),is(0L));

        cmsTarBuilder.addPage("test","ing");
        cmsTarBuilder.rollback();

        assertThat(Files.size(targetFile), is(0L));

    }

    @Test
    public void rollback_removes_working_directory() throws IOException {
        Files.createFile(targetFile);
        assertThat(Files.size(targetFile),is(0L));

        cmsTarBuilder.addPage("test","ing");
        cmsTarBuilder.rollback();

        long fileCount = getFileCount(targetFile.getParent());
        assertThat("There should be one file in the target folder after commit",
                fileCount, is(1L));
    }

    private long getFileCount(Path path) throws IOException {
        try(Stream<Path> paths = Files.list(path)) {
            return paths.count();
        }
    }

}
