package com.deliveryinsider.store.domain.store.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record StoreCreateRequest(

        @NotBlank(message = "사업자 검증 ID가 필요합니다.")
        String businessVerificationId,

        @NotBlank(message = "매장명을 입력해 주세요.")
        @Size(max = 100, message = "매장명은 100자 이하여야 합니다.")
        String storeName,

        @Size(max = 30, message = "전화번호는 30자 이하여야 합니다.")
        String phone,

        @NotBlank(message = "주소를 입력해 주세요.")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String address,

        @Size(max = 255, message = "상세 주소는 255자 이하여야 합니다.")
        String addressDetail,

        @NotBlank(message = "업종을 입력해 주세요.")
        @Size(max = 50, message = "업종은 50자 이하여야 합니다.")
        String industryType,

        @Min(value = 1, message = "주방 처리량은 1 이상이어야 합니다.")
        Integer kitchenCapacity,

        @Min(value = 0, message = "최소주문금액은 0 이상이어야 합니다.")
        Integer minimumOrderAmount,

        @NotBlank(message = "영업 시작 시각을 입력해 주세요.")
        @Pattern(
                regexp = "^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$",
                message = "영업 시작 시각은 HH:mm 또는 HH:mm:ss 형식이어야 합니다."
        )
        String openTime,

        @NotBlank(message = "영업 종료 시각을 입력해 주세요.")
        @Pattern(
                regexp = "^([01]\\d|2[0-3]):[0-5]\\d(:[0-5]\\d)?$",
                message = "영업 종료 시각은 HH:mm 또는 HH:mm:ss 형식이어야 합니다."
        )
        String closeTime

) {
}
