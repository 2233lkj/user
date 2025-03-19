/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-12
 * @description 角色权限模块
 */
package com.springboot.logindemo.controller;

import com.springboot.logindemo.dto.AssignPermissionsRequestDto;
import com.springboot.logindemo.dto.RemovePermissionsRequestDto;
import com.springboot.logindemo.service.RolePermissionService;
import com.springboot.logindemo.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import com.springboot.logindemo.dto.CreateRoleRequestDto;
import com.springboot.logindemo.domain.Role;
import com.springboot.logindemo.domain.Permission;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/role")
public class RolePermissionController {

    @Resource
    private RolePermissionService rolePermissionService;

    /**
     * @author 潘楠
     * @date 2025-3-12
     * @description 为角色分配权限
     * @body {
     *       adminPhoneNum:"操作者电话"
     *       roleId:"操作角色ID"
     *       permissionIds:[权限ID]
     *       }
     * @return code"200"+msg
     * @throws 权限分配失败 code:"123"
     */
    @PostMapping("/permissions/assign")
    public Result<String> assignPermissions(@RequestBody AssignPermissionsRequestDto request) {
        try {
            // 验证请求参数
            if (request.getAdminPhoneNum() == null || request.getAdminPhoneNum().trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (request.getRoleId() == null) {
                return Result.error("123", "角色ID不能为空");
            }
            if (request.getPermissionIds() == null || request.getPermissionIds().isEmpty()) {
                return Result.error("123", "权限ID列表不能为空");
            }

            rolePermissionService.assignPermissions(request.getAdminPhoneNum(), request.getRoleId(),
                    request.getPermissionIds());
            return Result.success(null, "权限分配成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "权限分配失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-12
     * @description 获取角色权限
     * @param adminPhoneNum 操作者电话
     * @param roleId        操作角色ID
     * @return code"200"+msg
     * @throws 获取权限失败 code:"123"
     */
    @GetMapping("/permissions")
    public Result<Set<Long>> getRolePermissions(@RequestParam String adminPhoneNum, @RequestParam Long roleId) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (roleId == null) {
                return Result.error("123", "角色ID不能为空");
            }

            Set<Long> permissionIds = rolePermissionService.getRolePermissionIds(adminPhoneNum, roleId);
            return Result.success(permissionIds, "获取角色权限成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取角色权限失败：" + e.getMessage());
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-12
     * @description 为角色移除权限
     * @body {
     *       adminPhoneNum:"操作者电话"
     *       roleId:"操作角色ID"
     *       permissionIds:[权限ID]
     *       }
     * @return code"200"+msg
     * @throws 权限移除失败 code:"123"
     */
    @DeleteMapping("/permissions/remove")
    public Result<String> removePermissions(@RequestBody RemovePermissionsRequestDto request) {
        try {
            // 验证请求参数
            if (request.getAdminPhoneNum() == null || request.getAdminPhoneNum().trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (request.getRoleId() == null) {
                return Result.error("123", "角色ID不能为空");
            }
            if (request.getPermissionIds() == null || request.getPermissionIds().isEmpty()) {
                return Result.error("123", "权限ID列表不能为空");
            }

            rolePermissionService.removePermissions(request.getAdminPhoneNum(), request.getRoleId(),
                    request.getPermissionIds());
            return Result.success(null, "权限移除成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "权限移除失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-13
     * @description 创建新角色
     * @body {
     *       adminPhoneNum:"操作者电话"
     *       name:"角色名称"
     *       description:"角色描述"
     *       permissionIds:[权限ID]
     *       }
     * @return code"200"+msg
     * @throws 创建角色失败 code:"123"
     */
    @PostMapping("/create")
    public Result<Role> createRole(@RequestBody CreateRoleRequestDto request) {
        try {
            // 验证请求参数
            if (request.getAdminPhoneNum() == null || request.getAdminPhoneNum().trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Result.error("123", "角色名称不能为空");
            }

            Role role = rolePermissionService.createRole(
                    request.getAdminPhoneNum(),
                    request.getName(),
                    request.getDescription(),
                    request.getPermissionIds());
            return Result.success(role, "角色创建成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "角色创建失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-18
     * @description 删除角色
     * @param adminPhoneNum 操作者电话
     * @param roleId        角色ID
     * @return code"200"+msg
     * @throws 删除角色失败 code:"123"
     */
    @DeleteMapping("/delete")
    public Result<String> deleteRole(@RequestParam String adminPhoneNum, @RequestParam Long roleId) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (roleId == null) {
                return Result.error("123", "角色ID不能为空");
            }

            rolePermissionService.deleteRole(adminPhoneNum, roleId);
            return Result.success(null, "角色删除成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "角色删除失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-19
     * @description 重新启用已被禁用的角色
     * @param adminPhoneNum 操作者电话
     * @param roleId        角色ID
     * @return code"200"+msg
     * @throws 启用角色失败 code:"123"
     */
    @PostMapping("/enable")
    public Result<String> enableRole(@RequestParam String adminPhoneNum, @RequestParam Long roleId) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (roleId == null) {
                return Result.error("123", "角色ID不能为空");
            }

            rolePermissionService.enableRole(adminPhoneNum, roleId);
            return Result.success(null, "角色启用成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "角色启用失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-19
     * @description 禁用权限
     * @param adminPhoneNum 操作者电话
     * @param permissionId  权限ID
     * @return code"200"+msg
     * @throws 禁用权限失败 code:"123"
     */
    @DeleteMapping("/permission/disable")
    public Result<String> disablePermission(@RequestParam String adminPhoneNum, @RequestParam Long permissionId) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (permissionId == null) {
                return Result.error("123", "权限ID不能为空");
            }

            rolePermissionService.disablePermission(adminPhoneNum, permissionId);
            return Result.success(null, "权限禁用成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "权限禁用失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-19
     * @description 启用权限
     * @param adminPhoneNum 操作者电话
     * @param permissionId  权限ID
     * @return code"200"+msg
     * @throws 启用权限失败 code:"123"
     */
    @PostMapping("/permission/enable")
    public Result<String> enablePermission(@RequestParam String adminPhoneNum, @RequestParam Long permissionId) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }
            if (permissionId == null) {
                return Result.error("123", "权限ID不能为空");
            }

            rolePermissionService.enablePermission(adminPhoneNum, permissionId);
            return Result.success(null, "权限启用成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "权限启用失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-19
     * @description 获取所有角色（包括禁用的）
     * @param adminPhoneNum 操作者电话
     * @return code"200"+data+msg
     * @throws 获取角色失败 code:"123"
     */
    @GetMapping("/all")
    public Result<List<Role>> getAllRoles(@RequestParam String adminPhoneNum) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }

            List<Role> roles = rolePermissionService.getAllRoles(adminPhoneNum);
            return Result.success(roles, "获取所有角色成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取所有角色失败：系统错误");
        }
    }

    /**
     * @author 潘楠
     * @date 2025-3-19
     * @description 获取所有权限（包括禁用的）
     * @param adminPhoneNum 操作者电话
     * @return code"200"+data+msg
     * @throws 获取权限失败 code:"123"
     */
    @GetMapping("/permission/all")
    public Result<List<Permission>> getAllPermissions(@RequestParam String adminPhoneNum) {
        try {
            // 验证请求参数
            if (adminPhoneNum == null || adminPhoneNum.trim().isEmpty()) {
                return Result.error("123", "管理员账号不能为空");
            }

            List<Permission> permissions = rolePermissionService.getAllPermissions(adminPhoneNum);
            return Result.success(permissions, "获取所有权限成功");
        } catch (RuntimeException e) {
            return Result.error("123", e.getMessage());
        } catch (Exception e) {
            return Result.error("123", "获取所有权限失败：系统错误");
        }
    }
}