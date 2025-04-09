/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 人员管理模块接口逻辑
 */
package com.springboot.logindemo.service.serviceImpl;

import com.springboot.logindemo.domain.Department;
import com.springboot.logindemo.domain.Role;
import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.repository.DepartmentDao;
import com.springboot.logindemo.repository.RoleDao;
import com.springboot.logindemo.repository.UserDao;
import com.springboot.logindemo.service.UserService;
import com.springboot.logindemo.dto.UserRolePermissionDto;
import com.springboot.logindemo.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder; // 自动注入BCryptPasswordEncoder

    @Resource // 帮助实例化UserDao对象
    private UserDao userDao;

    @Resource // 帮助实例化UserDao对象
    private RoleDao roleDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private DepartmentDao departmentDao;

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
    public boolean updateLoginPermission(String token, String targetPhoneNum, Integer loginPermission) {
        try {
            // 获取操作者信息
            Map<String, Object> operatorInfo = getUserInfoByToken(token);
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                throw new RuntimeException("无法获取操作者电话号码");
            }

            // 验证操作者是否为管理员
            validateAdminPermission(operatorPhoneNum);

            // 获取目标用户
            User targetUser = userDao.findByPhonenum(targetPhoneNum);
            if (targetUser == null) {
                throw new RuntimeException("目标用户不存在");
            }

            // 检查目标用户是否有角色ID为1（管理员角色）
            UserRolePermissionDto targetUserPermission = getUserRolePermissionByPhoneNum(targetPhoneNum);
            boolean isTargetAdmin = false;
            for (Map<String, Object> role : targetUserPermission.getRoles()) {
                Long roleId = ((Number) role.get("id")).longValue();
                if (roleId == 1L) {
                    isTargetAdmin = true;
                    break;
                }
            }

            // 如果目标用户是管理员，则不允许修改其登录权限
            if (isTargetAdmin) {
                throw new RuntimeException("不能修改管理员的登录权限");
            }

            // 更新目标用户的登录权限
            targetUser.setLoginPermission(loginPermission);
            targetUser.setUpdateTime(LocalDateTime.now());
            userDao.save(targetUser);
            return true;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("修改用户登录权限失败：" + e.getMessage());
        }
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
    public boolean deleteUser(String token) {
        try {
            // 获取用户信息
            Map<String, Object> userInfo = getUserInfoByToken(token);
            String phoneNum = (String) userInfo.get("phonenum");
            if (phoneNum == null || phoneNum.trim().isEmpty()) {
                throw new RuntimeException("无法获取用户电话号码");
            }

            // 检查用户是否存在
            User user = findByAccount(phoneNum);
            if (user == null) {
                return false;
            }

            // 获取用户的角色ID
            Set<Long> roleIds = getUserRoleIds(phoneNum, phoneNum);

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

    /**
     * 检查用户是否具有特定权限ID
     * 
     * @param token        用户的JWT令牌
     * @param permissionId 要检查的权限ID
     * @return 如果用户具有该权限返回true，否则返回false
     */
    private boolean hasPermission(String token, Long permissionId) {
        try {
            // 使用token获取用户角色和权限信息
            UserRolePermissionDto userRolePermission = getUserRolePermissionByToken(token);

            // 遍历用户的所有角色和权限
            for (Map<String, Object> role : userRolePermission.getRoles()) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> permissions = (List<Map<String, Object>>) role.get("permissions");
                if (permissions != null) {
                    // 检查是否有指定ID的权限
                    for (Map<String, Object> permission : permissions) {
                        Long currentPermissionId = ((Number) permission.get("id")).longValue();
                        if (currentPermissionId.equals(permissionId)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("检查用户权限失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否拥有指定角色
     * 
     * @param token  用户令牌
     * @param roleId 角色ID
     * @return 是否拥有该角色
     */
    private boolean hasRole(String token, Long roleId) {
        try {
            // 使用token获取用户角色和权限信息
            UserRolePermissionDto userRolePermission = getUserRolePermissionByToken(token);

            // 遍历用户的所有角色
            for (Map<String, Object> role : userRolePermission.getRoles()) {
                Long currentRoleId = ((Number) role.get("id")).longValue();
                if (currentRoleId.equals(roleId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("检查用户角色失败：" + e.getMessage());
        }
    }

    /**
     * 检查用户是否拥有指定角色 (通过账号直接查询)
     * 
     * @param account 用户账号(电话号码或用户名)
     * @param roleId  角色ID
     * @return 是否拥有该角色
     */
    @Override
    public boolean hasRoleByAccount(String account, Long roleId) {
        try {
            // 查找用户
            User user = findByAccount(account);
            if (user == null) {
                return false;
            }

            // 检查用户角色
            if (user.getRoles() != null) {
                for (Role role : user.getRoles()) {
                    if (role.getId().equals(roleId)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            throw new RuntimeException("检查用户角色失败：" + e.getMessage());
        }
    }

    // 获取所有用户（需要有查看权限的用户才能访问）
    @Override
    public List<User> getAllUsers(String token) {
        try {
            // 检查用户是否为管理员
            if (!hasRole(token, (long) 1L)) {
                throw new RuntimeException("没有查看所有用户的权限");
            }
            return userDao.findAll();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("获取所有用户失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getUserInfoByToken(String token) {
        try {
            // 从token中获取用户ID
            String uid = String.valueOf(JwtUtils.getUserIdFromToken(token));
            if (uid == null || uid.equals("null")) {
                throw new RuntimeException("无效的token");
            }

            // 从数据库获取最新的用户信息
            User user = userDao.findById(Long.parseLong(uid)).orElse(null);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            // 更新Redis缓存
            String redisKey = "user:" + uid;
            redisTemplate.delete(redisKey); // 删除旧的缓存
            redisTemplate.opsForValue().set(redisKey, user, 24, TimeUnit.HOURS);

            // 构建返回的用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", user.getUid());
            userInfo.put("uname", user.getUname());
            userInfo.put("phonenum", user.getPhonenum());
            userInfo.put("loginPermission", user.getLoginPermission());
            userInfo.put("adminPermission", user.getAdminPermission());
            userInfo.put("createTime", user.getCreateTime());
            userInfo.put("updateTime", user.getUpdateTime());
            userInfo.put("roles", user.getRoles());

            return userInfo;
        } catch (Exception e) {
            throw new RuntimeException("获取用户信息失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserRolePermissionDto getUserRolePermissionByToken(String token) {
        // 获取用户基本信息
        Map<String, Object> userInfo = getUserInfoByToken(token);

        // 获取用户电话号码
        String phoneNum = (String) userInfo.get("phonenum");
        if (phoneNum == null || phoneNum.trim().isEmpty()) {
            throw new RuntimeException("无法获取用户电话号码");
        }

        // 查询用户
        User user = findByAccount(phoneNum);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 创建DTO对象
        UserRolePermissionDto dto = new UserRolePermissionDto();
        dto.setUserInfo(userInfo);

        // 获取用户角色和权限
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                dto.addRole(role);
            }
        }

        return dto;
    }

    @Transactional(readOnly = true)
    @Override
    public UserRolePermissionDto getUserRolePermissionByPhoneNum(String phoneNum) {
        // 验证电话号码
        if (phoneNum == null || phoneNum.trim().isEmpty()) {
            throw new RuntimeException("电话号码不能为空");
        }

        // 查询用户
        User user = findByAccount(phoneNum);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 创建用户基本信息Map
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("uid", user.getUid());
        userInfo.put("uname", user.getUname());
        userInfo.put("phonenum", user.getPhonenum());
        userInfo.put("createTime", user.getCreateTime());
        userInfo.put("updateTime", user.getUpdateTime());
        userInfo.put("adminPermission", user.getAdminPermission());
        userInfo.put("loginPermission", user.getLoginPermission());

        // 创建DTO对象
        UserRolePermissionDto dto = new UserRolePermissionDto();
        dto.setUserInfo(userInfo);

        // 获取用户角色和权限
        if (user.getRoles() != null) {
            for (Role role : user.getRoles()) {
                dto.addRole(role);
            }
        }

        return dto;
    }
}
