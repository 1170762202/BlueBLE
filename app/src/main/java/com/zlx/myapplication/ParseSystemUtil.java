package com.zlx.myapplication;

import android.text.TextUtils;

public class ParseSystemUtil {



    public static String GetCRC_XMODEM_Str(String str) {
        byte[] bytes = hexStringToBytes(str);//16进制字符串转成16进制字符串数组
        int i = CRC16_XMODEM(bytes);//进行CRC—XMODEM校验得到十进制校验数
        String CRC = Integer.toHexString(i);//10进制转16进制
        return CRC;
    }
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }
    public static int CRC16_XMODEM(byte[] buffer) {
        /* StringUtil.getByteArrayByString();*/

        int wCRCin = 0x0000; // initial value 65535
        int wCPoly = 0x1021; // 0001 0000 0010 0001 (0, 5, 12)
        for (byte b : buffer) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((wCRCin >> 15 & 1) == 1);
                wCRCin <<= 1;
                if (c15 ^ bit)
                    wCRCin ^= wCPoly;
            }
        }
        wCRCin &= 0xffff;
        return wCRCin ^= 0x0000;
    }
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d;
        //转换的时候应该注意的是单双数的情况，网上很多都只处理了双数的情况，也就是默认刚好填满，这样16进制字符串长度是单数的话会丢失掉一个字符 因为length/2 是舍去余数的
        if (hexString.length() % 2 != 0) {// 16进制字符串长度是单数
            length = length + 1;
            d = new byte[length];
            // 这里把byte数组从后往前填，字符串也是翻转着填的，最后会空出byte数组的第一个（用来填充我们单出来的那个字符）
            for (int i = length - 1; i > 0; i--) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) | charToByte(hexChars[pos - 1]) << 4);
            }
            d[0] = charToByte(hexChars[0]);
        } else {// 双数情况
            d = new byte[length];
            for (int i = 0; i < length; i++) {
                int pos = i * 2;
                d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            }
        }
        return d;
    }

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
