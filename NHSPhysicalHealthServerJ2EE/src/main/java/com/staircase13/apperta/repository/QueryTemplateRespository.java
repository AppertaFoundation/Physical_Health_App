package com.staircase13.apperta.repository;

import com.staircase13.apperta.entities.DeviceApp;
import com.staircase13.apperta.entities.QueryTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryTemplateRespository extends JpaRepository<QueryTemplate, Long> {

    List<QueryTemplate> findByApp(DeviceApp app);

    void deleteByApp(DeviceApp app);

    List<QueryTemplate> findByName(String name);
}
