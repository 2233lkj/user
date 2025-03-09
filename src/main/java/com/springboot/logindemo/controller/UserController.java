package com.springboot.logindemo.controller;

import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.dto.*;
import com.springboot.logindemo.repository.UserDao;
import com.springboot.logindemo.service.UserService;
import com.springboot.logindemo.utils.Result;
import com.springboot.logindemo.utils.SMSUtils;
import com.springboot.logindemo.utils.ValidateCodeUtils;
import com.springboot.logindemo.utils.JwtUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private UserDao userDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/login/password")
    public Result<Map<String, Object>> loginController(@RequestBody LoginRequestDto loginRequest) {
        User user = userService.loginService(loginRequest.getAccount(), loginRequest.getPassword());
        if (user != null) {
            if (user.getLoginPermission() != 1) {
                return Result.error("123", "该账号已被禁止登录！");
            }

            String token = JwtUtils.generateToken(String.valueOf(user.getUid()));
            String redisKey = "user:" + user.getUid();
            redisTemplate.opsForValue().set(redisKey, user, 24, TimeUnit.HOURS);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);
            return Result.success(response, "登录成功");
        } else {
            return Result.error("123", "账号或密码错误！");
        }
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> registerController(@RequestBody RegisterRequestDto registerRequest,
            HttpSession session) {
        String phone = registerRequest.getPhoneNum();
        String code = registerRequest.getVerifyCode();
        String codeKey = (String) session.getAttribute(phone);

        if (codeKey == null) {
            return Result.error("123", "验证码已过期或电话号码错误");
        }
        if (!codeKey.equals(code)) {
            return Result.error("123", "验证码错误");
        }

        // 首先测试Redis连接
        try {
            String testKey = "test:redis:connection";
            redisTemplate.opsForValue().set(testKey, "test", 1, TimeUnit.SECONDS);
            redisTemplate.delete(testKey);
        } catch (Exception e) {
            return Result.error("123", "系统暂时无法提供服务，请稍后重试");
        }

        try {
            User user = userService.registerService(phone, registerRequest.getPassword(), registerRequest.getUname());
            if (user == null) {
                return Result.error("456", "用户已存在！");
            }

            String token = JwtUtils.generateToken(String.valueOf(user.getUid()));
            String redisKey = "user:" + user.getUid();

            try {
                redisTemplate.opsForValue().set(redisKey, user, 24, TimeUnit.HOURS);
            } catch (Exception e) {
                // 如果Redis操作失败，删除已创建的用户
                userService.deleteUser(phone);
                throw new RuntimeException("用户注册失败，请稍后重试");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            user.setPassword("");
            response.put("user", user);

            session.removeAttribute(phone);

            return Result.success(response, "注册成功！");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "注册失败：系统错误");
        }
    }

    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestParam String phoneNum, HttpSession session) {
        if (StringUtils.isEmpty(phoneNum)) {
            return Result.error("123", "短信发送失败");
        }
        String code = ValidateCodeUtils.generateValidateCode(4).toString();
        SMSUtils.sendMessage("验证码短信", "SMS_313071554", phoneNum, code);
        session.setAttribute(phoneNum, code);
        return Result.success(code, "验证码短信发送成功");
    }

    @PostMapping("/login/verify")
    public Result<Map<String, Object>> login(@RequestBody VerifyLoginRequestDto verifyRequest, HttpSession session) {
        try {
            String phone = verifyRequest.getPhoneNum();
            String code = verifyRequest.getVerifyCode();
            String codeKey = (String) session.getAttribute(phone);

            if (codeKey == null) {
                return Result.error("123", "验证码已过期或电话号码错误");
            }
            if (!codeKey.equals(code)) {
                return Result.error("123", "验证码错误");
            }

            User user = userService.verifyService(phone);
            if (user != null) {
                String token = JwtUtils.generateToken(String.valueOf(user.getUid()));
                String redisKey = "user:" + user.getUid();
                redisTemplate.opsForValue().set(redisKey, user, 24, TimeUnit.HOURS);

                Map<String, Object> response = new HashMap<>();
                response.put("token", token);
                response.put("user", user);

                session.removeAttribute("codeKey");
                session.removeAttribute(codeKey);

                return Result.success(response, "登录成功！");
            }
            return Result.error("123", "登录失败");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("123", "登录失败");
        }
    }

    @PostMapping("/changePassword")
    public Result<Map<String, Object>> changePassword(@RequestBody ChangePasswordRequestDto changeRequest,
            HttpSession session) {
        try {
            String phone = changeRequest.getPhoneNum();
            String code = changeRequest.getVerifyCode();
            String codeKey = (String) session.getAttribute(phone);

            if (codeKey == null) {
                return Result.error("123", "验证码已过期或电话号码错误");
            }
            if (!codeKey.equals(code)) {
                return Result.error("123", "验证码错误");
            }

            User user = userService.verifyService(phone);
            String token = JwtUtils.generateToken(String.valueOf(user.getUid()));
            String redisKey = "user:" + user.getUid();
            redisTemplate.opsForValue().set(redisKey, user, 24, TimeUnit.HOURS);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("user", user);

            boolean success = userService.updatePassword(phone, changeRequest.getNewPassword());
            if (success) {
                session.removeAttribute("codeKey");
                session.removeAttribute(codeKey);
                return Result.success(response, "密码修改成功");
            }
            return Result.error("123", "密码修改失败，用户不存在");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("123", "密码修改失败");
        }
    }

    @PostMapping("/admin/updateLoginPermission")
    public Result<String> updateLoginPermission(@RequestBody UpdateLoginPermissionRequestDto permissionRequest) {
        try {
            User admin = userService.verifyService(permissionRequest.getAdminPhoneNum());
            if (admin == null || admin.getAdminPermission() != 1) {
                return Result.error("123", "无管理员权限！");
            }

            User target = userService.verifyService(permissionRequest.getTargetPhoneNum());
            if (target.getAdminPermission() == 1) {
                return Result.error("123", "无法修改管理员账号权限！");
            }

            boolean success = userService.updateLoginPermission(permissionRequest.getTargetPhoneNum(),
                    permissionRequest.getLoginPermission());
            if (success) {
                return Result.success(null, "修改用户登录权限成功");
            }
            return Result.error("123", "用户不存在");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("123", "修改用户登录权限失败");
        }
    }

    @PostMapping("/delete")
    public Result<String> deleteUser(@RequestParam String account) {
        try {
            boolean success = userService.deleteUser(account);
            if (success) {
                User user = userService.findByAccount(account);
                if (user != null) {
                    String redisKey = "user:" + user.getUid();
                    redisTemplate.delete(redisKey);
                }
                return Result.success(null, "账号注销成功");
            }
            return Result.error("123", "账号注销失败，账号不存在或为管理员账号");
        } catch (Exception e) {
            return Result.error("123", "账号注销失败：" + e.getMessage());
        }
    }

    @PostMapping("/update")
    public Result<Map<String, Object>> updateUser(@RequestBody UpdateUserRequestDto updateRequest) {
        try {
            User user = userService.findByAccount(updateRequest.getAccount());
            if (user == null) {
                return Result.error("123", "用户不存在");
            }

            boolean hasChanges = false;

            if (updateRequest.getNewPhoneNum() != null && !updateRequest.getNewPhoneNum().equals(user.getPhonenum())) {
                if (userService.findByAccount(updateRequest.getNewPhoneNum()) != null) {
                    return Result.error("123", "该手机号已被使用");
                }
                user.setPhonenum(updateRequest.getNewPhoneNum());
                hasChanges = true;
            }

            if (updateRequest.getNewUname() != null && !updateRequest.getNewUname().equals(user.getUname())) {
                if (userService.findByAccount(updateRequest.getNewUname()) != null) {
                    return Result.error("123", "该用户名已被使用");
                }
                user.setUname(updateRequest.getNewUname());
                hasChanges = true;
            }

            if (!hasChanges) {
                return Result.error("123", "未提供需要更新的信息");
            }

            user.setUpdateTime(LocalDateTime.now());
            User updatedUser = userDao.save(user);

            String redisKey = "user:" + updatedUser.getUid();
            redisTemplate.opsForValue().set(redisKey, updatedUser, 24, TimeUnit.HOURS);

            Map<String, Object> response = new HashMap<>();
            response.put("user", updatedUser);

            return Result.success(response, "用户信息更新成功");
        } catch (Exception e) {
            return Result.error("123", "用户信息更新失败：" + e.getMessage());
        }
    }
}
