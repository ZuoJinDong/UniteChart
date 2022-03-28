package com.zjd.demo.net;

import android.annotation.SuppressLint;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptorUtil {

    private static final String seed = "logan";
    private static final String HEX = "0123456789ABCDEF";

    /**
     * 字符Base64加密
     */
    public static String encodeToString(String str) {
        try {
            return Base64.encodeToString(str.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符Base64解密
     */
    public static String decodeToString(String str) {
        try {
            return new String(Base64.decode(str.getBytes("UTF-8"), Base64.DEFAULT));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * AES加密
     */
    public static String encrypt(String cleartext) {
        byte[] rawKey;
        byte[] result = null;
        boolean isError = false;

        try {
            rawKey = getRawKey(seed.getBytes());
            result = encrypt(rawKey, cleartext.getBytes());
        } catch (Exception e) {
            isError = true;
            e.printStackTrace();
        }

        if (isError) {
            return "";
        }

        return toHex(result);
    }

    /**
     * AES解密
     */
    public static String decrypt(String encrypted) {
        byte[] rawKey;
        byte[] enc;
        byte[] result = null;
        boolean isError = false;

        try {
            rawKey = getRawKey(seed.getBytes());
            enc = toByte(encrypted);
            result = decrypt(rawKey, enc);
        } catch (Exception e) {
            isError = true;
            e.printStackTrace();
        }

        if (isError) {
            return "";
        }

        return new String(result);
    }

    private static byte[] getRawKey(byte[] seed) throws Exception {
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        @SuppressLint("DeletedProvider") SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(seed);
        kgen.init(128, sr);// 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        return raw;
    }

    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    private static byte[] decrypt(byte[] raw, byte[] encrypted)
            throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }


    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];

        for (int i = 0; i < len; i++) {
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2),
                    16).byteValue();
        }

        return result;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b >> 4) & 0x0f)).append(HEX.charAt(b & 0x0f));
    }

    public static String dncrypt(String S, char Key) // 加密函数
    {
        char C1 = 52845;
        char C2 = 22719;
        StringBuilder Result = new StringBuilder();
        StringBuilder str = new StringBuilder();
        int i = 0;
        int j = 0;
        for (i = 0; i < S.length(); i++) // 依次对字符串中各字符进行操作
        {
            Result.append((char) (S.charAt(i) ^ (Key >> 8))); // 将密钥移位后与字符异或
            Key = (char) ((Result.charAt(i) + Key) * C1 + C2); // 产生下一个密钥
        }
        S = Result.toString(); // 保存结果
        Result = new StringBuilder();
        for (i = 0; i < S.length(); i++) // 对加密结果进行转换
        {
            j = S.charAt(i); // 提取字符
            // 将字符转换为两个字母保存
            str.append("12"); // 设置str长度为2
            str.setCharAt(0, (char) (65 + j / 26));// 这里将65改大点的数例如256，密文就会变乱码，效果更好，相应的，解密处要改为相同的数
            str.setCharAt(1, (char) (65 + j % 26));
            Result.append(str.toString());
            str = new StringBuilder();
        }
        return Result.toString();
    }

    public static String decrypt(String S, char Key) // 解密函数
    {
        char C1 = 52845;
        char C2 = 22719;
        StringBuilder Result = new StringBuilder();
        StringBuilder str = new StringBuilder();
        int i = 0;
        int j = 0;
        for (i = 0; i < S.length() / 2; i++) // 将字符串两个字母一组进行处理
        {
            j = (S.charAt(2 * i) - 65) * 26;// 相应的，解密处要改为相同的数
            j += S.charAt(2 * i + 1) - 65;
            str.append("1"); // 设置str长度为1
            str.setCharAt(0, (char) j);
            Result.append(str.toString()); // 追加字符，还原字符串
            str = new StringBuilder();
        }
        S = Result.toString(); // 保存中间结果
        for (i = 0; i < S.length(); i++) // 依次对字符串中各字符进行操作
        {
            Result.setCharAt(i, (char) (S.charAt(i) ^ (Key >> 8))); // 将密钥移位后与字符异或
            Key = (char) ((S.charAt(i) + Key) * C1 + C2); // 产生下一个密钥
        }
        return Result.toString();
    }

    private static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * SHA1加密
     *
     * @param data 明文字符串
     * @return 16进制密文
     */
    public static String encryptSHA1ToString(String data) {
        return encryptSHA1ToString(data.getBytes());
    }

    /**
     * SHA1加密
     *
     * @param data 明文字节数组
     * @return 16进制密文
     */
    public static String encryptSHA1ToString(byte[] data) {
        return bytes2HexString(encryptSHA1(data));
    }

    /**
     * SHA1加密
     *
     * @param data 明文字节数组
     * @return 密文字节数组
     */
    public static byte[] encryptSHA1(byte[] data) {
        return hashTemplate(data, "SHA1");
    }

    /**
     * hash加密模板
     *
     * @param data      数据
     * @param algorithm 加密算法
     * @return 密文字节数组
     */
    private static byte[] hashTemplate(byte[] data, String algorithm) {
        if (data == null || data.length <= 0) return null;
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(data);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * byteArr转hexString
     * <p>例如：</p>
     * bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns 00A8
     *
     * @param bytes 字节数组
     * @return 16进制大写字符串
     */
    public static String bytes2HexString(byte[] bytes) {
        if (bytes == null) return null;
        int len = bytes.length;
        if (len <= 0) return null;
        char[] ret = new char[len << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
        }
        return new String(ret);
    }

}
