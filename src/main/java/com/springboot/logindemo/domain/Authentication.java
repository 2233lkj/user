package com.springboot.logindemo.domain;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author 认证信息实体类
 */
@Entity
@Table(name = "authentication")
@Data
public class Authentication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "auth_type", nullable = false, length = 20)
    private String authType;

    @Column(name = "auth_method", nullable = false, length = 20)
    private String authMethod;

    @Column(name = "auth_status")
    private Integer authStatus = 0; // 默认为待审核

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}