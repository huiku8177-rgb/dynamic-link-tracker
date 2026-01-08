package com.neozeng.trackerserve.util;

/**
 * @author strive_qin
 * @version 1.0
 * @description 短链接核心转换算法：10进制 ID <-> 62进制短码
 * @date 2026/1/8 14:44
 */
public class Base62Utils {

    // 包含 0-9, a-z, A-Z，共 62 个字符
    private static final String CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int SCALE = 62;

    /**
     * 编码：数字 -> 短码 (例如: 10001 -> "2vE")
     */
    public static String encode(long num) {
        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            int i = (int) (num % SCALE);
            sb.append(CHARS.charAt(i));
            num /= SCALE;
        }
        // 因为是取余法，最后需要反转字符串
        return sb.reverse().toString();
    }

    /**
     * 解码：短码 -> 数字 (例如: "2vE" -> 10001)
     */
    public static long decode(String str) {
        long num = 0;
        for (int i = 0; i < str.length(); i++) {
            num = num * SCALE + CHARS.indexOf(str.charAt(i));
        }
        return num;
    }
}