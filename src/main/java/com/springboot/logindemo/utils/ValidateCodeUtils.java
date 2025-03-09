package com.springboot.logindemo.utils;

import java.util.Random;

public class ValidateCodeUtils {
    /**
     * 随机生成验证码
     *
     * @param length 长度为4位或者6位
     * @return
     */
    public static Integer generateValidateCode(int length) {
        Integer code = null;

//      长度为4
        if (length == 4) {
            code = new Random().nextInt(9999);//生成随机数，最大为9999
            if (code < 1000) {
                code = code + 1000;//保证随机数为4位数字
            }
        }
        return code;
    }
}
