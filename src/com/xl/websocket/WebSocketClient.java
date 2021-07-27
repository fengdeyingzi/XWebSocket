package com.xl.websocket;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import com.xl.util.ByteBuffer;

public class WebSocketClient{
	InputStream inputStream;
	OutputStream outputStream;
	Socket socket;
	ByteBuffer recvData = new ByteBuffer();
	ByteBuffer messageData = new ByteBuffer();//接收数据缓存
	WebSocketListener listener;
	long startTime;
	Timer timer_heartbeat;
	boolean isSendHearBeat; //是否发送心跳包
	String host;
	String road;
	int port;
	boolean isRun;
	String key = "puVOuWb7rel6z2AVZBKnfw==";
	

	public static void main(String[] args) {
		WebSocketClient client = new WebSocketClient();
		String host = "websocket.yzjlb.net";
		String road = "/socket";
		int port = 2022;
		WebSocketListener socketListener = new WebSocketListener() {
			
			@Override
			public void onOpen(WebSocketClient client) {
				System.out.println("onOpen");
				
			}
			
			@Override
			public void onMessage(WebSocketClient client, String msg) {
				System.out.println("onMessage");
				
			}
			
			@Override
			public void onError(WebSocketClient client, int err) {
				System.out.println("onError");
				
			}
			
			@Override
			public void onClose(WebSocketClient client) {
				System.out.println("onClose");
				
			}
		};
		client.setWebSocketListener(socketListener);
		client.start(host, road, port);
	}

