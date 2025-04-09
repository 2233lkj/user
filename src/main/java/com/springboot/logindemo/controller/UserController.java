/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-6
 * @description 用户注册登录管理模块
 */
package com.springboot.logindemo.controller;

import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.domain.Role;
import com.springboot.logindemo.domain.Department;
import com.springboot.logindemo.dto.*;
import com.springboot.logindemo.repository.UserDao;
import com.springboot.logindemo.repository.RoleDao;
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
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private UserDao userDao;

    @Resource
    private RoleDao roleDao;

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

            // 检查是否请求以管理员身份登录
            Boolean isAdmin = loginRequest.getIsAdmin();
            if (isAdmin != null && isAdmin) {
                // 如果要求管理员登录，检查是否有管理员角色(ID为1)
                boolean hasAdminRole = userService.hasRoleByAccount(loginRequest.getAccount(), 1L);
                if (!hasAdminRole) {
                    return Result.error("403", "您没有管理员权限，无法以管理员身份登录！");
                }
            }

            // 从数据库重新获取最新的用户信息
            user = userDao.findById(user.getUid()).orElse(null);
            if (user == null) {
                return Result.error("123", "用户不存在");
            }

            String token = JwtUtils.generateToken(String.valueOf(user.getUid()));
            String redisKey = "user:" + user.getUid();
            // 删除旧的缓存
            redisTemplate.delete(redisKey);
            // 设置新的缓存
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

        // 验证两次输入的密码是否一致
        if (!registerRequest.getPassword().equals(registerRequest.getPasswordAgain())) {
            return Result.error("123", "密码输入不一致");
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
                if (user.getLoginPermission() != 1) {
                    return Result.error("123", "该账号已被禁止登录！");
                }

                // 检查是否请求以管理员身份登录
                Boolean isAdmin = verifyRequest.getIsAdmin();
                if (isAdmin != null && isAdmin) {
                    // 如果要求管理员登录，检查是否有管理员角色(ID为1)
                    boolean hasAdminRole = userService.hasRoleByAccount(phone, 1L);
                    if (!hasAdminRole) {
                        return Result.error("403", "您没有管理员权限，无法以管理员身份登录！");
                    }
                }

                // 从数据库重新获取最新的用户信息
                user = userDao.findById(user.getUid()).orElse(null);
                if (user == null) {
                    return Result.error("123", "用户不存在");
                }

                String token = JwtUtils.generateToken(String.valueOf(user.getUid()));
                String redisKey = "user:" + user.getUid();
                // 删除旧的缓存
                redisTemplate.delete(redisKey);
                // 设置新的缓存
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
     * @description 禁用用户登录权限
     * @param token          操作者token
     * @param targetPhoneNum 目标用户电话
     * @return code"200"+data+msg
     * @throws 禁用失败 code:"123"
     */
    @PostMapping("/disableLoginPermission")
    public Result<String> disableLoginPermission(@RequestParam String token, @RequestParam String targetPhoneNum) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (targetPhoneNum == null || targetPhoneNum.trim().isEmpty()) {
                return Result.error("123", "目标用户电话不能为空");
            }

            boolean success = userService.updateLoginPermission(token, targetPhoneNum, 0);

            if (success) {
                return Result.success(null, "禁用用户登录权限成功");
            }
            return Result.error("123", "禁用用户登录权限失败");
        } catch (RuntimeException e) {
            return Result.error("123", "禁用用户登录权限失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("123", "禁用用户登录权限失败: " + e.getMessage());
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 启用用户登录权限
     * @param token          操作者token
     * @param targetPhoneNum 目标用户电话
     * @return code"200"+data+msg
     * @throws 启用失败 code:"123"
     */
    @PostMapping("/enableLoginPermission")
    public Result<String> enableLoginPermission(@RequestParam String token, @RequestParam String targetPhoneNum) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (targetPhoneNum == null || targetPhoneNum.trim().isEmpty()) {
                return Result.error("123", "目标用户电话不能为空");
            }

            boolean success = userService.updateLoginPermission(token, targetPhoneNum, 1);

            if (success) {
                return Result.success(null, "启用用户登录权限成功");
            }
            return Result.error("123", "启用用户登录权限失败");
        } catch (RuntimeException e) {
            return Result.error("123", "启用用户登录权限失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("123", "启用用户登录权限失败: " + e.getMessage());
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-6
     * @description 注销用户
     * @param token:"用户token"
     * @return code"200"+data+msg
     * @throws 注销失败 code:"123"
     */
    @DeleteMapping("/delete")
    public Result<String> deleteUser(@RequestParam String token) {
        try {
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }

            // 获取用户信息
            Map<String, Object> userInfo = userService.getUserInfoByToken(token);
            String phoneNum = (String) userInfo.get("phonenum");
            if (phoneNum == null || phoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取用户电话号码");
            }

            boolean success = userService.deleteUser(phoneNum);
            if (success) {
                // 获取用户信息用于删除Redis缓存
                Long uid = (Long) userInfo.get("uid");
                if (uid != null) {
                    String redisKey = "user:" + uid;
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
     *              token:"用户token"
     *              newPhoneNum:"新电话"
     *              newUname:"新用户名"
     *              verifyCode:"验证码"
     *              }
     * @return code"200"+data+msg
     * @throws 修改失败 code:"123"
     */
    @PostMapping("/update")
    public Result<Map<String, Object>> updateUser(@RequestBody UpdateUserRequestDto updateRequest,
            HttpSession session) {
        try {
            if (updateRequest.getToken() == null || updateRequest.getToken().trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }

            // 获取用户信息
            Map<String, Object> userInfo = userService.getUserInfoByToken(updateRequest.getToken());
            if (userInfo == null) {
                return Result.error("123", "无效的token");
            }

            Long uid = ((Number) userInfo.get("uid")).longValue();
            String currentPhoneNum = (String) userInfo.get("phonenum");

            if (currentPhoneNum == null || currentPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取用户电话号码");
            }

            // 直接从数据库获取用户
            User user = userDao.findById(uid).orElse(null);
            if (user == null) {
                return Result.error("123", "用户不存在");
            }

            boolean hasChanges = false;

            // 验证新手机号
            if (updateRequest.getNewPhoneNum() != null && !updateRequest.getNewPhoneNum().equals(user.getPhonenum())) {
                // 检查新手机号是否已被其他用户使用
                User existingUser = userDao.findByPhonenum(updateRequest.getNewPhoneNum());
                if (existingUser != null && existingUser.getUid() != user.getUid()) {
                    return Result.error("123", "该手机号已被使用");
                }

                // 验证新手机号的验证码
                String code = updateRequest.getVerifyCode();
                if (code == null || code.trim().isEmpty()) {
                    return Result.error("123", "验证码不能为空");
                }

                String codeKey = (String) session.getAttribute(updateRequest.getNewPhoneNum());
                if (codeKey == null) {
                    return Result.error("123", "验证码已过期或电话号码错误");
                }
                if (!codeKey.equals(code)) {
                    return Result.error("123", "验证码错误");
                }

                user.setPhonenum(updateRequest.getNewPhoneNum());
                hasChanges = true;

                // 清除验证码
                session.removeAttribute(updateRequest.getNewPhoneNum());
            }

            // 修改用户名
            if (updateRequest.getNewUname() != null && !updateRequest.getNewUname().equals(user.getUname())) {
                // 检查新用户名是否已被其他用户使用
                User existingUser = userDao.findByUname(updateRequest.getNewUname());
                if (existingUser != null && existingUser.getUid() != user.getUid()) {
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

            // 生成新的token
            String newToken = JwtUtils.generateToken(String.valueOf(updatedUser.getUid()));

            // 更新Redis中的用户信息
            String redisKey = "user:" + updatedUser.getUid();
            redisTemplate.delete(redisKey); // 先删除旧的缓存
            redisTemplate.opsForValue().set(redisKey, updatedUser, 24, TimeUnit.HOURS);

            // 构建返回的用户信息
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("uid", updatedUser.getUid());
            userMap.put("uname", updatedUser.getUname());
            userMap.put("phonenum", updatedUser.getPhonenum());
            userMap.put("loginPermission", updatedUser.getLoginPermission());
            userMap.put("adminPermission", updatedUser.getAdminPermission());
            userMap.put("createTime", updatedUser.getCreateTime());
            userMap.put("updateTime", updatedUser.getUpdateTime());
            userMap.put("roles", updatedUser.getRoles());

            response.put("user", userMap);
            response.put("token", newToken);

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
     *              token:"操作者token"
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
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (request.getTargetPhoneNum() == null || request.getTargetPhoneNum().trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }
            if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
                return Result.error("123", "角色ID列表不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(request.getToken());
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            userService.assignRoles(operatorPhoneNum, request.getTargetPhoneNum(), request.getRoleIds());
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
     * @param token 用户的token
     * @return code"200"+""+msg
     * @throws 获取用户角色失败 code:"123"
     */
    @GetMapping("/roles")
    public Result<List<String>> getUserRoles(@RequestParam String token) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }

            // 获取用户信息
            Map<String, Object> userInfo = userService.getUserInfoByToken(token);
            String phoneNum = (String) userInfo.get("phonenum");
            if (phoneNum == null || phoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取用户电话号码");
            }

            // 获取用户角色ID
            Set<Long> roleIds = userService.getUserRoleIds(phoneNum, phoneNum);

            // 获取角色名称
            List<String> roleNames = new ArrayList<>();
            for (Long roleId : roleIds) {
                Role role = roleDao.findById(roleId).orElse(null);
                if (role != null) {
                    roleNames.add(role.getName());
                }
            }

            return Result.success(roleNames, "获取用户角色成功");
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
     *              token:"操作者token"
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
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (request.getTargetPhoneNum() == null || request.getTargetPhoneNum().trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }
            if (request.getRoleIds() == null || request.getRoleIds().isEmpty()) {
                return Result.error("123", "角色ID列表不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(request.getToken());
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            userService.removeRoles(operatorPhoneNum, request.getTargetPhoneNum(), request.getRoleIds());
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
     * @param token 用户的JWT令牌
     * @return code"200"+data+msg
     * @throws 获取用户失败 code:"123"
     */
    @GetMapping("/all")
    public Result<List<User>> getAllUsers(@RequestParam String token) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }

            List<User> users = userService.getAllUsers(token);
            return Result.success(users, "获取所有用户成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取所有用户失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-23
     * @description 根据token获取用户信息
     * @param token JWT令牌
     * @return code"200"+data+msg
     * @throws 获取用户信息失败 code:"123"
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo(@RequestParam String token) {
        try {
            Map<String, Object> userInfo = userService.getUserInfoByToken(token);
            return Result.success(userInfo, "获取用户信息成功");
        } catch (Exception e) {
            e.printStackTrace(); // 开发环境打印堆栈跟踪，生产环境可以考虑移除
            return Result.error("123", "获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-23
     * @description 根据token获取用户角色和权限信息
     * @param token JWT令牌
     * @return code"200"+data+msg
     * @throws 获取用户角色权限失败 code:"123"
     */
    @GetMapping("/roles/permissions")
    public Result<UserRolePermissionDto> getUserRolePermission(@RequestParam String token) {
        try {
            UserRolePermissionDto dto = userService.getUserRolePermissionByToken(token);
            return Result.success(dto, "获取用户角色和权限成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("123", "获取用户角色和权限失败：" + e.getMessage());
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-24
     * @description 获取用户的权限ID列表
     * @param token 用户的token
     * @return code"200"+data+msg
     * @throws 获取用户权限失败 code:"123"
     */
    @GetMapping("/permissions")
    public Result<List<Long>> getUserPermissions(@RequestParam String token) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }

            // 获取用户信息
            Map<String, Object> userInfo = userService.getUserInfoByToken(token);
            String phoneNum = (String) userInfo.get("phonenum");
            if (phoneNum == null || phoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取用户电话号码");
            }

            // 获取用户角色权限
            UserRolePermissionDto userRolePermission = userService.getUserRolePermissionByToken(token);

            // 提取权限ID
            List<Long> permissionIds = new ArrayList<>();
            if (userRolePermission != null && userRolePermission.getRoles() != null) {
                for (Map<String, Object> role : userRolePermission.getRoles()) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> permissions = (List<Map<String, Object>>) role.get("permissions");
                    if (permissions != null) {
                        for (Map<String, Object> permission : permissions) {
                            // 获取权限ID
                            Number id = (Number) permission.get("id");
                            if (id != null) {
                                permissionIds.add(id.longValue());
                            }
                        }
                    }
                }
            }

            return Result.success(permissionIds, "获取用户权限成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取用户权限失败：" + e.getMessage());
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-24
     * @description 根据手机号获取用户信息
     * @param token          操作者的JWT令牌
     * @param targetPhoneNum 目标用户的手机号
     * @return code"200"+data+msg
     * @throws 获取用户信息失败 code:"123"
     */
    @GetMapping("/info/phone")
    public Result<Map<String, Object>> getUserInfoByPhone(@RequestParam String token,
            @RequestParam String targetPhoneNum) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (targetPhoneNum == null || targetPhoneNum.trim().isEmpty()) {
                return Result.error("123", "目标用户手机号不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(token);
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            // 验证操作者是否为管理员
            if (!userService.hasRoleByAccount(operatorPhoneNum, 1L)) {
                return Result.error("123", "您没有访问此资源的权限");
            }

            // 查找目标用户
            User targetUser = userService.findByAccount(targetPhoneNum);
            if (targetUser == null) {
                return Result.error("123", "目标用户不存在");
            }

            // 构建返回的用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("uid", targetUser.getUid());
            userInfo.put("uname", targetUser.getUname());
            userInfo.put("phonenum", targetUser.getPhonenum());
            userInfo.put("loginPermission", targetUser.getLoginPermission());
            userInfo.put("adminPermission", targetUser.getAdminPermission());
            userInfo.put("createTime", targetUser.getCreateTime());
            userInfo.put("updateTime", targetUser.getUpdateTime());
            userInfo.put("roles", targetUser.getRoles());

            // 添加部门信息
            Set<Department> departments = targetUser.getDepartments();
            if (departments != null && !departments.isEmpty()) {
                List<Map<String, Object>> deptList = departments.stream()
                        .filter(dept -> dept.getActive() == 1) // 只返回活跃的部门
                        .map(dept -> {
                            Map<String, Object> deptMap = new HashMap<>();
                            deptMap.put("id", dept.getId());
                            deptMap.put("name", dept.getName());
                            deptMap.put("description", dept.getDescription());
                            // 标记是否为主部门
                            deptMap.put("isPrimary", dept.equals(targetUser.getPrimaryDepartment()));
                            return deptMap;
                        })
                        .collect(Collectors.toList());

                userInfo.put("departments", deptList);
            }

            // 获取主部门信息
            Department primaryDept = targetUser.getPrimaryDepartment();
            if (primaryDept != null && primaryDept.getActive() == 1) {
                Map<String, Object> primaryDeptMap = new HashMap<>();
                primaryDeptMap.put("id", primaryDept.getId());
                primaryDeptMap.put("name", primaryDept.getName());
                primaryDeptMap.put("description", primaryDept.getDescription());
                userInfo.put("primaryDepartment", primaryDeptMap);
            }

            return Result.success(userInfo, "获取用户信息成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("123", "获取用户信息失败：" + e.getMessage());
        }
    }
}
