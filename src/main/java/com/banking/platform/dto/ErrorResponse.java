package com.banking.platform.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private String message;
    private int statusCode;
    private LocalDateTime timeStamp;

    public ErrorResponse(String message, int statusCode){
        this.message=message;
        this.statusCode=statusCode;
        this.timeStamp=LocalDateTime.now();
    }



}
