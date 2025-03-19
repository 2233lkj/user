/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 修改用户密码
 */
package com.springboot.logindemo.dto;

public class ChangePasswordRequestDto {
    private String phoneNum;
    private String verifyCode;
    private String newPassword;

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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}