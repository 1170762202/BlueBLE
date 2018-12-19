package com.zlx.myapplication;

import com.zlx.myapplication.blue.BytesHexStrTranslate;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        byte[] bytes1 = BytesHexStrTranslate.toBytes("33");

        System.out.println(bytes1[0]);
        System.out.println(bytes1[1]);
        int i = bytes1[0] * 10 + bytes1[1];

        if ((bytes1[0] & 0x03) != 0) {
            System.out.println("压差过大" + (i & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 2 & 0x03) != 0) {
            System.out.println("温差过大" + (i >> 2 & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 4 & 0x03) != 0) {
            System.out.println("温度过高" + (i >> 4 & 0x03) + "级\n");
        }
        if ((bytes1[0] >> 6 & 0x03) != 0) {
            System.out.println("温度过低" + (i >> 6 & 0x03) + "级\n");
        }
    }

    private double calcTwoByte(int low, int high, double ex, int offset) {
        return (low + high * 256) * ex + offset;
    }


    // 使用两个 for 语句
    //java 合并两个byte数组
    public static byte[] byteMerger(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        int i = 0;
        for (byte bt : bt1) {
            bt3[i] = bt;
            i++;
        }

        for (byte bt : bt2) {
            bt3[i] = bt;
            i++;
        }
        return bt3;
    }


    /**
     * 16进制的字符串表示转成字节数组
     *
     * @param hexString 16进制格式的字符串
     * @return 转换后的字节数组
     **/
    public static byte[] toByteArray(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }
}