/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-17
 * @description 部门管理控制器
 */
package com.springboot.logindemo.controller;

import com.springboot.logindemo.domain.Department;
import com.springboot.logindemo.dto.AssignUserToDepartmentRequestDto;
import com.springboot.logindemo.dto.CreateDepartmentRequestDto;
import com.springboot.logindemo.service.DepartmentService;
import com.springboot.logindemo.service.UserService;
import com.springboot.logindemo.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @Resource
    private UserService userService;

    /**
     * @author 潘楠
     * @date 2025-3-17
     * @description 创建部门
     * @body {
     *       token:"操作者token"
     *       name:"部门名称"
     *       description:"部门描述"
     *       }
     * @return code"200"+data+msg
     * @throws 创建部门失败 code:"123"
     */
    @PostMapping("/create")
    public Result<Department> createDepartment(@RequestBody CreateDepartmentRequestDto request) {
        try {
            // 验证请求参数
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Result.error("123", "部门名称不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(request.getToken());
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            Department newDepartment = departmentService.createDepartment(
                    operatorPhoneNum,
                    request.getName(),
                    request.getDescription());
            return Result.success(newDepartment, "部门创建成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "部门创建失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-17
     * @description 将用户添加到部门
     * @body {
     *       token:"操作者token"
     *       targetPhoneNum:"目标用户电话"
     *       departmentIds:[部门ID]
     *       primaryDepartmentId:"主部门ID"
     *       }
     * @return code"200"+msg
     * @throws 分配部门失败 code:"123"
     */
    @PostMapping("/user/assign")
    public Result<String> assignUserToDepartment(@RequestBody AssignUserToDepartmentRequestDto request) {
        try {
            // 验证请求参数
            if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (request.getTargetPhoneNum() == null || request.getTargetPhoneNum().trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }
            if (request.getDepartmentIds() == null || request.getDepartmentIds().isEmpty()) {
                return Result.error("123", "部门ID列表不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(request.getToken());
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            departmentService.assignUserToDepartment(
                    operatorPhoneNum,
                    request.getTargetPhoneNum(),
                    request.getDepartmentIds(),
                    request.getPrimaryDepartmentId());
            return Result.success(null, "用户添加到部门成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "用户添加到部门失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-17
     * @description 获取用户的部门列表
     * @param token          操作者token
     * @param targetPhoneNum 目标用户电话
     * @return code"200"+data+msg
     * @throws 获取部门列表失败 code:"123"
     */
    @GetMapping("/user/departments")
    public Result<Set<Department>> getUserDepartments(@RequestParam String token,
            @RequestParam String targetPhoneNum) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (targetPhoneNum == null || targetPhoneNum.trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(token);
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            Set<Department> departments = departmentService.getUserDepartments(operatorPhoneNum, targetPhoneNum);
            return Result.success(departments, "获取用户部门列表成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取用户部门列表失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-17
     * @description 删除部门
     * @param token        操作者token
     * @param departmentId 部门ID
     * @return code"200"+msg
     * @throws 删除部门失败 code:"123"
     */
    @PostMapping("/delete")
    public Result<String> deleteDepartment(@RequestParam String token, @RequestParam Long departmentId) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (departmentId == null) {
                return Result.error("123", "部门ID不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(token);
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            departmentService.deleteDepartment(operatorPhoneNum, departmentId);
            return Result.success(null, "部门删除成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "删除部门失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-17
     * @description 从部门中移除用户
     * @param token          操作者token
     * @param departmentId   部门ID
     * @param targetPhoneNum 目标用户电话
     * @return code"200"+msg
     * @throws 移除用户失败 code:"123"
     */
    @DeleteMapping("/user/remove")
    public Result<String> removeUserFromDepartment(@RequestParam String token,
            @RequestParam Long departmentId,
            @RequestParam String targetPhoneNum) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (departmentId == null) {
                return Result.error("123", "部门ID不能为空");
            }
            if (targetPhoneNum == null || targetPhoneNum.trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(token);
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            departmentService.removeUserFromDepartment(operatorPhoneNum, departmentId, targetPhoneNum);
            return Result.success(null, "从部门中移除用户成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "从部门中移除用户失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-19
     * @description 重新启用已被禁用的部门
     * @param token        操作者token
     * @param departmentId 部门ID
     * @return code"200"+msg
     * @throws 启用部门失败 code:"123"
     */
    @PostMapping("/enable")
    public Result<String> enableDepartment(@RequestParam String token, @RequestParam Long departmentId) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }
            if (departmentId == null) {
                return Result.error("123", "部门ID不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(token);
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            departmentService.enableDepartment(operatorPhoneNum, departmentId);
            return Result.success(null, "部门启用成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "部门启用失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-19
     * @description 获取所有部门（包括禁用的）
     * @param token 操作者token
     * @return code"200"+data+msg
     * @throws 获取部门失败 code:"123"
     */
    @GetMapping("/all")
    public Result<List<Department>> getAllDepartments(@RequestParam String token) {
        try {
            // 验证请求参数
            if (token == null || token.trim().isEmpty()) {
                return Result.error("123", "Token不能为空");
            }

            // 获取操作者信息
            Map<String, Object> operatorInfo = userService.getUserInfoByToken(token);
            String operatorPhoneNum = (String) operatorInfo.get("phonenum");
            if (operatorPhoneNum == null || operatorPhoneNum.trim().isEmpty()) {
                return Result.error("123", "无法获取操作者电话号码");
            }

            List<Department> departments = departmentService.getAllDepartments(operatorPhoneNum);
            return Result.success(departments, "获取所有部门成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取所有部门失败：系统错误");
        }
    }
}