	// 判断头信息是否获取完成
	boolean isHeadSuccess() {
		try {
			String data = new String(recvData.getBytes(), "UTF-8");
			if (data.indexOf("\r\n\r\n") > 0) {
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		return false;
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
	//发送字符数据
	public void sendMessage(String text){
		int isMask = 1;
		byte[] maskKey = new byte[]{0x66,0x66,0x66,0x66};
		byte temp1,temp2;
		if(socket == null) return;
		try {
			byte[] payloadData = text.getBytes("UTF-8");
			byte[] sendData = null;
			int FIN = 1; //1 bit 
			int opencode = 1;
			int total_len = 0;
			int payload_len = payloadData.length;
			int ptr = 0;
			total_len = payload_len + 2;
			if(payload_len > 125){
				total_len += 2;
			}
			if(isMask == 1){
				total_len += 4;
			}
			sendData = new byte[total_len];
			sendData[ptr] = (byte)((FIN<<7) | opencode);
			ptr += 1; 
			int mask = 1;
			
			if(payload_len < 126){
				sendData[ptr++] = (byte) ((isMask<<7) | payload_len);
				
			}
			else if(payload_len < 65536){
				sendData[ptr++] = (byte)((isMask<<7) | 126);
				writeShort(sendData, ptr, payload_len);
				ptr+=2;
				
			}
			if(isMask == 1){
				sendData[ptr++] = maskKey[0];
				sendData[ptr++] = maskKey[1];
				sendData[ptr++] = maskKey[2];
				sendData[ptr++] = maskKey[3];
				for(int i=0,count = 0;i<payloadData.length;i++){
					temp1 = maskKey[count];
					temp2 = payloadData[i];
					sendData[ptr++] = (byte)(((~temp1)&temp2) | (temp1&(~temp2)));
					count++;
					if(count >= 4)count = 0;
				}
				
			}
			else{
				for(int i=0;i<payloadData.length;i++){
					sendData[ptr++] = payloadData[i];
				}
			}
			try {
				System.out.println("----------- 发送 \n"+text);
				if(socket!=null)
				outputStream.write(sendData);
				startTime = System.currentTimeMillis();
			} catch (IOException e) {
				
				e.printStackTrace();
				socket = null;
				if(listener !=null)listener.onError(this, 3);
			}
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		
	}
	//读取一帧 如果为null表示读取失败
	byte[] readFrame(){
		//1字节
		int total_len = 0; //帧的总长度
		int ptr = 0;
		int FIN = 0; //1 bit 
		int RSV1,RSV2,RSV3 = 0; //共3bit
		int opcode = 0; //4 bit
		//2字节
		int mask = 0; //1 bit
		long payload_len = 0; //7 bit | 7+16 bit | 7+64 bit
		int MaskingKey = 0;  //0 | 4 bytes 掩码密钥，所有从客户端发送到服务端的帧都包含一个 32bits 的掩码（如果mask被设置成1），否则为0。一旦掩码被设置，所有接收到的 payload data 都必须与该值以一种算法做异或运算来获取真实值。
		byte[] payload_data;
		byte[] extension_data;
		int extension_len = 0;
		byte[] data = messageData.getBytes();
		if(data.length <= 2)return null;
		FIN = (data[0] & 0x80)>>7;
		RSV1 = (data[0] & 0x40)>>6;
		RSV2 = (data[0] & 0x20)>>5;
		RSV3 = (data[0] & 0x10)>>4;
		opcode = (data[0] & 0x8);
		mask = (data[1] & 0x80)>>7;
		payload_len = (data[1]&0x7f);
		ptr = 2;
		if(payload_len == 0x7e){
			if(data.length <= ptr+2)return null;
			//接下来的2字节
			payload_len = readShort(data, 2);
			ptr+=2;
		}else if(payload_len == 0x7f){
			if(data.length <= ptr+8)return null;
			//接下来的8字节
			payload_len = readLongLong(data,2);
			ptr+=8;
		}
		if(mask == 1){ //存在掩码
			total_len = (int)(ptr+4+payload_len+extension_len);
			if(data.length < total_len)return null;
			MaskingKey = readInt(data, ptr);
			ptr+= 4;
		}
		else{
			total_len = (int)(ptr+payload_len+extension_len);
			if(data.length < total_len)return null;
		}
		//读取数据
		payload_data = readBytes(data, ptr, (int)payload_len);
		messageData.clear(); //读取一帧成功 丢弃帧
		
		return payload_data;
	}
	
	public void setWebSocketListener(WebSocketListener lis){
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

			
			StringBuffer sendBuffer = new StringBuffer();
			sendBuffer.append(String.format("GET %s HTTP/1.1\r\n", road));
			sendBuffer.append("Connection:Upgrade\r\n");
			sendBuffer.append(String.format("Host:%s:%d\r\n", host, port));
			sendBuffer.append("Origin:null\r\n");
			sendBuffer.append("Sec-WebSocket-Extensions:x-webkit-deflate-frame\r\n");
			sendBuffer.append(String.format("Sec-WebSocket-Key:%s\r\n", key));
			sendBuffer.append("Sec-WebSocket-Version:13\r\n");
			sendBuffer.append("Upgrade:websocket\r\n");
			sendBuffer.append("\r\n");

			System.out.println("连接websocket" + ip);
			try {
				socket = new Socket(ip, port);
				if(isSendHearBeat)
				socket.setSoTimeout(10000);
				startTime = System.currentTimeMillis();
				outputStream = socket.getOutputStream();
				outputStream.write(sendBuffer.toString().getBytes());
				System.out.println("write " + sendBuffer.toString());
				// --输出服务器传回的消息的头信息
				InputStream inputStream = socket.getInputStream();
				int c = 0;
				while ((c = inputStream.read()) != -1) {
					recvData.put((byte) (c & 0xff));
					if (!isOpen) {
						if (isHeadSuccess()) {
							isOpen = true;
							System.out.println("----------- " + new String(recvData.getBytes()));
							if(listener!=null)listener.onOpen(this);
						}
					}
					else{
						messageData.put((byte)(c&0xff));
						byte[] msgData = readFrame();
						
						if(msgData != null){
							System.out.println("--------- 读取帧成功 \n "+new String(msgData));
							startTime = System.currentTimeMillis();
							if(listener!=null)
							listener.onMessage(this, new String(msgData));
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
				if(listener!=null)listener.onClose(this);
				// System.out.println(new String(getBody(),"UTF-8"));

			} catch (UnknownHostException e) {
				e.printStackTrace();
				socket = null;
				if(listener!=null)listener.onError(this, 1);
			} catch (IOException e) {
				e.printStackTrace();
				socket = null;
				if(listener!=null)listener.onError(this, 2);
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
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				WebSocketClient.this.run();
			}
		}).start();
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
	
	public interface WebSocketListener{
		public void onOpen(WebSocketClient client);
		
		public void onMessage(WebSocketClient client, String msg);
		
		public void onClose(WebSocketClient client);
		
		public void onError(WebSocketClient client, int err);
	}
}
