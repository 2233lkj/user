package com.springboot.logindemo.service;

import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.dto.UserRolePermissionDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

//注册登录的业务逻辑方法
public interface UserService {
    User loginService(String account, String password);

    User registerService(String phoneNum, String password, String uname);

    User verifyService(String phonenum);

    boolean updatePassword(String phoneNum, String newPassword);

    boolean updateLoginPermission(String token, String targetPhoneNum, Integer loginPermission);

    // 删除用户
    boolean deleteUser(String token);

    // 新增根据账号查找用户方法
    User findByAccount(String account);

    boolean assignRoles(String adminPhoneNum, String targetPhoneNum, Set<Long> roleIds);

    Set<Long> getUserRoleIds(String adminPhoneNum, String targetPhoneNum);

    // 新增移除用户角色方法
    boolean removeRoles(String adminPhoneNum, String targetPhoneNum, Set<Long> roleIds);

    // 获取所有用户（包括loginPermission为0的）
    List<User> getAllUsers(String token);

    // 根据token获取用户信息
    Map<String, Object> getUserInfoByToken(String token);

    // 根据token获取用户角色和权限信息
    UserRolePermissionDto getUserRolePermissionByToken(String token);

    // 根据账号检查用户是否拥有指定角色
    boolean hasRoleByAccount(String account, Long roleId);

    @Transactional(readOnly = true)
    UserRolePermissionDto getUserRolePermissionByPhoneNum(String phoneNum);
}
