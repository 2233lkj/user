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
 * 用于标记需要特定权限的方法或类
 * 使用方式：@RequirePermission(Permission.READ_USER)
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    Permission[] value();

    boolean anyMatch() default false; // 默认需要满足所有权限，设为true则只需满足任一权限
}