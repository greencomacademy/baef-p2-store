package com.deliveryinsider.store.domain.store.controller;

import com.deliveryinsider.store.domain.store.request.StoreUpdateRequest;
import com.deliveryinsider.store.domain.store.response.StoreResponse;
import com.deliveryinsider.store.domain.store.service.StoreService;
import com.deliveryinsider.store.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/me")
    public ResponseEntity<GlobalResponse<StoreResponse>> findMyStore(
            @RequestHeader("X-User-Id") Long userId
    ) {
        StoreResponse result = storeService.findMyStore(userId);
        return ResponseEntity.ok(GlobalResponse.success("내 매장 조회 성공", result));
    }

    @PatchMapping("/me")
    public ResponseEntity<GlobalResponse<StoreResponse>> updateMyStore(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody StoreUpdateRequest storeUpdateRequest
    ) {
        StoreResponse result = storeService.update(userId, storeUpdateRequest);
        return ResponseEntity.ok(GlobalResponse.success("내 매장 수정 성공", result));
    }
}
