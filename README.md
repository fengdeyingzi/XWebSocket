# XWebSocket

利用java的socket实现WebSocket客户端，附测试工具，直接运行本项目可以进行图形化测试

图形程序采用java swing编写



websocket客户端调用方法：

~~~java
		WebSocketClient client = new WebSocketClient();
		String host = "127.0.0.1";
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
~~~

websocket服务端调用方法：

```
            WebSocketServer server = new WebSocketServer("127.0.0.1", 2024);

			server.setListener(new WebSocketServerListener() {

				@Override
				public void onOpen(WebSocketConnect con) {
					System.out.println("-------- onOpen");
					con.sendMessage("Hello, I am java server");
				}

				@Override
				public void onMessage(WebSocketConnect con, String msg) {
					System.out.println("-------- onMessage\n" + msg);
				}

				@Override
				public void onError(WebSocketConnect con, int code, String error) {
					System.out.println("------- onError " + error);
				}

				@Override
				public void onClose(WebSocketConnect con) {
					System.out.println("-------- onClose");
				}
			});
			
			server.start();
```

