package com.minhtriet.se3979.catalogservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeductResult {
    private boolean success;
    private String message;

    public static DeductResult success() { return new DeductResult(true, "Trừ kho thành công"); }
    public static DeductResult fail(String msg) { return new DeductResult(false, msg); }
}