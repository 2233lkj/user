package com.springboot.logindemo.service.serviceImpl;

import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.repository.UserDao;
import com.springboot.logindemo.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder; // 自动注入BCryptPasswordEncoder

    @Resource // 帮助实例化UserDao对象
    private UserDao userDao;

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

    @Override
    public boolean updateLoginPermission(String phoneNum, Integer loginPermission) {
        User user = userDao.findByPhonenum(phoneNum);
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
            User user = userDao.findByPhonenum(account);
            if (user == null) {
                // 如果找不到，再通过用户名查找
                user = userDao.findByUname(account);
            }
            if (user == null) {
                return false;
            }
            // 检查是否是管理员
            if (user.getAdminPermission() == 1) {
                return false; // 不允许删除管理员账号
            }
            userDao.delete(user);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除用户失败：" + e.getMessage());
        }
    }
}
