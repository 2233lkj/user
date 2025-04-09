/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-22
 * @description 权限管理
 */
package com.springboot.logindemo.security;

public enum Permission {
    READ_USER,
    CREATE_USER,
    UPDATE_USER,
    DELETE_USER,

    READ_PROFILE,
    UPDATE_PROFILE,

    ADMIN_ACCESS,
    MANAGER_ACCESS,
    USER_ACCESS
}