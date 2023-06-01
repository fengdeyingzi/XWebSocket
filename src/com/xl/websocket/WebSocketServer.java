package com.xl.websocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import com.xl.util.ByteBuffer;
import com.xl.util.BytesUtil;

public class WebSocketServer {
	private String ip;
	private int port;
	ServerSocket server_socket;
	Vector<Socket> list_socket;
	private ExecutorService mExecutorService = null;
	private boolean isRun;
	private WebSocketServerListener listener;

	// 创建
	public WebSocketServer(String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.list_socket = new Vector<Socket>();

		/**
		 * 创建缓存类型的线程池
		 */
		mExecutorService = Executors.newCachedThreadPool();
	}

	public interface WebSocketServerListener {

		public void onOpen(WebSocketConnect con);

		public void onMessage(WebSocketConnect con, String msg);

		public void onClose(WebSocketConnect con);

		public void onError(WebSocketConnect con, int code, String error);
	}

	public class WebSocketConnect implements Runnable {
		// 缓存
		private ByteBuffer cache_buffer;
		private Socket socket;
		private long startTime;
		private long endTime;
		private boolean isUpdate;
		private String serverKey = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";// "d359Fdo6omyqfxyYF7Yacw==";
		private String clientKey;
		private WebSocketServerListener listener;

		public WebSocketConnect(Socket socket) {
			this.socket = socket;
			this.startTime = System.currentTimeMillis();
			this.endTime = System.currentTimeMillis();
			this.cache_buffer = new ByteBuffer();
			this.isUpdate = false;
		}

		public void setListener(WebSocketServerListener listener) {
			this.listener = listener;
		}

		String getIP() {
			return socket.getInetAddress().getHostAddress();
		}

		int getID() {
			return socket.getPort();
		}

