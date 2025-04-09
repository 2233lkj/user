/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-22
 * @description 权限管理
 */
package com.springboot.logindemo.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class PermissionAspect {

    private final PermissionService permissionService;

    @Autowired
    public PermissionAspect(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Around("@annotation(com.springboot.logindemo.security.RequirePermission) || @within(com.springboot.logindemo.security.RequirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 首先检查方法级注解
        RequirePermission methodAnnotation = method.getAnnotation(RequirePermission.class);
        if (methodAnnotation != null) {
            if (!checkPermissions(methodAnnotation.value(), methodAnnotation.anyMatch())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足");
            }
        } else {
            // 如果方法级别没有，则检查类级别注解
            RequirePermission classAnnotation = method.getDeclaringClass().getAnnotation(RequirePermission.class);
            if (classAnnotation != null && !checkPermissions(classAnnotation.value(), classAnnotation.anyMatch())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "权限不足");
            }
        }

        return joinPoint.proceed();
    }

    @Around("@annotation(com.springboot.logindemo.security.RequireRole) || @within(com.springboot.logindemo.security.RequireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 首先检查方法级注解
        RequireRole methodAnnotation = method.getAnnotation(RequireRole.class);
        if (methodAnnotation != null) {
            if (!checkRoles(methodAnnotation.value(), methodAnnotation.anyMatch())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "角色权限不足");
            }
        } else {
            // 如果方法级别没有，则检查类级别注解
            RequireRole classAnnotation = method.getDeclaringClass().getAnnotation(RequireRole.class);
            if (classAnnotation != null && !checkRoles(classAnnotation.value(), classAnnotation.anyMatch())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "角色权限不足");
            }
        }

        return joinPoint.proceed();
    }

    private boolean checkPermissions(Permission[] permissions, boolean anyMatch) {
        if (anyMatch) {
            return Arrays.stream(permissions).anyMatch(permissionService::hasPermission);
        } else {
            return permissionService.hasPermission(permissions);
        }
    }

    private boolean checkRoles(Role[] roles, boolean anyMatch) {
        if (anyMatch) {
            return permissionService.hasRole(roles);
        } else {
            return Arrays.stream(roles).allMatch(role -> permissionService.hasRole(role));
        }
    }
}