package com.xingkaichun.helloworldblockchain.core.utils;

import com.google.common.primitives.Bytes;
import com.xingkaichun.helloworldblockchain.setting.GlobalSetting;

/**
 * Bytes工具类
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class ByteUtil {

    /**
     * long转换为8个字节的字节数组(8*8=64个bit)。
     */
    public static byte[] longToBytes8(long value) {
        byte[] bytes = new byte[8];
        bytes[7] = (byte)(0xFF & (value));
        bytes[6] = (byte)(0xFF & (value >> 8));
        bytes[5] = (byte)(0xFF & (value >> 16));
        bytes[4] = (byte)(0xFF & (value >> 24));
        bytes[3] = (byte)(0xFF & (value >> 32));
        bytes[2] = (byte)(0xFF & (value >> 40));
        bytes[1] = (byte)(0xFF & (value >> 48));
        bytes[0] = (byte)(0xFF & (value >> 56));
        return bytes;
    }

    /**
     * long转换为4个字节的字节数组(4*8=32个bit)。
     */
    public static byte[] intToBytes4(int value) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte)(0xFF & (value));
        bytes[2] = (byte)(0xFF & (value >> 8));
        bytes[1] = (byte)(0xFF & (value >> 16));
        bytes[0] = (byte)(0xFF & (value >> 24));
        return bytes;
    }



    /**
     * 计算[传入字节数组]的长度，然后将长度转为4个字节的字节数组(大端)，然后将长度字节数组拼接在[传入字节数组]前，然后返回。
     */
    public static byte[] concatBytesLength(byte[] bytes) {
        return Bytes.concat(intToBytes4(bytes.length),bytes);
    }

    public static byte[] stringToBytes(String strValue) {
        return strValue.getBytes(GlobalSetting.GLOBAL_CHARSET);
    }

    public static String bytesToString(byte[] bytesValue) {
        return new String(bytesValue, GlobalSetting.GLOBAL_CHARSET);
    }

    public static byte[] longToBytes(long longValue) {
        return stringToBytes(String.valueOf(longValue));
    }

    public static long bytesToLong(byte[] bytesValue) {
        return Long.valueOf(bytesToString(bytesValue));
    }
}
