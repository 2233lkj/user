/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-22
 * @description 权限管理
 */
package com.springboot.logindemo.security;

import java.util.Set;
import java.util.stream.Collectors;

import static com.springboot.logindemo.security.Permission.*;

public enum Role {
    ADMIN(Set.of(
            ADMIN_ACCESS,
            MANAGER_ACCESS,
            USER_ACCESS,
            READ_USER,
            CREATE_USER,
            UPDATE_USER,
            DELETE_USER,
            READ_PROFILE,
            UPDATE_PROFILE)),

    MANAGER(Set.of(
            MANAGER_ACCESS,
            USER_ACCESS,
            READ_USER,
            CREATE_USER,
            UPDATE_USER,
            READ_PROFILE,
            UPDATE_PROFILE)),

    USER(Set.of(
            USER_ACCESS,
            READ_PROFILE,
            UPDATE_PROFILE));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public Set<String> getPermissionNames() {
        return permissions.stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
    }
}