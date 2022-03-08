package com.xl.websocket;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import com.xl.util.ByteBuffer;

public class WebUpgradleUtil {
	
	/**
     * Base64
     *
     */
    public static String base64(String str) {
        byte[] bytes = str.getBytes();
 
        //Base64 加密
        String encoded = Base64.getEncoder().encodeToString(bytes);
        System.out.println("Base 64 加密后：" + encoded);
 
 
        System.out.println();
 return encoded;
 
    }
    
    /**
     * Base64
     *
     */
    public static String base64Byte(byte[] bytes) {
 
        //Base64 加密
        String encoded = Base64.getEncoder().encodeToString(bytes);
        System.out.println("Base 64 加密后：" + encoded);
 
 
        System.out.println();
 return encoded;
 
    }

    
	 /**
     * @Comment SHA1实现
     * @Author Ron
     * @Date 2017年9月13日 下午3:30:36
     * @return
     */
    public static String shaEncode(String inStr) throws Exception {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
 
        byte[] byteArray = inStr.getBytes("UTF-8");
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();
    }
    
    public static byte[] shaEncodeByte(String inStr) throws Exception {
        MessageDigest sha = null;
        ByteBuffer buffer = new ByteBuffer();
        try {
            sha = MessageDigest.getInstance("SHA");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return buffer.getBytes();
        }
 
        byte[] byteArray = inStr.getBytes("UTF-8");
        byte[] md5Bytes = sha.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if(val<0)val+=256;
            if (val < 16) {
                hexValue.append("0");
                buffer.put((byte)'0');
            }
            hexValue.append(Integer.toHexString(val));
            buffer.put((byte)val);
        }
        return buffer.getBytes();
    }
    
    /**
     * @param data 字符串信息
     * @return 将字符串进行 sha1 散列，得到长度为 40 的签名。
     * @throws NoSuchAlgorithmException
     */
    private static byte[] sha1(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.update(data.getBytes());
        StringBuilder buf = new StringBuilder();
        ByteBuffer buffer = new ByteBuffer();
        byte[] bits = md.digest();
        for (int bit : bits) {
            int a = bit;
            if (a < 0) a += 256;
            if (a < 16) {
            	buf.append("0");
//            	buffer.put((byte)'0');
            }
            buffer.put((byte)a);
            buf.append(Integer.toHexString(a));
        }
        return buffer.getBytes();
    }


	//将客户端的key与服务端的key结合生成返回key
	public static String WebSocketKey(String clientKey, String serverKey){
		String temp = clientKey+serverKey;
		System.out.println(temp);
	 try {
		byte[] sha1data =	sha1(temp);
		System.out.println("sha1长度"+sha1data.length);
	} catch (Exception e1) {
		e1.printStackTrace();
	}
	 
		try {
			return base64Byte(sha1(temp));
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
}
