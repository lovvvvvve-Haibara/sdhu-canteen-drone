package com.example.sdhucanteendrone.service;

import com.example.sdhucanteendrone.dto.DroneDto;
import com.example.sdhucanteendrone.entity.enums.DroneStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DroneService {

    Page<DroneDto.DroneSummary> listDrones(DroneStatus status, Pageable pageable);

    DroneDto.DroneDetail getDroneDetail(Long droneId);

    DroneDto.DroneDetail createDrone(DroneDto.DroneCreateReq req);

    DroneDto.DroneDetail updateDrone(Long droneId, DroneDto.DroneUpdateReq req);

    void changeDroneStatus(Long droneId, DroneStatus status);

    void deleteDrone(Long droneId);

}
