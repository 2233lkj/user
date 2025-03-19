/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 用户登录信息
 */
package com.springboot.logindemo.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String account;
    private String password;
}