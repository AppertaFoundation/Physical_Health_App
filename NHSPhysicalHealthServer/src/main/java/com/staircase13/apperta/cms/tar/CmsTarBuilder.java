package com.staircase13.apperta.cms.tar;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

public interface CmsTarBuilder {
    void addPage(String name, String content) throws IOException;
    Optional<Path> commit(LocalDateTime modified) throws IOException;
    void rollback() throws IOException;
}
