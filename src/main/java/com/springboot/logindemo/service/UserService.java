package com.springboot.logindemo.service;

import com.springboot.logindemo.domain.User;

//注册登录的业务逻辑方法
public interface UserService {
    User loginService(String account, String password);

    User registerService(String phoneNum, String password, String uname);

    User verifyService(String phonenum);

    boolean updatePassword(String phoneNum, String newPassword);

    boolean updateLoginPermission(String phoneNum, Integer loginPermission);

    // 新增注销用户方法
    boolean deleteUser(String account);

    // 新增根据账号查找用户方法
    User findByAccount(String account);
}
