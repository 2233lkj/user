/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-12
 * @description 角色权限模块
 */
package com.springboot.logindemo.service.serviceImpl;

import com.springboot.logindemo.domain.Permission;
import com.springboot.logindemo.domain.Role;
import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.repository.PermissionDao;
import com.springboot.logindemo.repository.RoleDao;
import com.springboot.logindemo.service.RolePermissionService;
import com.springboot.logindemo.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    @Resource
    private UserService userService;

    @Resource
    private RoleDao roleDao;

    @Resource
    private PermissionDao permissionDao;

    // 根据用户拥有的角色验证用户是否为管理员
    private void validateAdminPermission(String adminPhoneNum) {
        User admin = userService.findByAccount(adminPhoneNum);
        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }

        // 获取用户的角色ID
        Set<Long> roleIds = userService.getUserRoleIds(adminPhoneNum, adminPhoneNum);
        if (roleIds.isEmpty()) {
            throw new RuntimeException("用户没有任何角色");
        }

        // 检查是否包含管理员角色（检查角色名是否包含"admin"或"管理员"）
        boolean isAdmin = roleIds.stream()
                .anyMatch(roleId -> {
                    Role role = roleDao.findById(roleId).orElse(null);
                    return role != null && ("admin".equalsIgnoreCase(role.getName()) ||
                            role.getName().contains("admin") ||
                            role.getName().contains("管理员"));
                });

        if (!isAdmin) {
            throw new RuntimeException("无管理员权限");
        }
    }

    // 有管理员权限才可以为角色分配权限
    @Override
    @Transactional
    public boolean assignPermissions(String adminPhoneNum, Long roleId, Set<Long> permissionIds) {
        validateAdminPermission(adminPhoneNum);

        // 验证角色是否存在
        Role role = roleDao.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 检查角色是否活跃
        if (role.getActive() != 1) {
            throw new RuntimeException("角色已被删除或禁用");
        }

        // 验证所有权限ID是否存在且活跃
        Set<Permission> newPermissions = permissionDao.findByIdInAndActive(permissionIds, 1);
        if (newPermissions.size() != permissionIds.size()) {
            // 找出哪些权限ID不存在或不活跃
            Set<Long> foundPermissionIds = newPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());
            Set<Long> invalidPermissionIds = permissionIds.stream()
                    .filter(id -> !foundPermissionIds.contains(id))
                    .collect(Collectors.toSet());
            throw new RuntimeException("以下权限ID无效或已被禁用: " + invalidPermissionIds);
        }

        try {
            // 获取角色现有的权限集合，如果为null则创建新的集合
            Set<Permission> currentPermissions = role.getPermissions();
            if (currentPermissions == null) {
                currentPermissions = new java.util.HashSet<>();
            }

            // 将新权限添加到现有权限集合中
            currentPermissions.addAll(newPermissions);
            role.setPermissions(currentPermissions);
            role.setUpdateTime(LocalDateTime.now());
            roleDao.save(role);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("分配权限失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以为角色移除权限
    @Override
    @Transactional
    public boolean removePermissions(String adminPhoneNum, Long roleId, Set<Long> permissionIds) {
        validateAdminPermission(adminPhoneNum);

        // 验证角色是否存在
        Role role = roleDao.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 检查角色是否活跃
        if (role.getActive() != 1) {
            throw new RuntimeException("角色已被删除或禁用");
        }

        try {
            // 获取角色现有的权限集合
            Set<Permission> currentPermissions = role.getPermissions();
            if (currentPermissions == null || currentPermissions.isEmpty()) {
                throw new RuntimeException("角色没有任何权限可以移除");
            }

            // 只获取数据库中存在的权限
            Set<Permission> permissionsToRemove = permissionDao.findByIdIn(permissionIds);
            if (permissionsToRemove.isEmpty()) {
                throw new RuntimeException("没有找到任何要删除的有效权限");
            }

            // 移除存在的权限
            currentPermissions.removeAll(permissionsToRemove);
            role.setPermissions(currentPermissions);
            role.setUpdateTime(LocalDateTime.now());
            roleDao.save(role);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("移除权限失败：" + e.getMessage());
        }
    }

    // 用户自身可以获取自己角色的权限 ？ 未完善
    // 用户可以看自己的角色/管理员可以看所有用户的角色
    @Override
    public Set<Long> getRolePermissionIds(String adminPhoneNum, Long roleId) {
        // validateAdminPermission(adminPhoneNum);

        Role role = roleDao.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 检查角色是否活跃
        if (role.getActive() != 1) {
            throw new RuntimeException("角色已被禁用");
        }

        if (role.getPermissions() == null) {
            return Set.of();
        }

        return role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Permission> getRolePermissions(String adminPhoneNum, Long roleId) {
        // validateAdminPermission(adminPhoneNum);

        Role role = roleDao.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 检查角色是否活跃
        if (role.getActive() != 1) {
            throw new RuntimeException("角色已被禁用");
        }

        if (role.getPermissions() == null) {
            return List.of();
        }

        // 过滤出活跃的权限
        return role.getPermissions().stream()
                .filter(permission -> permission.getActive() == 1)
                .collect(Collectors.toList());
    }

    // 有管理员权限才可以创建角色
    @Override
    @Transactional
    public Role createRole(String adminPhoneNum, String name, String description, Set<Long> permissionIds) {
        validateAdminPermission(adminPhoneNum);

        // 验证角色名是否已存在
        Role existingRole = roleDao.findByName(name);
        if (existingRole != null) {
            throw new RuntimeException("角色名已存在");
        }

        Role role = new Role();
        role.setName(name);
        role.setDescription(description);

        // 如果提供了权限ID，则设置权限
        if (permissionIds != null && !permissionIds.isEmpty()) {
            Set<Permission> permissions = permissionDao.findByIdIn(permissionIds);
            if (permissions.size() != permissionIds.size()) {
                // 找出哪些权限ID不存在
                Set<Long> foundPermissionIds = permissions.stream()
                        .map(Permission::getId)
                        .collect(Collectors.toSet());
                Set<Long> invalidPermissionIds = permissionIds.stream()
                        .filter(id -> !foundPermissionIds.contains(id))
                        .collect(Collectors.toSet());
                throw new RuntimeException("以下权限ID无效: " + invalidPermissionIds);
            }
            role.setPermissions(permissions);
        }

        try {
            return roleDao.save(role);
        } catch (Exception e) {
            throw new RuntimeException("创建角色失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以禁用角色
    @Override
    @Transactional
    public boolean deleteRole(String adminPhoneNum, Long roleId) {
        validateAdminPermission(adminPhoneNum);

        // 验证角色是否存在
        Role role = roleDao.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 检查角色是否活跃
        if (role.getActive() != 1) {
            throw new RuntimeException("角色已被禁用");
        }

        try {
            // 逻辑删除角色（将active字段设置为0）
            role.setActive(0);
            role.setUpdateTime(LocalDateTime.now());
            roleDao.save(role);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("删除角色失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以重新启用角色
    @Override
    @Transactional
    public boolean enableRole(String adminPhoneNum, Long roleId) {
        validateAdminPermission(adminPhoneNum);

        // 验证角色是否存在
        Role role = roleDao.findById(roleId)
                .orElseThrow(() -> new RuntimeException("角色不存在"));

        // 检查角色是否已经被禁用
        if (role.getActive() == 1) {
            throw new RuntimeException("角色已经处于启用状态");
        }

        try {
            // 检查是否有同名的活跃角色
            Role activeRoleWithSameName = roleDao.findByNameAndActive(role.getName(), 1);
            if (activeRoleWithSameName != null) {
                throw new RuntimeException("已存在同名的活跃角色，无法启用");
            }

            // 启用角色（将active字段设置为1）
            role.setActive(1);
            role.setUpdateTime(LocalDateTime.now());
            roleDao.save(role);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("启用角色失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以禁用权限
    @Override
    @Transactional
    public boolean disablePermission(String adminPhoneNum, Long permissionId) {
        validateAdminPermission(adminPhoneNum);

        // 验证权限是否存在
        Permission permission = permissionDao.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("权限不存在"));

        // 检查权限是否已被禁用
        if (permission.getActive() != 1) {
            throw new RuntimeException("权限已被删除或禁用");
        }

        try {
            // 逻辑删除权限（将active字段设置为0）
            permission.setActive(0);
            permission.setUpdateTime(LocalDateTime.now());
            permissionDao.save(permission);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("禁用权限失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以启用权限
    @Override
    @Transactional
    public boolean enablePermission(String adminPhoneNum, Long permissionId) {
        validateAdminPermission(adminPhoneNum);

        // 验证权限是否存在
        Permission permission = permissionDao.findById(permissionId)
                .orElseThrow(() -> new RuntimeException("权限不存在"));

        // 检查权限是否已经被启用
        if (permission.getActive() == 1) {
            throw new RuntimeException("权限已经处于启用状态");
        }

        try {
            // 检查是否有同名的活跃权限
            Permission activePermissionWithSameName = permissionDao.findByNameAndActive(permission.getName(), 1);
            if (activePermissionWithSameName != null) {
                throw new RuntimeException("已存在同名的活跃权限，无法启用");
            }

            // 启用权限（将active字段设置为1）
            permission.setActive(1);
            permission.setUpdateTime(LocalDateTime.now());
            permissionDao.save(permission);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("启用权限失败：" + e.getMessage());
        }
    }

    // 获取所有角色（包括active为0的）
    @Override
    public List<Role> getAllRoles(String adminPhoneNum) {
        validateAdminPermission(adminPhoneNum);

        try {
            return roleDao.findAll();
        } catch (Exception e) {
            throw new RuntimeException("获取所有角色失败：" + e.getMessage());
        }
    }

    // 获取所有权限（包括active为0的）
    @Override
    public List<Permission> getAllPermissions(String adminPhoneNum) {
        validateAdminPermission(adminPhoneNum);

        try {
            return permissionDao.findAll();
        } catch (Exception e) {
            throw new RuntimeException("获取所有权限失败：" + e.getMessage());
        }
    }
}