package com.example.sdhucanteendrone.controller;

import com.example.sdhucanteendrone.Common.Result;
import com.example.sdhucanteendrone.dto.DroneDto;
import com.example.sdhucanteendrone.entity.enums.DroneStatus;
import com.example.sdhucanteendrone.service.DroneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 无人机管理相关接口
 *
 * - 管理员：新增无人机、更新信息、调整状态
 * - 用于后续派送系统使用
 */
@RestController
@RequestMapping("/api/drones")
@RequiredArgsConstructor
public class DroneController {

    private final DroneService droneService;

    /**
     * 分页查询无人机列表（按状态过滤）
     */
    @GetMapping
    public Result<Page<DroneDto.DroneSummary>> listDrones(
            @RequestParam(required = false) DroneStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<DroneDto.DroneSummary> resultPage =
                droneService.listDrones(status, PageRequest.of(page, size));
        return Result.success(resultPage);
    }

    /**
     * 获取无人机详情
     */
    @GetMapping("/{id}")
    public Result<DroneDto.DroneDetail> getDrone(@PathVariable("id") Long droneId) {
        return Result.success(droneService.getDroneDetail(droneId));
    }

    /**
     * 新增无人机
     */
    @PostMapping
    public Result<DroneDto.DroneDetail> createDrone(
            @Valid @RequestBody DroneDto.DroneCreateReq req) {
        return Result.success(droneService.createDrone(req));
    }

    /**
     * 修改无人机基本信息（型号 / 载重 / 备注等）
     */
    @PatchMapping("/{id}")
    public Result<DroneDto.DroneDetail> updateDrone(
            @PathVariable("id") Long droneId,
            @Valid @RequestBody DroneDto.DroneUpdateReq req) {
        return Result.success(droneService.updateDrone(droneId, req));
    }

    /**
     * 修改无人机状态（IDLE / IN_MISSION / MAINTENANCE）
     */
    @PatchMapping("/{id}/status")
    public Result<Void> changeDroneStatus(
            @PathVariable("id") Long droneId,
            @RequestParam("status") DroneStatus status) {
        droneService.changeDroneStatus(droneId, status);
        return Result.success();
    }
//    删除无人机
    @DeleteMapping("/{id}/delete")
        public Result<Void> deleteDrone(@PathVariable("id") Long droneId) {
        droneService.deleteDrone(droneId);
        return Result.success();
    }
}
