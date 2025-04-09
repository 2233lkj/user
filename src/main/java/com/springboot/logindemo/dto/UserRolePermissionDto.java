/**
 * @author 潘楠
 * @cooperators 
 * @date 2025-3-25
 * @description 用户角色权限DTO
 */
package com.springboot.logindemo.dto;

import com.springboot.logindemo.domain.Permission;
import com.springboot.logindemo.domain.Role;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;

public class UserRolePermissionDto {
    private Map<String, Object> userInfo; // 用户基本信息
    private List<Map<String, Object>> roles; // 用户角色列表

    public UserRolePermissionDto() {
        this.userInfo = new HashMap<>();
        this.roles = new ArrayList<>();
    }

    public Map<String, Object> getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Map<String, Object> userInfo) {
        this.userInfo = userInfo;
    }

    public List<Map<String, Object>> getRoles() {
        return roles;
    }

    public void setRoles(List<Map<String, Object>> roles) {
        this.roles = roles;
    }

    // 添加角色及其权限
    public void addRole(Role role) {
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("id", role.getId());
        roleMap.put("name", role.getName());
        roleMap.put("description", role.getDescription());

        List<Map<String, Object>> permissions = new ArrayList<>();
        if (role.getPermissions() != null) {
            for (Permission permission : role.getPermissions()) {
                Map<String, Object> permissionMap = new HashMap<>();
                permissionMap.put("id", permission.getId());
                permissionMap.put("name", permission.getName());
                permissionMap.put("description", permission.getDescription());
                permissions.add(permissionMap);
            }
        }
        roleMap.put("permissions", permissions);
        this.roles.add(roleMap);
    }
}