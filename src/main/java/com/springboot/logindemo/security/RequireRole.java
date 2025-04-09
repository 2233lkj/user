/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-22
 * @description 权限管理
 */
package com.springboot.logindemo.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于标记需要特定角色的方法或类
 * 使用方式：@RequireRole(Role.ADMIN)
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    Role[] value();

    boolean anyMatch() default true; // 默认只需满足任一角色
}