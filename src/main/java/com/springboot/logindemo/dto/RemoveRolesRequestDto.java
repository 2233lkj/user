/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-12
 * @description 移除用户的角色
 */
package com.springboot.logindemo.dto;

import java.util.Set;

public class RemoveRolesRequestDto {
    private String token;
    private String targetPhoneNum;
    private Set<Long> roleIds;

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

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
}