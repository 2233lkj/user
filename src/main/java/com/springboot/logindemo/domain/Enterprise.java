package com.springboot.logindemo.domain;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author 企业法人认证信息实体类
 */
@Entity
@Table(name = "enterprise")
@Data
public class Enterprise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth_id", nullable = false)
    private Long authId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "tax_no", nullable = false, length = 50)
    private String taxNo;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "license_file", length = 255)
    private String licenseFile;

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