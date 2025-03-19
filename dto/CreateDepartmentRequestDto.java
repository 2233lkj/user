/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-17
 * @description 创建部门请求DTO
 */
package com.springboot.logindemo.dto;

public class CreateDepartmentRequestDto {
    private String adminPhoneNum; // 操作者的手机号
    private String name; // 部门名称
    private String description; // 部门描述

    public String getAdminPhoneNum() {
        return adminPhoneNum;
    }

    public void setAdminPhoneNum(String adminPhoneNum) {
        this.adminPhoneNum = adminPhoneNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}