package com.xl.http;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.xl.util.ByteBuffer;
import com.xl.util.Str;

public class HttpParse {
	ByteBuffer sendData;
	ByteBuffer recvData;

	
	//通过url获取host
	String getUrlHost(String url){
		int type = 0;
		int i = 0;
		int start = 0;
		int end = url.length();
		DO_URL:
		for(i=0;i<url.length();i++){
			switch (type) {
			case 0:
				if(url.charAt(i) == ':'){
				type = 1;
			}
				break;
			case 1:
				if(url.charAt(i) == '/'){
					type = 2;
				}
				break;
			case 2:
				if(url.charAt(i) == '/'){
					type = 3;
					start = i+1;
				}
				break;
			case 3:
				if(url.charAt(i) == '/'){
					end = i;
					break DO_URL;
				}
				break;
			default:
				break;
			}
			
		}
		return url.substring(start,end);
	}
	//通过url获取路由
	String getUrlRoad(String url){
		int type = 0;
		int i = 0;
		int start = 0;
		int end = url.length();
		DO_URL:
		for(i=0;i<url.length();i++){
			switch (type) {
			case 0:
				if(url.charAt(i) == ':'){
				type = 1;
			}
				break;
			case 1:
				if(url.charAt(i) == '/'){
					type = 2;
				}
				break;
			case 2:
				if(url.charAt(i) == '/'){
					type = 3;
					start = i+1;
				}
				break;
			case 3:
				if(url.charAt(i) == '/'){
					start = i;
					type = 4;
					break DO_URL;
				}
				break;
			case 4:
				
				break;
			default:
				break;
			}
			if(i==url.length()-1) start = url.length();
			
		}
		return url.substring(start,end);
	}

	//判断头信息是否获取完成
	boolean isHeadSuccess(){
		try {
			String data = new String(recvData.getBytes(),"UTF-8");
			if(data.indexOf("\r\n\r\n")>0){
				return true;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	//获取recv接收到的Content-Length长度
	int getRecvContentLength(){
		int type = 0;
		int start = 0;
		int len = 0;
		DO_DATA:
		for(int i=0;i<recvData.length();i++){
			switch (type) {
			case 0:
				if(recvData.get(i) == '\n'){
					if(i-start <= 2){
						type = 1;
						start = i+1;
						len = recvData.length()-start;
						break DO_DATA;
					}
					start = i;
				}
				break;
			case 1:
				
break;
			default:
				break;
			}
		}
		return len;
	}
	
	byte[] getBody(){
		int type = 0;
		int start = 0;
		int end = recvData.length();
		int len = 0;
		DO_DATA:
		for(int i=0;i<recvData.length();i++){
			switch (type) {
			case 0:
				if(recvData.get(i) == '\n'){
					if(i-start <= 2){
						type = 1;
						start = i+1;
						len = recvData.length()-start;
						break DO_DATA;
					}
					start = i;
				}
				break;
			case 1:
				
break;
			default:
				break;
			}
		}
		byte[] data = recvData.getBytes();
		byte[] body = new byte[len];
		System.arraycopy(data, start, body, 0, body.length);
		return body;
	}
	
	//判断body是否获取完成
	boolean isBodySuccess(){
		int start = 0;
		int end = 0;
		try {
			String data = new String(recvData.getBytes(),"UTF-8");
			if(isHeadSuccess()){
				//获取Content-Length
				start = data.indexOf("Content-Length");
				if(start>=0){
					String temp = data.substring(start);
					
					start = temp.indexOf(":");
					if(start>=0){
						int content_len = Str.atoi(temp.substring(start+1));
//						System.out.println("content_len "+content_len +" "+temp.substring(start+1));
						if(content_len>=0){
							if(getRecvContentLength() >= content_len){
								return true;
							}
						}
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	//连接指定url
	String getHostByName(String host){
		InetAddress address = null;
		try {
			address = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return address.getHostAddress();
	}
	
	public HttpParse(){
		recvData = new ByteBuffer();
		sendData = new ByteBuffer();
		
	}
	
	public void getUrl(String url){
		
		String host = getUrlHost(url);
		String road = getUrlRoad(url);
		String ip = getHostByName(host);
		if(road == null || road.equals("")){
			road = "/";
		}
		String sendString = String.format("GET %s HTTP/1.1\r\nHost: %s\r\n\r\n", road, host);
		sendData.put(sendString.getBytes());
		System.out.println("连接socket"+ip);
		try {
			Socket socket = new Socket(ip, 80);
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(sendString.getBytes());
			System.out.println("write "+sendString);
			//--输出服务器传回的消息的头信息
			InputStream inputStream = socket.getInputStream();
			int c = 0;
			while((c = inputStream.read())!=-1){
				recvData.put((byte)(c&0xff));
//				System.out.println(""+c);
				
				if(isHeadSuccess()){
					if(isBodySuccess()){
						break;
					}
				}
			}
 
			//关闭流
			inputStream.close();
			System.out.println(new String(getBody(),"UTF-8"));
 
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("长度"+getRecvContentLength());
//		try {
//			System.out.println("获取完成 "+new String(recvData.getBytes(),"UTF-8"));
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	
	
}
