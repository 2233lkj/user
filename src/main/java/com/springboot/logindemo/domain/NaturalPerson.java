package com.springboot.logindemo.domain;

import lombok.Data;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * @author 自然人认证信息实体类
 */
@Entity
@Table(name = "natural_person")
@Data
public class NaturalPerson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "auth_id", nullable = false)
    private Long authId;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "id_card", nullable = false, length = 20)
    private String idCard;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "bank_card", nullable = false, length = 30)
    private String bankCard;

    @Column(name = "promise_file", length = 255)
    private String promiseFile;

    @Column(name = "delegate_file", length = 255)
    private String delegateFile;

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