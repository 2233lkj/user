/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 人员管理模块为用户设置角色
 */
package com.springboot.logindemo.dto;

import java.util.Set;

public class AssignRolesRequestDto {
    private String adminPhoneNum; // 操作者的手机号
    private String targetPhoneNum; // 目标用户的手机号
    private Set<Long> roleIds;

    public String getAdminPhoneNum() {
        return adminPhoneNum;
    }

    public void setAdminPhoneNum(String adminPhoneNum) {
        this.adminPhoneNum = adminPhoneNum;
    }

    public String getTargetPhoneNum() {
        return targetPhoneNum;
    }

    public void setTargetPhoneNum(String targetPhoneNum) {
        this.targetPhoneNum = targetPhoneNum;
    }

    public Set<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(Set<Long> roleIds) {
        this.roleIds = roleIds;
    }
}