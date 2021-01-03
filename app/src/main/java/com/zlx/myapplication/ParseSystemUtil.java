package com.zlx.myapplication;

import android.text.TextUtils;

public class ParseSystemUtil {
    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        int i = Integer.parseInt(hexStr, 16);
        String s = Integer.toBinaryString(i);
        String val = "";
        for (int j = 0; j < 8 - s.length(); j++) {
            val = "0" + val;
        }
        if (!TextUtils.isEmpty(val)) {
            s = val + s;
        }
        System.out.println("parseHexStr2Byte=" + s);
        byte[] bytes = s.getBytes();
        for (int j = 0; j < bytes.length; j++) {
            bytes[j] = (byte) (bytes[j] & 0x01);
        }
        return bytes;
    }

    public static String asciiToString(String value) {
        StringBuffer sbu = new StringBuffer();
        String[] chars = value.split(",");
        for (int i = 0; i < chars.length; i++) {
            sbu.append((char) Integer.parseInt(chars[i]));
        }
        return sbu.toString();
    }
}
