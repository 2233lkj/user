package com.springboot.logindemo.controller;

import com.springboot.logindemo.utils.Result;
import com.springboot.logindemo.dto.AuthenticationDto;
import com.springboot.logindemo.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author 认证控制器
 * @date 2025-3-11
 */
@RestController
@RequestMapping("/api/authentication")
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173" }, allowCredentials = "true")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * 提交认证信息
     * 
     * @param authDto 认证信息DTO
     * @return Result
     */
    @PostMapping("/submit")
    public Result<Map<String, Object>> submitAuthentication(@RequestBody AuthenticationDto authDto) {
        try {
            if (authDto.getToken() == null || authDto.getToken().trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }

            Map<String, Object> result = authenticationService.submitAuthentication(authDto);
            return Result.success(result, "认证信息提交成功，等待审核");
        } catch (Exception e) {
            logger.error("提交认证信息失败", e);
            return Result.error("123", "认证信息提交失败: " + e.getMessage());
        }
    }

    /**
     * 上传认证文件
     * 
     * @param file     文件
     * @param fileType 文件类型
     * @return Result
     */
    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file,
            @RequestParam("fileType") String fileType) {
        try {
            logger.info("开始上传文件: {}, 类型: {}", file.getOriginalFilename(), fileType);

            if (file.isEmpty()) {
                logger.warn("上传的文件为空");
                return Result.error("123", "文件不能为空");
            }

            logger.info("文件大小: {} bytes", file.getSize());
            logger.info("文件内容类型: {}", file.getContentType());

            String filePath = authenticationService.uploadFile(file, fileType);
            logger.info("文件上传成功，路径: {}", filePath);

            return Result.success(filePath, "文件上传成功");
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return Result.error("123", "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取认证状态
     * 
     * @param token 用户token
     * @return Result
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getAuthenticationStatus(@RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }

            Map<String, Object> result = authenticationService.getAuthenticationStatus(token);
            return Result.success(result, "获取认证状态成功");
        } catch (Exception e) {
            logger.error("获取认证状态失败", e);
            return Result.error("123", "获取认证状态失败: " + e.getMessage());
        }
    }
}