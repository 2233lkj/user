/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 人员管理模块接口逻辑
 */
package com.springboot.logindemo.service.serviceImpl;

import com.springboot.logindemo.domain.Role;
import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.repository.RoleDao;
import com.springboot.logindemo.repository.UserDao;
import com.springboot.logindemo.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder; // 自动注入BCryptPasswordEncoder

    @Resource // 帮助实例化UserDao对象
    private UserDao userDao;

    @Resource // 帮助实例化UserDao对象
    private RoleDao roleDao;

    @Override
    public User loginService(String account, String password) {
        // 根据电话号码和用户名查找用户
        User user = userDao.findByPhonenum(account);
        if (user == null) {
            user = userDao.findByUname(account);
        }
        if (user == null) {
            return null; // 用户不存在
        }
        // 验证密码是否正确
        boolean isValid = passwordEncoder.matches(password, user.getPassword());
        if (!isValid) {
            return null; // 密码错误
        }
        return user;
    }

    @Override
    @Transactional
    public User registerService(String phoneNum, String password, String uname) {
        // 在事务中进行所有检查
        try {
            // 检查用户名是否存在
            if (userDao.findByUname(uname) != null) {
                return null;
            }
            // 检查手机号是否存在
            if (userDao.findByPhonenum(phoneNum) != null) {
                return null;
            }

            User newUser = new User();
            newUser.setUname(uname);
            newUser.setPhonenum(phoneNum);
            String encryptedPassword = passwordEncoder.encode(password);
            newUser.setPassword(encryptedPassword);

            // 保存用户并立即返回保存后的用户对象
            return userDao.save(newUser);
        } catch (Exception e) {
            // 如果发生任何错误，事务会自动回滚
            throw new RuntimeException("注册失败：" + e.getMessage());
        }
    }

    @Override
    public User verifyService(String phonenum) {
        User user = userDao.findByPhonenum(phonenum);
        return user;
    }

    @Override
    public boolean updatePassword(String phoneNum, String newPassword) {
        User user = userDao.findByPhonenum(phoneNum);
        if (user == null) {
            return false;
        }
        // 对新密码进行加密
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encryptedPassword);
        user.setUpdateTime(LocalDateTime.now());// 保存更新时间
        userDao.save(user);
        return true;
    }

    // 管理员才能修改登录权限
    @Override
    public boolean updateLoginPermission(String adminPhoneNum, String targetPhoneNum, Integer loginPermission) {
        validateAdminPermission(adminPhoneNum);
        User user = userDao.findByPhonenum(targetPhoneNum);
        if (user == null) {
            return false;
        }
        user.setLoginPermission(loginPermission);// 设置登录权限
        user.setUpdateTime(LocalDateTime.now());// 保存更新时间
        userDao.save(user);
        return true;
    }

    @Override
    public User findByAccount(String account) {
        // 先通过手机号查找
        User user = userDao.findByPhonenum(account);
        if (user == null) {
            // 如果找不到，再通过用户名查找
            user = userDao.findByUname(account);
        }
        return user;
    }

    @Override
    @Transactional
    public boolean deleteUser(String account) {
        try {
            // 检查用户是否存在
            User user = findByAccount(account);
            if (user == null) {
                return false;
            }

            // 获取用户的角色ID
            Set<Long> roleIds = getUserRoleIds(account, account);

            // 检查是否包含管理员角色（假设角色名为"admin"是管理员角色）
            boolean isAdmin = roleIds.stream()
                    .anyMatch(roleId -> {
                        Role role = roleDao.findById(roleId).orElse(null);
                        return role != null && "admin".equals(role.getName());
                    });

            if (isAdmin) {
                return false; // 不允许删除管理员账号
            }
            userDao.delete(user);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除用户失败：" + e.getMessage());
        }
    }

    private void validateAdminPermission(String adminPhoneNum) {
        User admin = findByAccount(adminPhoneNum);
        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }

        // 获取用户的角色ID
        Set<Long> roleIds = getUserRoleIds(adminPhoneNum, adminPhoneNum);
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

    // 获取用户的角色
    @Override
    public Set<Long> getUserRoleIds(String adminPhoneNum, String targetPhoneNum) {
        // validateAdminPermission(adminPhoneNum);
        User user = findByAccount(targetPhoneNum);
        if (user == null || user.getRoles() == null) {
            return Set.of();
        }
        return user.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());
    }

    // 有管理员权限才可以为用户分配角色
    @Override
    @Transactional
    public boolean assignRoles(String adminPhoneNum, String targetPhoneNum, Set<Long> roleIds) {
        validateAdminPermission(adminPhoneNum);

        // 验证目标用户是否存在
        User user = findByAccount(targetPhoneNum);
        if (user == null) {
            throw new RuntimeException("目标用户不存在");
        }

        // 验证所有角色ID是否存在
        Set<Role> newRoles = roleDao.findByIdIn(roleIds);
        if (newRoles.size() != roleIds.size()) {
            // 找出哪些角色ID不存在
            Set<Long> foundRoleIds = newRoles.stream()
                    .map(Role::getId)
                    .collect(Collectors.toSet());
            Set<Long> invalidRoleIds = roleIds.stream()
                    .filter(id -> !foundRoleIds.contains(id))
                    .collect(Collectors.toSet());
            throw new RuntimeException("以下角色ID无效: " + invalidRoleIds);
        }

        try {
            // 获取用户现有的角色集合，如果为null则创建新的集合
            Set<Role> currentRoles = user.getRoles();
            if (currentRoles == null) {
                currentRoles = new java.util.HashSet<>();
            }

            // 将新角色添加到现有角色集合中
            currentRoles.addAll(newRoles);
            user.setRoles(currentRoles);
            user.setUpdateTime(LocalDateTime.now());
            userDao.save(user);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("分配角色失败：" + e.getMessage());
        }
    }

    // 有管理员权限才可以为用户移除角色
    @Override
    @Transactional
    public boolean removeRoles(String adminPhoneNum, String targetPhoneNum, Set<Long> roleIds) {
        validateAdminPermission(adminPhoneNum);

        // 验证目标用户是否存在
        User user = findByAccount(targetPhoneNum);
        if (user == null) {
            throw new RuntimeException("目标用户不存在");
        }

        try {
            // 获取用户现有的角色集合
            Set<Role> currentRoles = user.getRoles();
            if (currentRoles == null || currentRoles.isEmpty()) {
                throw new RuntimeException("用户没有任何角色可以移除");
            }

            // 只获取数据库中存在的角色
            Set<Role> rolesToRemove = roleDao.findByIdIn(roleIds);
            if (rolesToRemove.isEmpty()) {
                throw new RuntimeException("没有找到任何要删除的有效角色");
            }

            // 移除存在的角色
            currentRoles.removeAll(rolesToRemove);
            user.setRoles(currentRoles);
            user.setUpdateTime(LocalDateTime.now());
            userDao.save(user);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("移除角色失败：" + e.getMessage());
        }
    }

    // 获取所有用户（包括loginPermission为0的）
    @Override
    public List<User> getAllUsers(String adminPhoneNum) {
        // 验证管理员权限
        User admin = findByAccount(adminPhoneNum);
        if (admin == null) {
            throw new RuntimeException("用户不存在");
        }

        Set<Long> roleIds = getUserRoleIds(adminPhoneNum, adminPhoneNum);
        if (roleIds.isEmpty()) {
            throw new RuntimeException("用户没有任何角色");
        }

        boolean isAdmin = roleIds.stream()
                .anyMatch(roleId -> {
                    Role role = roleDao.findById(roleId).orElse(null);
                    return role != null && role.getActive() == 1 && ("admin".equalsIgnoreCase(role.getName()) ||
                            role.getName().contains("admin") ||
                            role.getName().contains("管理员"));
                });

        if (!isAdmin) {
            throw new RuntimeException("无管理员权限");
        }

        try {
            return userDao.findAll();
        } catch (Exception e) {
            throw new RuntimeException("获取所有用户失败：" + e.getMessage());
        }
    }
}
