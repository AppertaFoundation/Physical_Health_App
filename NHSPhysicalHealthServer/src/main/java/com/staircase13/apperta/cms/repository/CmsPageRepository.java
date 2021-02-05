package com.staircase13.apperta.cms.repository;

import com.staircase13.apperta.cms.entities.CmsPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CmsPageRepository extends JpaRepository<CmsPage, Long> {
}
