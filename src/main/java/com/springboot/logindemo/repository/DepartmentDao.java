/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-14
 * @description 部门数据访问接口
 */
package com.springboot.logindemo.repository;

import com.springboot.logindemo.domain.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DepartmentDao extends JpaRepository<Department, Long> {
    Department findByName(String name);

    Set<Department> findByIdIn(Set<Long> ids);

    // 查找活跃部门
    Set<Department> findByActive(Integer active);

    // 根据ID集合和活跃状态查找部门
    Set<Department> findByIdInAndActive(Set<Long> ids, Integer active);

    // 根据名称和活跃状态查找部门
    Department findByNameAndActive(String name, Integer active);
}