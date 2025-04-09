/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-17
 * @description 部门管理服务实现
 */
package com.springboot.logindemo.service.serviceImpl;

import com.springboot.logindemo.domain.Department;
import com.springboot.logindemo.domain.Role;
import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.repository.DepartmentDao;
import com.springboot.logindemo.repository.RoleDao;
import com.springboot.logindemo.repository.UserDao;
import com.springboot.logindemo.service.DepartmentService;
import com.springboot.logindemo.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private UserService userService;

    @Resource
    private DepartmentDao departmentDao;

    @Resource
    private RoleDao roleDao;

    @Resource
    private UserDao userDao;

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

    // 有管理员权限才可以创建部门
    @Override
    @Transactional
    public Department createDepartment(String adminPhoneNum, String name, String description) {
        validateAdminPermission(adminPhoneNum);

        // 验证部门名是否已存在
        if (departmentDao.findByName(name) != null) {
            throw new RuntimeException("部门名已存在");
        }

        Department department = new Department();
        department.setName(name);
        department.setDescription(description);

        try {
            return departmentDao.save(department);
        } catch (Exception e) {
            throw new RuntimeException("创建部门失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以为用户分配部门
    @Override
    @Transactional
    public boolean assignUserToDepartment(String adminPhoneNum, String targetPhoneNum, Set<Long> departmentIds,
            Long primaryDepartmentId) {
        validateAdminPermission(adminPhoneNum);

        // 验证用户是否存在
        User user = userService.findByAccount(targetPhoneNum);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证所有部门ID是否存在
        Set<Department> departments = departmentDao.findByIdIn(departmentIds);
        if (departments.size() != departmentIds.size()) {
            // 找出哪些部门ID不存在
            Set<Long> foundDepartmentIds = departments.stream()
                    .map(Department::getId)
                    .collect(java.util.stream.Collectors.toSet());
            Set<Long> invalidDepartmentIds = departmentIds.stream()
                    .filter(id -> !foundDepartmentIds.contains(id))
                    .collect(java.util.stream.Collectors.toSet());
            throw new RuntimeException("以下部门ID无效: " + invalidDepartmentIds);
        }

        try {
            // 获取用户现有的部门集合，如果为null则创建新的集合
            Set<Department> currentDepartments = user.getDepartments();
            if (currentDepartments == null) {
                currentDepartments = new HashSet<>();
            }

            // 将新部门添加到现有部门集合中
            currentDepartments.addAll(departments);
            user.setDepartments(currentDepartments);

            // 如果指定了主部门，设置主部门
            if (primaryDepartmentId != null) {
                Department primaryDepartment = departmentDao.findById(primaryDepartmentId)
                        .orElseThrow(() -> new RuntimeException("主部门不存在"));

                // 确保主部门在用户的部门列表中
                if (!currentDepartments.contains(primaryDepartment)) {
                    currentDepartments.add(primaryDepartment);
                }

                user.setPrimaryDepartment(primaryDepartment);
            }

            userDao.save(user);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("分配部门失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以获取用户的部门
    @Override
    public Set<Department> getUserDepartments(String adminPhoneNum, String targetPhoneNum) {
        validateAdminPermission(adminPhoneNum);

        User user = userService.findByAccount(targetPhoneNum);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (user.getDepartments() == null) {
            return Set.of();
        }

        return user.getDepartments();
    }

    // 有管理员权限才可以删除部门
    @Override
    @Transactional
    public boolean deleteDepartment(String adminPhoneNum, Long departmentId) {
        validateAdminPermission(adminPhoneNum);

        // 验证部门是否存在
        Department department = departmentDao.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        // 检查部门是否已被删除
        if (department.getActive() != 1) {
            throw new RuntimeException("部门已被删除或禁用");
        }

        try {
            // 检查部门是否还有关联的用户或角色
            if ((department.getUsers() != null && !department.getUsers().isEmpty()) ||
                    (department.getRoles() != null && !department.getRoles().isEmpty())) {
                throw new RuntimeException("部门中还存在用户或角色，无法删除");
            }

            // 逻辑删除部门（将active字段设置为0）
            department.setActive(0);
            department.setUpdateTime(LocalDateTime.now());
            departmentDao.save(department);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("删除部门失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以从部门中移除用户
    @Override
    @Transactional
    public boolean removeUserFromDepartment(String adminPhoneNum, Long departmentId, String targetPhoneNum) {
        validateAdminPermission(adminPhoneNum);

        // 验证部门是否存在
        Department department = departmentDao.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        // 验证用户是否存在
        User user = userService.findByAccount(targetPhoneNum);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        try {
            // 获取部门的用户集合
            Set<User> departmentUsers = department.getUsers();
            if (departmentUsers == null || departmentUsers.isEmpty()) {
                throw new RuntimeException("部门中没有用户可以移除");
            }

            // 移除用户
            boolean removed = departmentUsers.remove(user);
            if (!removed) {
                throw new RuntimeException("该用户不在部门中");
            }

            // 更新部门
            department.setUsers(departmentUsers);
            department.setUpdateTime(LocalDateTime.now());
            departmentDao.save(department);

            // 更新用户
            Set<Department> userDepartments = user.getDepartments();
            if (userDepartments != null) {
                userDepartments.remove(department);
                user.setDepartments(userDepartments);

                // 如果移除的是用户的主部门，清除主部门设置
                if (department.equals(user.getPrimaryDepartment())) {
                    user.setPrimaryDepartment(null);
                }

                user.setUpdateTime(LocalDateTime.now());
                userDao.save(user);
            }

            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("从部门中移除用户失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以重新启用部门
    @Override
    @Transactional
    public boolean enableDepartment(String adminPhoneNum, Long departmentId) {
        validateAdminPermission(adminPhoneNum);

        // 验证部门是否存在
        Department department = departmentDao.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("部门不存在"));

        // 检查部门是否已经被禁用
        if (department.getActive() == 1) {
            throw new RuntimeException("部门已经处于启用状态");
        }

        try {
            // 检查是否有同名的活跃部门
            Department activeDepartmentWithSameName = departmentDao.findByNameAndActive(department.getName(), 1);
            if (activeDepartmentWithSameName != null) {
                throw new RuntimeException("已存在同名的活跃部门，无法启用");
            }

            // 启用部门（将active字段设置为1）
            department.setActive(1);
            department.setUpdateTime(LocalDateTime.now());
            departmentDao.save(department);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("启用部门失败：" + e.getMessage());
        }
    }

    // 获取所有部门（包括active为0的）
    @Override
    public List<Department> getAllDepartments(String adminPhoneNum) {
        validateAdminPermission(adminPhoneNum);
        try {
            return departmentDao.findAll();
        } catch (Exception e) {
            throw new RuntimeException("获取所有部门失败：" + e.getMessage());
        }
    }
}