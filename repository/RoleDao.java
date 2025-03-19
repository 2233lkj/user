/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 人员管理模块角色模块
 */
package com.springboot.logindemo.repository;

import com.springboot.logindemo.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDao extends JpaRepository<Role, Long> {
    Set<Role> findByIdIn(Set<Long> roleIds);

    Role findByName(String name);

    // 查找活跃角色
    Set<Role> findByActive(Integer active);

    // 根据ID集合和活跃状态查找角色
    Set<Role> findByIdInAndActive(Set<Long> roleIds, Integer active);

    // 根据名称和活跃状态查找角色
    Role findByNameAndActive(String name, Integer active);
}