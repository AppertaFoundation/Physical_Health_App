package com.staircase13.apperta.repository;

import com.staircase13.apperta.entities.CompositionParameters;
import com.staircase13.apperta.entities.DeviceApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompositionParametersRepository extends JpaRepository<CompositionParameters, Long> {

    List<CompositionParameters> findByApp(DeviceApp app);

    List<CompositionParameters> findByAppAndSetName(DeviceApp app, String setName);

    List<CompositionParameters> findBySetName(String setName);

    void deleteByApp(DeviceApp app);


}
