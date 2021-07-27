package com.xl.window;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.xl.util.Str;
import com.xl.websocket.WebSocketClient;
import com.xl.websocket.WebSocketClient.WebSocketListener;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class WebSocketTestWindow {

	public JFrame frame;
	private JTextField edit_url;

	

	/**
	 * Create the application.
	 */
	public WebSocketTestWindow() {
		initialize();
	}
	
	//通过url获取host 需free
	String getUrlHost(String url) {
		int len = (url.length());
		
		int i = 0;
		int type = 0;
		int start = 0;
		int end = len;
		out_host:
		while (i<len) {
			switch (type) {
			case 0:
				if (url.charAt(i) == ':') {
					type = 1;
				}
				break;
			case 1:
				if (url.charAt(i) == '/') {
					type = 2;
				} else {
					break out_host;
				}
				break;
			case 2:
				if (url.charAt(i) == '/') {
					start = i + 1;
					type = 3;
				} else {
					break out_host;
				}
				break;
			case 3:
				if (url.charAt(i) == '/' || url.charAt(i) == ':') {
					end = i;
					break out_host;
				}
				break;
			default:
				break;
			}

			i++;
		}
	
		if (start > 0 && end > 0) {
			String host = url.substring( start, end);
			return host;
		}
		
		return null;
	}

	//通过url获取路由 需free
	String getUrlRoad(String url) {
		int len = (url.length());
//		char *host = malloc(len);
		int i = 0;
		int type = 0;
		int start = 0;
		int end = len;
//		memset(host, '\0', len);
		out_road:
		while (i<len) {
			switch (type) {
			case 0:
				if (url.charAt(i) == ':') {
					type = 1;
				}
				break;
			case 1:
				if (url.charAt(i) == '/') {
					type = 2;
				} else {
					break out_road;
				}
				break;
			case 2:
				if (url.charAt(i) == '/') {
					type = 3;
				} else {
					break out_road;
				}
				break;
			case 3:
				if (url.charAt(i) == '/') {
					start = i;
					type = 4;
					break out_road;
				}
				break;
			case 4:

				break;
			default:
				break;
			}
			if (i == len - 1) {
				start = -1;
				break out_road;
			}
			i++;
		}
	
		if (start > 0 && end > 0) {
			String host = url.substring( start, len);
			return host;
		} else {
//			memcpy(host, "/", 1);
			return "/";
		}
		
		
	}

	//通过url获取port 需free
	int getUrlPort(String url) {
		int len = (url.length());

		int i = 0;
		int type = 0;
		int start = 0;
		int end = len;
		int port = 80;
		
		out_road:
		while (i<len) {
			switch (type) {
			case 0:
				if (url.charAt(i) == ':') {
					type = 1;
				}
				break;
			case 1:
				if (url.charAt(i) == '/') {
					type = 2;
				} else {
					break out_road;
				}
				break;
			case 2:
				if (url.charAt(i) == '/') {
					type = 3;
				} else {
					break out_road;
				}
				break;
			case 3:
				if (url.charAt(i) == ':') {
					start = i + 1;
					type = 4;
					// printf("atoi \n");
					port = Str.atoi(url.substring(start));
					break out_road;
				} else if (url.charAt(i) == '/') {
					break out_road;
				}
				break;
			case 4:

				break;
			default:
				break;
			}
			if (i == len - 1) {
				start = -1;
				break out_road;
			}
			i++;
		}
	

		return port;
	}

	JTextArea text_left;
	JTextArea text_right;
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Box horizontalBox_send = Box.createHorizontalBox();
		frame.getContentPane().add(horizontalBox_send, BorderLayout.SOUTH);
		
		final JEditorPane edit_send = new JEditorPane();
		edit_send.setMinimumSize(new Dimension(400,32));
		edit_send.setToolTipText("输入要发送的内容");
		horizontalBox_send.add(edit_send);
		
		JButton btn_send = new JButton("发送");
		btn_send.setActionCommand("发送");
		horizontalBox_send.add(btn_send);
		
		 text_left = new JTextArea();
		 text_right = new JTextArea();
//		frame.getContentPane().add(text_left, BorderLayout.NORTH);
		
		JScrollPane scroll_left = new JScrollPane(text_left);
		frame.getContentPane().add(scroll_left, BorderLayout.WEST);
		text_left.setColumns(20);
		text_left.setRows(10);
		JScrollPane scroll_right = new JScrollPane(text_right);
		frame.getContentPane().add(scroll_right, BorderLayout.CENTER);
		final WebSocketClient client = new WebSocketClient();
		final Handler handler = new Handler(Looper.getMainLooper()) {
			
			@Override
			public void handleMessage(Message msg) {
				System.out.println("handleMessage");
				if(msg.what == 1){ //left
					text_left.append((String)msg.obj);
				}else if(msg.what == 2){ //right
					text_right.append((String)msg.obj);
				}
				
			}
		};
		final WebSocketClient.WebSocketListener listener = new WebSocketListener() {
			
			@Override
			public void onOpen(WebSocketClient client) {
				Message message = new Message();
				message.what = 1;
				message.obj = "--> onOpen\n";
				handler.sendMessage(message);
//				text_left.append();
				
			}
			
			@Override
			public void onMessage(WebSocketClient client, String msg) {
				System.out.println("--> onMessage");
				Message message = new Message();
				message.what = 2;
				message.obj = String.format("--> onMessage\n%s\n", msg);
				handler.sendMessage(message);
//				text_right.append(String.format("--> onMessage\n%s\n", msg));
				
			}
			
			@Override
			public void onError(WebSocketClient client, int err) {
				Message message = new Message();
				message.what = 1;
				message.obj = "--> onError\n";
				handler.sendMessage(message);
//				text_right.append("--> onError\n");
				
			}
			
			@Override
			public void onClose(WebSocketClient client) {
				Message message = new Message();
				message.what = 1;
				message.obj = "--> onClose\n";
				handler.sendMessage(message);
//				text_left.append("--> onClose\n");
				
			}
		};
		
//		frame.getContentPane().add(text_right, BorderLayout.CENTER);
		//		frame.getContentPane().add(text_right, BorderLayout.CENTER);
				text_right.setColumns(20);
				text_right.setRows(10);
				
				Box horizontalBox_url = Box.createHorizontalBox();
				frame.getContentPane().add(horizontalBox_url, BorderLayout.NORTH);
				
				edit_url = new JTextField();
				edit_url.setToolTipText("输入url");
				horizontalBox_url.add(edit_url);
				edit_url.setColumns(10);
				
				JButton btn_go = new JButton("连接");
				horizontalBox_url.add(btn_go);
				frame.setTitle("WebSocket测试 - 风的影子");
				
				btn_send.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						client.sendMessage(edit_send.getText());
						
					}
				});
				
				btn_go.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent actionevent) {
						final String url = edit_url.getText();
						
						
						client.setWebSocketListener(listener);
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								String host = getUrlHost(url);
								String road = getUrlRoad(url);
								int port = getUrlPort(url);
								client.start(host, road, port);
								
							}
						}).start();
						
					}
				});
				
				
	}

}
