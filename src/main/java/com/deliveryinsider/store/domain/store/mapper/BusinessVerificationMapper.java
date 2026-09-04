package com.deliveryinsider.store.domain.store.mapper;

import com.deliveryinsider.store.domain.store.entity.BusinessVerification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface BusinessVerificationMapper {

    int insert(BusinessVerification verification);

    BusinessVerification findByIdForUpdate(
            @Param("id") String id
    );

    int markConsumed(
            @Param("id") String id,
            @Param("userId") Long userId,
            @Param("consumedAt") LocalDateTime consumedAt
    );
}
