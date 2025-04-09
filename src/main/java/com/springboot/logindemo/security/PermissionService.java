/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-22
 * @description 权限管理
 */
package com.springboot.logindemo.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PermissionService {

    /**
     * 检查当前用户是否拥有指定的权限
     * 
     * @param permissions 需要检查的权限列表
     * @return 如果用户拥有所有指定的权限，返回true；否则返回false
     */
    public boolean hasPermission(Permission... permissions) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // 获取当前用户的所有权限
        Set<String> userPermissions = getUserPermissions(authentication);

        // 检查用户是否拥有所有需要的权限
        return Arrays.stream(permissions)
                .map(Enum::name)
                .allMatch(userPermissions::contains);
    }

    /**
     * 检查当前用户是否拥有指定的角色
     * 
     * @param roles 需要检查的角色列表
     * @return 如果用户拥有任意一个指定的角色，返回true；否则返回false
     */
    public boolean hasRole(Role... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Set<String> userRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return Arrays.stream(roles)
                .map(Enum::name)
                .anyMatch(userRoles::contains);
    }

    /**
     * 获取当前用户的角色名称
     * 
     * @return 角色名称集合
     */
    public Set<String> getUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Set.of();
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());
    }

    /**
     * 获取用户的所有权限
     * 
     * @param authentication 认证对象
     * @return 权限名称集合
     */
    private Set<String> getUserPermissions(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .flatMap(authority -> {
                    // 处理基于角色的权限
                    if (authority.startsWith("ROLE_")) {
                        String roleName = authority.replace("ROLE_", "");
                        try {
                            Role role = Role.valueOf(roleName);
                            return role.getPermissionNames().stream();
                        } catch (IllegalArgumentException e) {
                            return Stream.empty();
                        }
                    }
                    // 处理直接的权限项
                    else {
                        return Stream.of(authority);
                    }
                })
                .collect(Collectors.toSet());
    }
}