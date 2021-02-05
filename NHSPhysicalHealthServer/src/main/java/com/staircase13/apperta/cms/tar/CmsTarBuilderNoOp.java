package com.staircase13.apperta.cms.tar;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Used when we want to review the load log but not actually build the TAR
 */
public class CmsTarBuilderNoOp implements CmsTarBuilder {

    public void addPage(String name, String content) {

    }

    public Optional<Path> commit(LocalDateTime modified) {
       return Optional.empty();
    }

    public void rollback() {

    }
}
