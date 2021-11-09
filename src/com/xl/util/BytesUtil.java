package com.xl.util;

public class BytesUtil {
	//读取2字节
    public static int readShort(byte[] data, int index){
        return ((data[index]&0xff) << 8 )| (data[index+1]&0xff);
    }
    public static int readInt(byte[] data, int index){
        return ((data[index+0]&0xff) << 24 ) | ((data[index+1]&0xff) << 16 ) | ((data[index+2]&0xff) << 8 ) | (data[index+3]&0xff);
    }
    public static long readLongLong(byte[] data, int index){
        return ((data[index+4]&0xff) << 24 ) | ((data[index+5]&0xff) << 16 ) | ((data[index+6]&0xff) << 8 ) | (data[index+7]&0xff);
    }
    public static byte[] readBytes(byte[] data, int index,int len){
        byte[] readData = new byte[len];
        System.arraycopy(data, index, readData, 0, len);
        return readData;
    }

    public static void writeShort(byte[] data,int index, int num){
        data[index] = (byte)((num&0xff00)>>8);
        data[index+1] = (byte)(num&0xff);
    }

}
