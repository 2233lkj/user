package com.springboot.logindemo.service;

import com.springboot.logindemo.domain.User;
import java.util.List;
import java.util.Set;

//注册登录的业务逻辑方法
public interface UserService {
    User loginService(String account, String password);

    User registerService(String phoneNum, String password, String uname);

    User verifyService(String phonenum);

    boolean updatePassword(String phoneNum, String newPassword);

    boolean updateLoginPermission(String adminPhoneNum, String targetPhoneNum, Integer loginPermission);

    // 新增注销用户方法
    boolean deleteUser(String account);

    // 新增根据账号查找用户方法
    User findByAccount(String account);

    boolean assignRoles(String adminPhoneNum, String targetPhoneNum, Set<Long> roleIds);

    Set<Long> getUserRoleIds(String adminPhoneNum, String targetPhoneNum);

    // 新增移除用户角色方法
    boolean removeRoles(String adminPhoneNum, String targetPhoneNum, Set<Long> roleIds);

    // 获取所有用户（包括loginPermission为0的）
    List<User> getAllUsers(String adminPhoneNum);
}
