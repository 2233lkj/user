/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 移除角色的权限
 */
package com.springboot.logindemo.dto;

import java.util.Set;

public class RemovePermissionsRequestDto {
    private String adminPhoneNum; // 操作者的手机号
    private Long roleId; // 目标角色ID
    private Set<Long> permissionIds; // 权限ID列表

    public String getAdminPhoneNum() {
        return adminPhoneNum;
    }

    public void setAdminPhoneNum(String adminPhoneNum) {
        this.adminPhoneNum = adminPhoneNum;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Set<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(Set<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }
}