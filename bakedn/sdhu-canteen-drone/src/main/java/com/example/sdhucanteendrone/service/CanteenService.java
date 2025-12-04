package com.example.sdhucanteendrone.service;

import com.example.sdhucanteendrone.dto.CanteenDto;
import com.example.sdhucanteendrone.entity.enums.CanteenOpenStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CanteenService {

    Page<CanteenDto.CanteenSummary> listCanteens(CanteenOpenStatus status, Pageable pageable);

    CanteenDto.CanteenDetail getCanteenDetail(Long canteenId);

    CanteenDto.CanteenDetail createCanteen(CanteenDto.CanteenCreateReq req);

    CanteenDto.CanteenDetail updateCanteen(Long canteenId, CanteenDto.CanteenUpdateReq req);

    void changeCanteenStatus(Long canteenId, CanteenOpenStatus status);

    CanteenDto.CanteenManagerList listCanteenManagers(Long canteenId);

    void addCanteenManager(Long canteenId, Long userId);

    void removeCanteenManager(Long canteenId, Long userId);

    void deleteCanteen(Long canteenId);
}
