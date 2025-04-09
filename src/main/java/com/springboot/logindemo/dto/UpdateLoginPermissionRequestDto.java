/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 修改用户登录权限
 */
package com.springboot.logindemo.dto;

public class UpdateLoginPermissionRequestDto {
    private String token;
    private String targetPhoneNum;
    private Integer loginPermission;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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