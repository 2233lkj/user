package com.springboot.logindemo.service;

import com.springboot.logindemo.dto.AuthenticationDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author 认证服务接口
 */
public interface AuthenticationService {

    /**
     * 提交认证信息
     * 
     * @param authDto 认证信息DTO
     * @return 认证结果
     */
    Map<String, Object> submitAuthentication(AuthenticationDto authDto);

    /**
     * 查询用户认证状态
     * 
     * @param token 用户token
     * @return 认证状态信息
     */
    Map<String, Object> getAuthenticationStatus(String token);

    /**
     * 上传文件
     * 
     * @param file     文件
     * @param fileType 文件类型
     * @return 文件访问路径
     */
    String uploadFile(MultipartFile file, String fileType);
}