/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-11
 * @description 修改用户信息
 */
package com.springboot.logindemo.dto;

public class UpdateUserRequestDto {
    private String account;
    private String newPhoneNum;
    private String newUname;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
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
}