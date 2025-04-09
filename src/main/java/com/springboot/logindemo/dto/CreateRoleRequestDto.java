/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-13
 * @description 创建角色并为其分配权限
 */
package com.springboot.logindemo.dto;

import java.util.Set;

public class CreateRoleRequestDto {
    private String token; // 操作者的手机号
    private String name; // 角色名称
    private String description; // 角色描述
    private Set<Long> permissionIds; // 权限ID列表（可选）


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

    public Set<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(Set<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
}