		// 判断头信息是否获取完成
		boolean isHeadSuccess() {
			try {
				String data = new String(cache_buffer.getBytes(), "UTF-8");
				if (data.indexOf("\r\n\r\n") > 0) {
					return true;
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return false;
		}

		// 获取一个参数
		private String getArg(String head, String item) {
			int start = 0;
			int end = 0;
			int content_len = 0;

			// 获取Content-Length
			start = head.indexOf(item);
			if (start < 0) {
				start = head.indexOf(item.toLowerCase());
			}
			if (start >= 0) {
				String temp = head.substring(start);

				start = temp.indexOf(":");
				if (start >= 0) {
					for (int i = 0; i < temp.length(); i++) {
						if (temp.charAt(i) == '\r' || temp.charAt(i) == '\n') {
							return temp.substring(start + 1, i).trim();
						}
					}
				}
			}
			return null;
		}

		public void run() {
			InputStream inputStream;
			try {
				inputStream = this.socket.getInputStream();
				int temp;
				while ((temp = inputStream.read()) >= 0) {
					cache_buffer.put((byte) temp);
					if (!isUpdate) {
						if (isHeadSuccess()) {
							String data = new String(cache_buffer.getBytes(), "UTF-8");
							System.out.println(data);
							clientKey = getArg(data, "Sec-WebSocket-Key");
							if (clientKey == null) {
								System.out.println("获取客户端key失败");
								String sendData = "HTTP/1.1 404 not found\r\n" + "Content-Length: 13\r\n" + "\r\n"
										+ "404 not found";
								socket.getOutputStream().write(sendData.getBytes());
							} else {
								cache_buffer.clear();

								String key = WebUpgradleUtil.WebSocketKey(clientKey, serverKey);
								// 写入
								String sendData = "HTTP/1.1 101 Switching Protocols\r\n" + "Connection: Upgrade\r\n"
										+ "Upgrade: websocket\r\n" + "Sec-WebSocket-Accept: " + key + "\r\n"
										+ "Upgradle: websocket\r\n" + "\r\n";
								isUpdate = true;
								socket.getOutputStream().write(sendData.getBytes());
								System.out.println("协议已升级");
								if (listener != null) {
									listener.onOpen(this);
								} else {
									System.out.println("监听器为空");
								}
								// sendMessage("hello");
							}

						}
					} else {
						byte[] frame = readFrame();
						if (frame != null) {
							
							String msg = new String(frame, "UTF-8");
							if(!"#".equals(msg)){
								System.out.println("收到数据");
								System.out.println(msg);
							}
							
							if (listener != null) {
								listener.onMessage(this, msg);
							}
						}
					}

				}
			} catch (IOException e) {
				e.printStackTrace();
				if (listener != null) {
					listener.onError(this, 1, e.toString());
					try {
						socket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					list_socket.remove(this);
				}
			}

		}

		public long getEndTime() {
			return this.endTime;
		}

		public long getStartTime() {
			return this.startTime;
		}
		
		public void close(){
			if(socket!= null){
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("关闭socket失败");
					e.printStackTrace();
				}
			}
		}

		// 发送字符数据
		public void sendMessage(String text) {
			int isMask = 0;
			byte[] maskKey = new byte[] { 0x66, 0x66, 0x66, 0x66 };
			byte temp1, temp2;
			if (socket == null)
				return;
			try {
				byte[] payloadData = text.getBytes("UTF-8");
				byte[] sendData = null;
				int FIN = 1; // 1 bit
				int opencode = 1;
				int total_len = 0;
				int payload_len = payloadData.length;
				int ptr = 0;
				total_len = payload_len + 2;
				if (payload_len > 125) {
					total_len += 2;
				}
				if (isMask == 1) {
					total_len += 4;
				}
				sendData = new byte[total_len];
				sendData[ptr] = (byte) ((FIN << 7) | opencode);
				ptr += 1;
				int mask = 1;

				if (payload_len < 126) {
					sendData[ptr++] = (byte) ((isMask << 7) | payload_len);

				} else if (payload_len < 65536) {
					sendData[ptr++] = (byte) ((isMask << 7) | 126);
					BytesUtil.writeShort(sendData, ptr, payload_len);
					ptr += 2;

				}
				if (isMask == 1) {
					sendData[ptr++] = maskKey[0];
					sendData[ptr++] = maskKey[1];
					sendData[ptr++] = maskKey[2];
					sendData[ptr++] = maskKey[3];
					for (int i = 0, count = 0; i < payloadData.length; i++) {
						temp1 = maskKey[count];
						temp2 = payloadData[i];
						sendData[ptr++] = (byte) (((~temp1) & temp2) | (temp1 & (~temp2)));
						count++;
						if (count >= 4)
							count = 0;
					}

				} else {
					for (int i = 0; i < payloadData.length; i++) {
						sendData[ptr++] = payloadData[i];
					}
				}
				try {

					if (socket != null) {
						socket.getOutputStream().write(sendData);
						endTime = System.currentTimeMillis();
//						System.out.println("----------- 发送 \n" + text);
					}

					startTime = System.currentTimeMillis();
				} catch (IOException e) {

					e.printStackTrace();
					socket = null;
					if (listener != null) {
						listener.onError(this, 2, e.toString());
						list_socket.remove(this);
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				if (listener != null) {
					listener.onError(this, 2, e.toString());
					list_socket.remove(this);
				}
			}

		}

		// 读取一帧数据 如果读取失败就返回null
		public byte[] readFrame() throws IOException {

			byte[] maskKey = new byte[] { 0x66, 0x66, 0x66, 0x66 };

			// 1字节
			int total_len = 0; // 帧的总长度
			int ptr = 0;
			int FIN = 0; // 1 bit
			int RSV1, RSV2, RSV3 = 0; // 共3bit
			int opcode = 0; // 4 bit
			// 2字节
			int mask = 0; // 1 bit
			long payload_len = 0; // 7 bit | 7+16 bit | 7+64 bit
			// int MaskingKey = 0; //0 | 4 bytes 掩码密钥，所有从客户端发送到服务端的帧都包含一个 32bits
			// 的掩码（如果mask被设置成1），否则为0。一旦掩码被设置，所有接收到的 payload data
			// 都必须与该值以一种算法做异或运算来获取真实值。
			byte[] payload_data;
			byte[] extension_data;
			int extension_len = 0;
			byte[] data = cache_buffer.getBytes();
			if (data.length <= 2)
				return null;
			FIN = (data[0] & 0x80) >> 7;
			RSV1 = (data[0] & 0x40) >> 6;
			RSV2 = (data[0] & 0x20) >> 5;
			RSV3 = (data[0] & 0x10) >> 4;
			opcode = (data[0] & 0x8);
			mask = (data[1] & 0x80) >> 7;
			payload_len = (data[1] & 0x7f);
			ptr = 2;
			if (payload_len == 0x7e) {
				if (data.length <= ptr + 2)
					return null;
				// 接下来的2字节
				payload_len = BytesUtil.readShort(data, 2);
				ptr += 2;
			} else if (payload_len == 0x7f) {
				if (data.length <= ptr + 8)
					return null;
				// 接下来的8字节
				payload_len = BytesUtil.readLongLong(data, 2);
				ptr += 8;
			}
			if (mask == 1) { // 存在掩码
				total_len = (int) (ptr + 4 + payload_len + extension_len);
				if (data.length < total_len)
					return null;
				// MaskingKey = BytesUtil.readInt(data, ptr);
				maskKey = BytesUtil.readBytes(data, ptr, 4);
				ptr += 4;
			} else {
				total_len = (int) (ptr + payload_len + extension_len);
				if (data.length < total_len)
					return null;
			}
			// 读取数据
			payload_data = BytesUtil.readBytes(data, ptr, (int) payload_len);
			if (mask == 1) {
				payload_data = maskBytes(payload_data, maskKey);
			}
			cache_buffer.clear(); // 读取一帧成功 清空缓存
			endTime = System.currentTimeMillis();
			return payload_data;
		}

		// 将字节进行mask解码
		private byte[] maskBytes(byte[] data, byte[] maskKey) {

			int ptr = 0;
			byte temp1;
			byte temp2;
			// data[ptr++] = maskKey[0];
			// data[ptr++] = maskKey[1];
			// data[ptr++] = maskKey[2];
			// data[ptr++] = maskKey[3];
			for (int i = 0, count = 0; i < data.length; i++) {
				temp1 = maskKey[count];
				temp2 = data[i];
				data[ptr++] = (byte) (((~temp1) & temp2) | (temp1 & (~temp2)));
				count++;
				if (count >= 4)
					count = 0;
			}
			return data;
		}
	}

	// 开始启动
	public void start() {
		isRun = true;
		System.out.println("开始启动");
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			while (isRun) {
				Socket mSocket;
				try {
					mSocket = server_socket.accept();
					list_socket.add(mSocket);
					WebSocketConnect connect = new WebSocketConnect(mSocket);
					if (this.listener != null) {
						connect.setListener(this.listener);
					} else {
						System.out.println("监听器为空 websocketserver");
					}

					mExecutorService.execute(connect);
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			//stop了，断开所有socket
			for(int n=0;n<list_socket.size();n++){
				try{
					list_socket.get(n).close();
				}catch(Exception e2){
					System.out.println("断开socket异常："+e2.toString());
				}
			}
		}

	}

	public void setListener(WebSocketServerListener listener) {
//		System.out.println("设置监听器 websocket 1");
		this.listener = listener;
		if (this.listener != null) {
//			System.out.println("设置监听器 websocket 2");
		}
	}

	// 停止运行
	public void stop() {
		isRun = false;
		
	}

	// onOpen
	public void onOpen(Socket socket) {

	}

	// onMessage
	public void onMessage(Socket socket, String msg) {

	}

	// onClose
	public void onClose(Socket socket) {

	}

	// onError
	public void onError(Socket socket) {

	}
}
