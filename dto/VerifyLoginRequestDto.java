/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 使用验证码登录
 */
package com.springboot.logindemo.dto;

public class VerifyLoginRequestDto {
    private String phoneNum;
    private String verifyCode;

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}