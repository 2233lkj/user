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
import com.springboot.logindemo.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    /**
     * @author 潘楠
     * @date 2025-3-17
     * @description 创建部门
     * @body {
     *       adminPhoneNum:"操作者电话"
     *       name:"部门名称"
     *       description:"部门描述"
     *       }
     * @return code"200"+data+msg
     * @throws 创建部门失败 code:"123"
     */
    @PostMapping("/create")
    public Result<Department> createDepartment(@RequestBody CreateDepartmentRequestDto request) {
        try {
            System.out.println("接收到创建部门请求: adminPhoneNum=" + request.getAdminPhoneNum());

            // 验证请求参数
            if (request.getAdminPhoneNum() == null || request.getAdminPhoneNum().trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }

            if (request.getName() == null || request.getName().trim().isEmpty()) {
                System.out.println("部门名称为空");
                return Result.error("123", "部门名称不能为空");
            }

            System.out.println("开始创建部门: " + request.getName());
            Department newDepartment = departmentService.createDepartment(
                    request.getAdminPhoneNum(),
                    request.getName(),
                    request.getDescription());
            System.out.println("部门创建成功: " + newDepartment.getId());
            return Result.success(newDepartment, "部门创建成功");
        } catch (RuntimeException e) {
            System.out.println("创建部门失败(RuntimeException): " + e.getMessage());
            e.printStackTrace();
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            System.out.println("创建部门失败(Exception): " + e.getMessage());
            e.printStackTrace();
            return Result.error("123", "部门创建失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-17
     * @description 将用户添加到部门
     * @body {
     *       adminPhoneNum:"操作者电话"
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
            if (request.getAdminPhoneNum() == null || request.getAdminPhoneNum().trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (request.getTargetPhoneNum() == null || request.getTargetPhoneNum().trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }
            if (request.getDepartmentIds() == null || request.getDepartmentIds().isEmpty()) {
                return Result.error("123", "部门ID列表不能为空");
            }

            departmentService.assignUserToDepartment(
                    request.getAdminPhoneNum(),
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
     * @param adminPhoneNum  操作者电话
     * @param targetPhoneNum 目标用户电话
     * @return code"200"+data+msg
     * @throws 获取部门列表失败 code:"123"
     */
    @GetMapping("/user/departments")
    public Result<Set<Department>> getUserDepartments(@RequestParam String adminPhoneNum,
            @RequestParam String targetPhoneNum) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (targetPhoneNum == null || targetPhoneNum.trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }

            Set<Department> departments = departmentService.getUserDepartments(adminPhoneNum, targetPhoneNum);
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
     * @param adminPhoneNum 操作者电话
     * @param departmentId  部门ID
     * @return code"200"+msg
     * @throws 删除部门失败 code:"123"
     */
    @DeleteMapping("/delete")
    public Result<String> deleteDepartment(@RequestParam String adminPhoneNum, @RequestParam Long departmentId) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (departmentId == null) {
                return Result.error("123", "部门ID不能为空");
            }

            departmentService.deleteDepartment(adminPhoneNum, departmentId);
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
     * @param adminPhoneNum  操作者电话
     * @param departmentId   部门ID
     * @param targetPhoneNum 目标用户电话
     * @return code"200"+msg
     * @throws 移除用户失败 code:"123"
     */
    @DeleteMapping("/user/remove")
    public Result<String> removeUserFromDepartment(@RequestParam String adminPhoneNum,
            @RequestParam Long departmentId,
            @RequestParam String targetPhoneNum) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (departmentId == null) {
                return Result.error("123", "部门ID不能为空");
            }
            if (targetPhoneNum == null || targetPhoneNum.trim().isEmpty()) {
                return Result.error("123", "目标用户账号不能为空");
            }

            departmentService.removeUserFromDepartment(adminPhoneNum, departmentId, targetPhoneNum);
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
     * @param adminPhoneNum 操作者电话
     * @param departmentId  部门ID
     * @return code"200"+msg
     * @throws 启用部门失败 code:"123"
     */
    @PostMapping("/enable")
    public Result<String> enableDepartment(@RequestParam String adminPhoneNum, @RequestParam Long departmentId) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (departmentId == null) {
                return Result.error("123", "部门ID不能为空");
            }

            departmentService.enableDepartment(adminPhoneNum, departmentId);
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
     * @param adminPhoneNum 操作者电话
     * @return code"200"+data+msg
     * @throws 获取部门失败 code:"123"
     */
    @GetMapping("/all")
    public Result<List<Department>> getAllDepartments(@RequestParam String adminPhoneNum) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }

            List<Department> departments = departmentService.getAllDepartments(adminPhoneNum);
            return Result.success(departments, "获取所有部门成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取所有部门失败：系统错误");
        }
    }
}