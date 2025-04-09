/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 人员管理模块权限配置
 */
package com.springboot.logindemo.utils;

import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.domain.Role;
import com.springboot.logindemo.domain.Permission;

public class PermissionUtils {

    public static boolean hasPermission(User user, String permissionName) {
        if (user == null || user.getRoles() == null) {
            return false;
        }

        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .anyMatch(permission -> permission.getName().equals(permissionName));
    }
}