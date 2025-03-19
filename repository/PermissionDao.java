/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-12
 * @description 角色权限模块
 */
package com.springboot.logindemo.repository;

import com.springboot.logindemo.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PermissionDao extends JpaRepository<Permission, Long> {
    Set<Permission> findByIdIn(Set<Long> ids);

    // 查找活跃权限
    Set<Permission> findByActive(Integer active);

    // 根据ID集合和活跃状态查找权限
    Set<Permission> findByIdInAndActive(Set<Long> ids, Integer active);

    // 根据名称和活跃状态查找权限
    Permission findByNameAndActive(String name, Integer active);
}