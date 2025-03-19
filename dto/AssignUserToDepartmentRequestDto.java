/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-17
 * @description 将用户添加到部门
 */
package com.springboot.logindemo.dto;

import java.util.Set;

public class AssignUserToDepartmentRequestDto {
    private String adminPhoneNum; // 操作者的手机号
    private String targetPhoneNum; // 目标用户的手机号
    private Set<Long> departmentIds; // 部门ID列表
    private Long primaryDepartmentId; // 主部门ID（可选）

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

    public Set<Long> getDepartmentIds() {
        return departmentIds;
    }

    public void setDepartmentIds(Set<Long> departmentIds) {
        this.departmentIds = departmentIds;
    }

    public Long getPrimaryDepartmentId() {
        return primaryDepartmentId;
    }

    public void setPrimaryDepartmentId(Long primaryDepartmentId) {
        this.primaryDepartmentId = primaryDepartmentId;
    }
}