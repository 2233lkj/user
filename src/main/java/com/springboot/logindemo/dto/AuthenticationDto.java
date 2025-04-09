package com.springboot.logindemo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 认证信息DTO
 * @date 2025-3-11
 */
@Data
public class AuthenticationDto {
    private String token; // 操作者token
    private String authType; // 认证类型：natural, individual, enterprise
    private String authMethod; // 认证方式：biometric, national, document

    // 自然人信息
    private String name;
    private String idCard;
    private String phone;
    private String bankCard;
    private MultipartFile promiseFile;
    private MultipartFile delegateFile;

    // 个体工商户和企业法人共有信息
    private String businessName;
    private String taxNo;
    private String address;
    private MultipartFile licenseFile;
}