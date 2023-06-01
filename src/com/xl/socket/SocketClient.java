package com.xl.socket;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import com.xl.util.ByteBuffer;

public class SocketClient{
    InputStream inputStream;
    OutputStream outputStream;
    Socket socket;
    ByteBuffer recvData = new ByteBuffer();
    ByteBuffer messageData = new ByteBuffer();//接收数据缓存
    SocketListener listener;
    long startTime;
    Timer timer_heartbeat;
    boolean isSendHearBeat; //是否发送心跳包
    String host;
    String road;
    int port;
    boolean isRun;
    String key = "puVOuWb7rel6z2AVZBKnfw==";
    Handler handler;
    private static int WHAT_ONOPEN = 1;
    private static int WHAT_ONMESSAGE = 2;
    private static int WHAT_ONCLOSE = 3;
    private static int WHAT_ONERROR = 4;
    boolean useThread = false; //是否使用线程通信
    String coding = "UTF-8";


    public static void main(String[] args) {
        SocketClient client = new SocketClient();
        String host = "websocket.yzjlb.net";
        String road = "/socket";
        int port = 2022;
        SocketListener socketListener = new SocketListener() {

            @Override
            public void onOpen(SocketClient client) {
                System.out.println("onOpen");

            }

            @Override
            public void onMessage(SocketClient client, String msg) {
                System.out.println("onMessage");

            }

            @Override
            public void onError(SocketClient client, int err) {
                System.out.println("onError");

            }

            @Override
            public void onClose(SocketClient client) {
                System.out.println("onClose");

            }
        };
        client.setSocketListener(socketListener);
        client.start(host, road, port);
    }

    // 判断头信息是否获取完成
    boolean isHeadSuccess() {
        return true;
    }

    public static byte[] getIntByte(int number){
        byte[] bytes = new byte[4];
        bytes[0] = (byte) (number&0xff);
        bytes[1] = (byte) ((number>>8)&0xff);
        bytes[2] = (byte) ((number>>16)&0xff);
        bytes[3] = (byte) ((number>>24)&0xff);
        return bytes;
    }

    public static byte[] getShortByte(int number){
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (number&0xff);
        bytes[1] = (byte) ((number>>8)&0xff);
        return bytes;
    }

    public static byte[] getBigIntByte(int number){
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (number&0xff);
        bytes[2] = (byte) ((number>>8)&0xff);
        bytes[1] = (byte) ((number>>16)&0xff);
        bytes[0] = (byte) ((number>>24)&0xff);
        return bytes;
    }
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
    public byte[] readBytes(byte[] data, int index,int len){
        byte[] readData = new byte[len];
        System.arraycopy(data, index, readData, 0, len);
        return readData;
    }

    public void writeShort(byte[] data,int index, int num){
        data[index] = (byte)((num&0xff00)>>8);
        data[index+1] = (byte)(num&0xff);
    }

