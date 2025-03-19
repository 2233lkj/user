/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 修改用户登录权限
 */
package com.springboot.logindemo.dto;

public class UpdateLoginPermissionRequestDto {
    private String adminPhoneNum;
    private String targetPhoneNum;
    private Integer loginPermission;

    public String getAdminPhoneNum() {
        return adminPhoneNum;
    }

    public void setAdminPhoneNum(String adminPhoneNum) {
        this.adminPhoneNum = adminPhoneNum;
    }

    public String getTargetPhoneNum() {
        return targetPhoneNum;
    }

    public void setTargetPhoneNum(String targetPhoneNum) {
        this.targetPhoneNum = targetPhoneNum;
    }

    public Integer getLoginPermission() {
        return loginPermission;
    }

    public void setLoginPermission(Integer loginPermission) {
        this.loginPermission = loginPermission;
    }
}