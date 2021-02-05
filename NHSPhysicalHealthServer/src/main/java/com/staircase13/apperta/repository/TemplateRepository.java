package com.staircase13.apperta.repository;

import com.staircase13.apperta.entities.DeviceApp;
import com.staircase13.apperta.entities.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    List<Template> findByApp(DeviceApp app);

    void deleteByApp(DeviceApp app);

    @Query("SELECT DISTINCT t.template FROM Template t")
    List<String> findDistinctTemplates();

}
