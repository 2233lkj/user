/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 修改用户信息
 */
package com.springboot.logindemo.dto;

public class UpdateUserRequestDto {
    private String token;
    private String newPhoneNum;
    private String newUname;
    private String verifyCode;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewPhoneNum() {
        return newPhoneNum;
    }

    public void setNewPhoneNum(String newPhoneNum) {
        this.newPhoneNum = newPhoneNum;
    }

    public String getNewUname() {
        return newUname;
    }

    public void setNewUname(String newUname) {
        this.newUname = newUname;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}