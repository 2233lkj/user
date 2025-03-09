package com.springboot.logindemo.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

@Table(name = "user") // 说明此实体类对应数据库user表
@Entity // 说明此类是个实体类
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long uid;

    private String uname;
    private String password;
    private String phonenum;
    private Integer loginPermission = 1; // 默认登录权限为1
    private Integer adminPermission = 0; // 默认管理员权限为0

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime; // 用户创建时间

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime; // 用户信息更新时间

    @PrePersist
    protected void onCreate() {
        updateTime = LocalDateTime.now();
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhonenum() {
        return phonenum;
    }

    public void setPhonenum(String phonenum) {
        this.phonenum = phonenum;
    }

    public Integer getLoginPermission() {
        return loginPermission;
    }

    public void setLoginPermission(Integer loginPermission) {
        this.loginPermission = loginPermission;
    }

    public Integer getAdminPermission() {
        return adminPermission;
    }

    public void setAdminPermission(Integer adminPermission) {
        this.adminPermission = adminPermission;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
