package com.springboot.logindemo.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String account;
    private String password;
}