package com.staircase13.apperta.cms.repository;

import com.staircase13.apperta.cms.entities.CmsCache;
import com.staircase13.apperta.cms.entities.NhsApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CmsCacheRepository extends JpaRepository<CmsCache, Long> {
    Optional<CmsCache> getByApiAndPrimaryEntityNameAndSecondaryEntityName(NhsApi api, String primaryEntityName, String secondaryEntityName);
}
