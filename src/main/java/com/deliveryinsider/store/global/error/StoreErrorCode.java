package com.deliveryinsider.store.global.error;

import org.springframework.http.HttpStatus;

public enum StoreErrorCode implements ErrorCode {
    STORE_NOT_FOUND("STORE-001", HttpStatus.NOT_FOUND, "매장을 찾을 수 없습니다."),
    STORE_ALREADY_EXISTS("STORE-002", HttpStatus.CONFLICT, "활성 매장이 이미 존재합니다."),
    BUSINESS_VERIFICATION_FAILED("STORE-003", HttpStatus.BAD_REQUEST, "사업자 검증에 실패했습니다."),
    IN_EDT_STORE_ERROR("STORE_004",HttpStatus.INTERNAL_SERVER_ERROR,"매장 수정 중 문제가 발생했습니다."),
    STORE_NOT_ORDERABLE("STORE-005", HttpStatus.CONFLICT, "현재 주문을 받을 수 없는 매장입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;

    StoreErrorCode(String code, HttpStatus status, String message) { this.code = code; this.status = status; this.message = message; }
    @Override public String code() { return code; }
    @Override public HttpStatus status() { return status; }
    @Override public String message() { return message; }
}
