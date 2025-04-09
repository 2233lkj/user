/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-17
 * @description 部门管理服务接口
 */
package com.springboot.logindemo.service;

import com.springboot.logindemo.domain.Department;
import java.util.List;
import java.util.Set;

public interface DepartmentService {
    // 创建部门
    Department createDepartment(String adminPhoneNum, String name, String description);

    // 将用户添加到部门
    boolean assignUserToDepartment(String adminPhoneNum, String targetPhoneNum, Set<Long> departmentIds,
            Long primaryDepartmentId);

    // 获取用户的部门列表
    Set<Department> getUserDepartments(String adminPhoneNum, String targetPhoneNum);

    // 删除部门
    boolean deleteDepartment(String adminPhoneNum, Long departmentId);

    // 从部门中移除用户
    boolean removeUserFromDepartment(String adminPhoneNum, Long departmentId, String targetPhoneNum);

    // 重新启用部门
    boolean enableDepartment(String adminPhoneNum, Long departmentId);

    // 获取所有部门（包括active为0的）
    List<Department> getAllDepartments(String adminPhoneNum);
}