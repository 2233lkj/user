/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-12
 * @description 角色权限管理
 */
package com.springboot.logindemo.service;

import com.springboot.logindemo.domain.Role;
import com.springboot.logindemo.domain.Permission;

import java.util.List;
import java.util.Set;

public interface RolePermissionService {
    // 为角色分配权限
    boolean assignPermissions(String adminPhoneNum, Long roleId, Set<Long> permissionIds);

    // 移除角色的权限
    boolean removePermissions(String adminPhoneNum, Long roleId, Set<Long> permissionIds);

    // 获取角色的权限列表
    Set<Long> getRolePermissionIds(String adminPhoneNum, Long roleId);

    // 创建新角色
    Role createRole(String adminPhoneNum, String name, String description, Set<Long> permissionIds);

    // 删除角色
    boolean deleteRole(String adminPhoneNum, Long roleId);

    // 重新启用角色
    boolean enableRole(String adminPhoneNum, Long roleId);

    // 禁用权限
    boolean disablePermission(String adminPhoneNum, Long permissionId);

    // 启用权限
    boolean enablePermission(String adminPhoneNum, Long permissionId);

    // 获取所有角色（包括active为0的）
    List<Role> getAllRoles(String adminPhoneNum);

    // 获取所有权限（包括active为0的）
    List<Permission> getAllPermissions(String adminPhoneNum);
}