    private void sendToHandler(int what, String msg, int p1){
        if(useThread){
            Message message = new Message();
            message.what = what;
            message.obj = msg;
            message.arg1 = p1;
            handler.sendMessage(message);
        }else{
            if(what == WHAT_ONOPEN){
                if(listener!=null){
                    listener.onOpen(this);
                }
            }else if(what == WHAT_ONMESSAGE){
                if(listener!=null){
                    listener.onMessage(this,msg);
                }
            }else if(what == WHAT_ONCLOSE){
                if(listener!=null){
                    listener.onClose(this);
                }
            }else if(what == WHAT_ONERROR){
                if(listener!=null){
                    listener.onError(this,p1);
                }
            }
        }

    }
    //发送字符数据
    public void sendMessage(String text){
        byte[] sendData = new byte[0];
        try {
            if(text.charAt(text.length()-1)!='\n'){
                text = text+"";
            }
            sendData = text.getBytes(coding);
            System.out.println("----------- 发送 \n"+text);
            if(socket!=null){
                outputStream.write(sendData);
            }
            System.out.println("----------- 发送成功");
            startTime = System.currentTimeMillis();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    //读取一帧 如果为null表示读取失败
    byte[] readFrame(){
        if(messageData.length()==0)return null;
        byte[] data = messageData.getBytes();
        byte[] payload_data = null;
        if(data[data.length-1]=='\n'){
            payload_data = new byte[data.length-1];
            System.arraycopy(data,0,payload_data,0, payload_data.length);
            messageData.clear();
            return payload_data;
        }

        return payload_data;
    }

    public void setSocketListener(SocketListener lis){
        this.listener = lis;
    }

    //连接指定url
    String getHostByName(String host){
        InetAddress address = null;
        if(isIP(host)){
            return host;
        }
        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        return address.getHostAddress();
    }

    //判断host是否为ip
    public boolean isIP(String host){
        for(int i=0;i<host.length();i++){
            if(host.charAt(i)>='0' && host.charAt(i)<='9'){

            }else if(host.charAt(i) == '.'){

            }
            else{
                return false;
            }
        }
        return true;
    }


    private void run() {

        String ip = getHostByName(host);
        startTime = System.currentTimeMillis();
        boolean isOpen = false;
        boolean isWrite = false;
        timer_heartbeat = new Timer();
        timer_heartbeat.schedule(new TimerTask() {
            @Override
            public void run() {
                if(System.currentTimeMillis() - startTime > 5000){
                    if(isSendHearBeat){
                        sendMessage("#");
                    }

//	                	sendMessage("{\"action\":\"setname\", \"data\":\"test\"}");
                    startTime = System.currentTimeMillis();
                }
            }
        },0, 1 * 1000);


        

        System.out.println("连接socket" + ip);
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress( ip, port ), 5000);
            if (!isOpen) {
                if (isHeadSuccess()) {
                    isOpen = true;
                    System.out.println("----------- " + new String(recvData.getBytes()));
                    sendToHandler(WHAT_ONOPEN, "",0);

                }
            }
            if(isSendHearBeat)
                socket.setSoTimeout(10000);
            startTime = System.currentTimeMillis();
            outputStream = socket.getOutputStream();
            
            // --输出服务器传回的消息的头信息
            InputStream inputStream = socket.getInputStream();

            int c = 0;
            while ((c = inputStream.read()) != -1) {
//                recvData.put((byte) (c & 0xff));

                {
                	System.out.println(""+c);
                    messageData.put((byte)(c&0xff));
                    byte[] msgData = readFrame();

                    if(msgData != null){
                        System.out.println("--------- 读取帧成功 \n "+new String(msgData, coding));
                        startTime = System.currentTimeMillis();
                        sendToHandler(WHAT_ONMESSAGE, new String(msgData, coding),0);

                        if(isWrite == false){
//								sendMessage("{\"action\":\"setname\", \"data\":\"test\"}");
                            isWrite = true;
                        }

//							break;
                    }
                    else {
                        if(System.currentTimeMillis() - startTime > 5000){
                            System.out.println("发送心跳包");
                            sendMessage("#");
                            startTime = System.currentTimeMillis();
                        }
                        else{

                        }
                    }
                }

                // System.out.println(""+c);

                ;
            }

            // 关闭流
            inputStream.close();
            socket = null;
            System.out.println("socket关闭");
            sendToHandler(WHAT_ONCLOSE,"",0);
            // System.out.println(new String(getBody(),"UTF-8"));

        } catch (UnknownHostException e) {
            e.printStackTrace();
            socket = null;
            sendToHandler(WHAT_ONERROR,"未知主机异常",1);

        } catch (SocketTimeoutException e){
            e.printStackTrace();
            socket = null;
            sendToHandler(WHAT_ONERROR, "连接出错",2);
        }
        catch (SocketException e){
            e.printStackTrace();
            socket = null;
            sendToHandler(WHAT_ONERROR, "连接出错",2);
        }
        catch (IOException e) {
            e.printStackTrace();
            socket = null;
            sendToHandler(WHAT_ONERROR, "连接出错",2);
        }
        System.out.println("--end");
    }

    /*
     * 服务器返回数据 HTTP/1.1 101 Switching Protocols Upgrade: websocket Connection:
     * Upgrade Sec-WebSocket-Accept: lt1/FHuL6o2V8tma5G4mOcqYBFA=
     */
    public void start(String host, String road,int port) {
        String key = "puVOuWb7rel6z2AVZBKnfw==";
        this.host = host;
        this.road = road;
        this.port = port;
        isRun = true;
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage( Message msg) {

                if(msg.what == WHAT_ONOPEN){ //open
                    if(listener!=null){
                        listener.onOpen(SocketClient.this);
                    }
                }else if(msg.what == WHAT_ONMESSAGE){ //message
                    if(listener!=null){
                        listener.onMessage(SocketClient.this, (String)msg.obj);
                    }
                }else if(msg.what == WHAT_ONCLOSE){ //close
                    if(listener!=null){
                        listener.onClose(SocketClient.this);
                    }
                } else if(msg.what == WHAT_ONERROR){ //error
                    if(listener!=null){
                        listener.onError(SocketClient.this,msg.arg1);
                    }
                }
            }
        };
        if(useThread){
            new Thread(new Runnable() {

                @Override
                public void run() {
                    SocketClient.this.run();
                }
            }).start();
        }else{
            run();
        }

    }

    public void stop(){
        isRun = false;
        timer_heartbeat.cancel();
        if(socket!=null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }

    }

    public interface SocketListener{
        public void onOpen(SocketClient client);

        public void onMessage(SocketClient client, String msg);

        public void onClose(SocketClient client);

        public void onError(SocketClient client, int err);
    }
}
