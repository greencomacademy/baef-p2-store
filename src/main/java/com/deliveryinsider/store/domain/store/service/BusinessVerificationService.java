package com.deliveryinsider.store.domain.store.service;

import com.deliveryinsider.store.domain.store.entity.BusinessVerification;
import com.deliveryinsider.store.domain.store.mapper.BusinessVerificationMapper;
import com.deliveryinsider.store.domain.store.mapper.StoreMapper;
import com.deliveryinsider.store.domain.store.request.BusinessVerificationRequest;
import com.deliveryinsider.store.domain.store.response.BusinessVerificationResponse;
import com.deliveryinsider.store.global.error.BusinessException;
import com.deliveryinsider.store.global.error.StoreErrorCode;
import com.deliveryinsider.store.integration.nts.NtsBusinessClient;
import com.deliveryinsider.store.integration.nts.NtsBusinessVerificationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BusinessVerificationService {

    private static final String CONTINUING_BUSINESS_CODE = "01";
    private static final String SUSPENDED_BUSINESS_CODE = "02";
    private static final String CLOSED_BUSINESS_CODE = "03";

    private final NtsBusinessClient ntsBusinessClient;
    private final BusinessVerificationMapper businessVerificationMapper;
    private final StoreMapper storeMapper;

    @Value("${store.onboarding.verification-ttl-minutes:30}")
    private long verificationTtlMinutes;

    public BusinessVerificationResponse verify(
            Long userId,
            BusinessVerificationRequest request
    ) {
        if (storeMapper.findByUserId(userId) != null) {
            throw new BusinessException(
                    StoreErrorCode.STORE_ALREADY_EXISTS
            );
        }

        String businessRegistrationNumber = normalizeDigits(
                request.businessRegistrationNumber()
        );

        if (storeMapper.existsByBusinessRegistrationNumber(
                businessRegistrationNumber
        )) {
            throw new BusinessException(
                    StoreErrorCode.BUSINESS_ALREADY_REGISTERED
            );
        }

        String openingDate = normalizeDigits(
                request.openingDate()
        );

        NtsBusinessVerificationResult verificationResult =
                ntsBusinessClient.verify(
                        businessRegistrationNumber,
                        request.representativeName().trim(),
                        openingDate
                );

        if (!verificationResult.valid()) {
            throw new BusinessException(
                    StoreErrorCode.BUSINESS_VERIFICATION_FAILED
            );
        }

        validateBusinessStatus(
                verificationResult.businessStatusCode()
        );

        LocalDateTime verifiedAt = LocalDateTime.now(
                ZoneOffset.UTC
        );

        LocalDateTime expiresAt = verifiedAt.plusMinutes(
                verificationTtlMinutes
        );

        String verificationId = UUID.randomUUID().toString();

        BusinessVerification verification = BusinessVerification.builder()
                .id(verificationId)
                .userId(userId)
                .businessRegistrationNumber(businessRegistrationNumber)
                .representativeName(request.representativeName().trim())
                .openingDate(openingDate)
                .businessStatusCode(verificationResult.businessStatusCode())
                .businessStatusName(verificationResult.businessStatusName())
                .verifiedAt(verifiedAt)
                .expiresAt(expiresAt)
                .build();

        int inserted = businessVerificationMapper.insert(
                verification
        );

        if (inserted != 1) {
            throw new BusinessException(
                    StoreErrorCode.BUSINESS_VERIFICATION_PROVIDER_ERROR
            );
        }

        return new BusinessVerificationResponse(
                verificationId,
                businessRegistrationNumber,
                verificationResult.businessStatusCode(),
                verificationResult.businessStatusName(),
                verifiedAt,
                expiresAt
        );
    }

    private void validateBusinessStatus(
            String businessStatusCode
    ) {
        if (CONTINUING_BUSINESS_CODE.equals(
                businessStatusCode
        )) {
            return;
        }

        if (SUSPENDED_BUSINESS_CODE.equals(
                businessStatusCode
        )) {
            throw new BusinessException(
                    StoreErrorCode.BUSINESS_SUSPENDED
            );
        }

        if (CLOSED_BUSINESS_CODE.equals(
                businessStatusCode
        )) {
            throw new BusinessException(
                    StoreErrorCode.BUSINESS_CLOSED
            );
        }

        throw new BusinessException(
                StoreErrorCode.BUSINESS_VERIFICATION_FAILED
        );
    }

    private String normalizeDigits(
            String value
    ) {
        return value.replaceAll("[^0-9]", "");
    }
}
