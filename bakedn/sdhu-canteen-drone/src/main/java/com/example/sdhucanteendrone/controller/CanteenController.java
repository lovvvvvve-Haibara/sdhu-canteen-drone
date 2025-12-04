package com.example.sdhucanteendrone.controller;

import com.example.sdhucanteendrone.Common.Result;
import com.example.sdhucanteendrone.dto.CanteenDto;
import com.example.sdhucanteendrone.entity.enums.CanteenOpenStatus;
import com.example.sdhucanteendrone.service.CanteenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

/**
 * 食堂管理相关接口
 *
 * - 面向普通用户：查看食堂列表、详情
 * - 面向管理员/食堂管理员：新增/修改食堂、配置管理员
 */
@RestController
@RequestMapping("/api/canteens")
@RequiredArgsConstructor
public class CanteenController {

    private final CanteenService canteenService;

    // ==================================================
    // 公共接口：列表 + 详情
    // ==================================================

    /**
     * 查询食堂列表（支持按状态过滤、分页）
     */
    @GetMapping
    public Result<Page<CanteenDto.CanteenSummary>> listCanteens(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CanteenOpenStatus status
    ) {
        Page<CanteenDto.CanteenSummary> resultPage =
                canteenService.listCanteens(status, PageRequest.of(page, size));
        return Result.success(resultPage);
    }

    /**
     * 获取指定食堂详情
     */
    @GetMapping("/{id}")
    public Result<CanteenDto.CanteenDetail> getCanteenById(@PathVariable("id") Long canteenId) {
        return Result.success(canteenService.getCanteenDetail(canteenId));
    }

    // ============================================================
    // 后台管理：新增/修改/状态
    // ============================================================

    /**
     * 新增食堂（仅管理员）
     */
    @PostMapping
    public Result<CanteenDto.CanteenDetail> createCanteen(
            @Valid @RequestBody CanteenDto.CanteenCreateReq req) {
        return Result.success(
                canteenService.createCanteen(req)
        );
    }


    /**
     * 删除食堂（仅管理员）
     */
    @DeleteMapping("/{id}")
    public Result deleteCanteen(@PathVariable("id") Long canteenId) {
        canteenService.deleteCanteen(canteenId);
        return Result.success();
    }

    /**
     * 修改食堂信息（名称 / 地点等）
     */
    @PatchMapping("/{id}")
    public Result<CanteenDto.CanteenDetail> updateCanteen(
            @PathVariable("id") Long canteenId,
            @Valid @RequestBody CanteenDto.CanteenUpdateReq req) {
        return Result.success(
                canteenService.updateCanteen(canteenId, req)
        );
    }

    /**
     * 修改食堂营业状态（OPEN / CLOSED）
     */
    @PatchMapping("/{id}/status")
    public Result<Void> changeCanteenStatus(
            @PathVariable("id") Long canteenId,
            @RequestParam("status") CanteenOpenStatus status) {
        canteenService.changeCanteenStatus(canteenId, status);
        return Result.success();
    }

    // ============================================================
    // 食堂管理员配置
    // ============================================================

    /**
     * 查询食堂管理员列表
     */
    @GetMapping("/{id}/managers")
    public Result<CanteenDto.CanteenManagerList> listCanteenManagers(
            @PathVariable("id") Long canteenId) {
        return Result.success(
                canteenService.listCanteenManagers(canteenId)
        );
    }

    /**
     * 添加食堂管理员
     *
     * @param userId 要绑定为管理员的用户ID（role 应为 CANTEEN）
     */
    @PostMapping("/{id}/managers")
    public Result<Void> addCanteenManager(@PathVariable("id") Long canteenId,
                                          @RequestParam("userId") Long userId) {
        canteenService.addCanteenManager(canteenId, userId);
        return Result.success();
    }

    /**
     * 移除食堂管理员
     */
    @DeleteMapping("/{id}/managers/{userId}")
    public Result<Void> removeCanteenManager(@PathVariable("id") Long canteenId,
                                             @PathVariable("userId") Long userId) {
        canteenService.removeCanteenManager(canteenId, userId);
        return Result.success();
    }
}
