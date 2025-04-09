/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-17
 * @description 分配用户到部门请求
 */
package com.springboot.logindemo.dto;

import java.util.Set;

public class AssignUserToDepartmentRequestDto {
    private String token; // 操作者token
    private String targetPhoneNum; // 目标用户电话
    private Set<Long> departmentIds; // 部门ID列表
    private Long primaryDepartmentId; // 主部门ID

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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