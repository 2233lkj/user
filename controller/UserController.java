/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-6
 * @description 用户注册登录管理模块
 */
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
import java.util.Set;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private UserDao userDao;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 使用电话/账号+密码登录
     *              @body{
     *              account:"电话/账号"
     *              password:"密码"
     *              }
     * @return code"200"+data+msg
     * @throws 登录失败 code:"123"
     */
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

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 注册账号
     *              @body{
     *              phoneNum:"电话"
     *              uname:"用户名"
     *              password:"密码"
     *              verifyCode:"验证码"
     *              }
     * @return code"200"+data+msg
     * @throws 注册失败 code:"123"
     */
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

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 统一发送验证码，包括注册登录
     * @param phoneNum 电话
     * @return code"200"+""+msg
     * @throws 发送验证码失败 code:"123"
     */
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

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 使用验证码登录
     *              @body{
     *              phoneNum:"电话",
     *              verifyCode:"验证码"
     *              }
     * @return code"200"+data+msg
     * @throws 登录失败 code:"123"
     */
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

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 使用验证码修改密码
     *              @body{
     *              phoneNum:"电话",
     *              verifyCode:"验证码",
     *              newPassword:"新密码"
     *              }
     * @return code"200"+data+msg
     * @throws 修改密码失败 code:"123"
     */
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

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 管理员修改用户登录权限
     *              @body{
     *              adminPhoneNum:"操作者电话"
     *              targetPhoneNum:"目标用户电话"
     *              loginPermission:"登录权限"
     *              }
     * @return code"200"+data+msg
     * @throws 修改失败 code:"123"
     */
    @PostMapping("/admin/updateLoginPermission")
    public Result<String> updateLoginPermission(@RequestBody UpdateLoginPermissionRequestDto permissionRequest) {
        try {
            // 待完善：添加无法禁用管理员账号的功能
            boolean success = userService.updateLoginPermission(permissionRequest.getAdminPhoneNum(),
                    permissionRequest.getTargetPhoneNum(), permissionRequest.getLoginPermission());
            if (success) {
                return Result.success(null, "修改用户登录权限成功");
            }
            return Result.error("123", "用户不存在");
        } catch (RuntimeException e) {
            return Result.error("123", "修改用户登录权限失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("123", "修改用户登录权限失败: " + e.getMessage());
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 注销用户
     * @param account 用户名/电话
     * @return code"200"+""+msg
     * @throws 账号注销失败 code:"123"
     */
    @DeleteMapping("/delete")
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

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 用户修改个人信息
     *              @body{
     *              account:"用户名/电话"
     *              newPhoneNum:"新电话"
     *              newUname:"新用户名"
     *              }
     * @return code"200"+data+msg
     * @throws 修改失败 code:"123"
     */
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

    /**
     * @author 潘楠
     * @date 2025-3-11
     * @description 分配用户的角色
     *              @body{
     *              adminPhoneNum:"操作者电话"
     *              targetPhoneNum:"目标用户电话"
     *              roleIds:[分配角色ID]
     *              }
     * @return code"200"+""+msg
     * @throws 分配用户角色失败 code:"123"
     */
    @PostMapping("/roles/assign")
    public Result<String> assignRoles(@RequestBody AssignRolesRequestDto request) {
        try {
            // 验证请求参数
            if (request.getAdminPhoneNum() == null || request.getAdminPhoneNum().trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (request.getTargetPhoneNum() == null || request.getTargetPhoneNum().trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }
            if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
                return Result.error("123", "角色ID列表不能为空");
            }

            userService.assignRoles(request.getAdminPhoneNum(), request.getTargetPhoneNum(), request.getRoleIds());
            return Result.success(null, "角色分配成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "角色分配失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-12
     * @description 获取用户的角色
     * @param adminPhoneNum  操作者电话
     * @param targetPhoneNum 被操作者电话
     * @return code"200"+""+msg
     * @throws 获取用户角色失败 code:"123"
     */
    @GetMapping("/roles")
    public Result<Set<Long>> getUserRoles(@RequestParam String adminPhoneNum, @RequestParam String targetPhoneNum) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (targetPhoneNum == null || targetPhoneNum.trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }

            Set<Long> roleIds = userService.getUserRoleIds(adminPhoneNum, targetPhoneNum);
            return Result.success(roleIds, "获取用户角色成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取用户角色失败：" + e.getMessage());
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-12
     * @description 删除用户的角色
     *              @body{
     *              adminPhoneNum:"操作者电话"
     *              targetPhoneNum:"目标用户电话"
     *              roleIds:[删除角色ID]
     *              }
     * @return code"200"+""+msg
     * @throws 删除用户角色失败 code:"123"
     */
    @DeleteMapping("/roles/remove")
    public Result<String> removeRoles(@RequestBody RemoveRolesRequestDto request) {
        try {
            // 验证请求参数
            if (request.getAdminPhoneNum() == null || request.getAdminPhoneNum().trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (request.getTargetPhoneNum() == null || request.getTargetPhoneNum().trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }
            if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
                return Result.error("123", "角色ID列表不能为空");
            }

            userService.removeRoles(request.getAdminPhoneNum(), request.getTargetPhoneNum(), request.getRoleIds());
            return Result.success(null, "角色移除成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "角色移除失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-19
     * @description 获取所有用户（包括禁止登录的）
     * @param adminPhoneNum 操作者电话
     * @return code"200"+data+msg
     * @throws 获取用户失败 code:"123"
     */
    @GetMapping("/all")
    public Result<List<User>> getAllUsers(@RequestParam String adminPhoneNum) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }

            List<User> users = userService.getAllUsers(adminPhoneNum);
            return Result.success(users, "获取所有用户成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取所有用户失败：系统错误");
        }
    }
}
