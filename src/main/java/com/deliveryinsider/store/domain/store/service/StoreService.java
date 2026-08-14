package com.deliveryinsider.store.domain.store.service;

import com.deliveryinsider.store.domain.store.entity.Store;
import com.deliveryinsider.store.domain.store.mapper.StoreMapper;
import com.deliveryinsider.store.domain.store.request.StoreUpdateRequest;
import com.deliveryinsider.store.domain.store.response.StoreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreMapper storeMapper;

    @Transactional(readOnly = true)
    public StoreResponse findMyStore(Long userId) {
        Store store = storeMapper.findByUserId(userId);
        if (store == null) {
            throw new RuntimeException("매장을 찾을 수 없습니다.");
        }
        return toStoreResponse(store);
    }

    @Transactional(rollbackFor = Exception.class)
    public StoreResponse update(Long userId, StoreUpdateRequest request) {
        Store currentStore = storeMapper.findByUserId(userId);

        if (currentStore == null) {
            throw new RuntimeException("수정할 매장이 없습니다.");
        }

        Store updateStore = Store.builder()
                .id(currentStore.getId())
                .userId(userId)
                .storeName(request.storeName())
                .phone(request.phone())
                .businessNumber(request.businessNumber())
                .address(request.address())
                .addressDetail(request.addressDetail())
                .industryType(request.industryType())
                .kitchenCapacity(request.kitchenCapacity())
                .minimumOrderAmount(request.minimumOrderAmount())
                .openTime(request.openTime())
                .closeTime(request.closeTime())
                .operationStatus(request.operationStatus())
                .build();

        int result = storeMapper.update(updateStore);

        if (result != 1) {
            throw new RuntimeException("매장 수정 중 문제가 발생했습니다.");
        }

        Store updatedStore = storeMapper.findByUserId(userId);
        return toStoreResponse(updatedStore);
    }

    private StoreResponse toStoreResponse(Store store) {
        return StoreResponse.builder()
                .id(store.getId())
                .userId(store.getUserId())
                .storeName(store.getStoreName())
                .phone(store.getPhone())
                .businessNumber(store.getBusinessNumber())
                .businessStatus(store.getBusinessStatus())
                .address(store.getAddress())
                .addressDetail(store.getAddressDetail())
                .industryType(store.getIndustryType())
                .kitchenCapacity(store.getKitchenCapacity())
                .minimumOrderAmount(store.getMinimumOrderAmount())
                .openTime(store.getOpenTime())
                .closeTime(store.getCloseTime())
                .operationStatus(store.getOperationStatus())
                .createdAt(store.getCreatedAt())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
