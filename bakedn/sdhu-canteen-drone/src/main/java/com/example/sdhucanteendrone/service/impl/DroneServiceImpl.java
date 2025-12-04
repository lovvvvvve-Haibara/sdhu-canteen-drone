package com.example.sdhucanteendrone.service.impl;

import com.example.sdhucanteendrone.Common.BizException;
import com.example.sdhucanteendrone.dto.DroneDto;
import com.example.sdhucanteendrone.entity.Drone;
import com.example.sdhucanteendrone.entity.enums.DroneStatus;
import com.example.sdhucanteendrone.repository.DroneRepository;
import com.example.sdhucanteendrone.service.DroneService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DroneServiceImpl implements DroneService {

    private final DroneRepository droneRepository;

    // ============================================================
    // 查询
    // ============================================================

    @Override
    public Page<DroneDto.DroneSummary> listDrones(DroneStatus status, Pageable pageable) {
        Page<Drone> page;
        if (status != null) {
            page = droneRepository.findByStatus(status, pageable);
        } else {
            page = droneRepository.findAll(pageable);
        }
        return page.map(this::toSummary);
    }

    @Override
    public DroneDto.DroneDetail getDroneDetail(Long droneId) {
        Drone d = droneRepository.findById(droneId)
                .orElseThrow(() -> BizException.notFound("无人机不存在"));
        return toDetail(d);
    }

    // ============================================================
    // 创建 / 修改
    // ============================================================

    @Override
    @Transactional
    public DroneDto.DroneDetail createDrone(DroneDto.DroneCreateReq req) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw BizException.badRequest("无人机编号不能为空");
        }
        if (req.getModel() == null || req.getModel().isBlank()) {
            throw BizException.badRequest("无人机型号不能为空");
        }
        if (req.getMaxPayloadKg() == null) {
            throw BizException.badRequest("最大载重不能为空");
        }
        if (req.getBattery() == null) {
            throw BizException.badRequest("电量不能为空");
        }

        if (droneRepository.existsByCode(req.getCode())) {
            throw BizException.badRequest("无人机编号已存在");
        }

        Drone d = new Drone();
        d.setCode(req.getCode());
        d.setModel(req.getModel());
        d.setMaxPayloadKg(req.getMaxPayloadKg());
        d.setBattery(req.getBattery());
        d.setStatus(req.getStatus() == null ? DroneStatus.IDLE : req.getStatus());
        d.setNote(req.getNote());
        d.setCreatedAt(Instant.now());
        d.setUpdatedAt(Instant.now());
        d.setLocation(req.getLocation());

        return toDetail(droneRepository.save(d));
    }

    @Override
    @Transactional
    public DroneDto.DroneDetail updateDrone(Long droneId, DroneDto.DroneUpdateReq req) {
        Drone d = droneRepository.findById(droneId)
                .orElseThrow(() -> BizException.notFound("无人机不存在"));

        if (req.getCode() != null && !req.getCode().isBlank()
                && !req.getCode().equals(d.getCode())) {
            if (droneRepository.existsByCode(req.getCode())) {
                throw BizException.badRequest("无人机编号已存在");
            }
            d.setCode(req.getCode());
        }

        if (req.getStatus() != null) {
            d.setStatus(req.getStatus());
        }

        d.setUpdatedAt(Instant.now());
        return toDetail(droneRepository.save(d));
    }

    @Override
    @Transactional
    public void changeDroneStatus(Long droneId, DroneStatus status) {
        int updated = droneRepository.updateStatusById(droneId, status);
        if (updated == 0) {
            throw BizException.notFound("无人机不存在");
        }
    }

    // ============================================================
    // DTO 映射
    // ============================================================

    private DroneDto.DroneSummary toSummary(Drone d) {
        DroneDto.DroneSummary dto = new DroneDto.DroneSummary();
        dto.setId(d.getId());
        dto.setCode(d.getCode());
        dto.setStatus(d.getStatus());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        return dto;
    }

    private DroneDto.DroneDetail toDetail(Drone d) {
        DroneDto.DroneDetail dto = new DroneDto.DroneDetail();
        dto.setId(d.getId());
        dto.setCode(d.getCode());
        dto.setStatus(d.getStatus());
        dto.setCreatedAt(d.getCreatedAt());
        dto.setUpdatedAt(d.getUpdatedAt());
        dto.setLocation(d.getLocation());
        dto.setBattery(d.getBattery());
        return dto;
    }

    @Override
    @Transactional
    public void deleteDrone(Long droneId) {
        if (!droneRepository.existsById(droneId)) {
            throw BizException.notFound("无人机不存在");
        }

        // 这里可以做更多校验，例如：
        // 不能删除正在执行任务的无人机
        Drone drone = droneRepository.findById(droneId).get();
        if (drone.getStatus() == DroneStatus.IN_MISSION) {
            throw BizException.badRequest("当前无人机正在执行任务，无法删除");
        }

        droneRepository.deleteById(droneId);
    }

}
