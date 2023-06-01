import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import javax.lang.model.element.Element;

import org.json.JSONArray;
import org.json.JSONObject;

import com.xl.util.FileUtils;
import com.xl.util.XConnect;
import com.xl.websocket.WebSocketServer;
import com.xl.websocket.WebSocketServer.WebSocketConnect;
import com.xl.websocket.WebSocketServer.WebSocketServerListener;

import android.os.Looper;

/*
 客户端：
心跳包：#
注册
  {action:register,data:密码,id:738565676}
登录
  {action:login,data:这是密码,id:654657465}
发送消息
  {action:sendmsg,data:这是一条消息}
发送xml消息（暂未实现）
  {action:sendxml,data:这是消息，可以加<image url=/>}
发送图片消息
  {action:sendimg,data:这是图片消息}
退出登录
  {action:exit}
获取QQ昵称
  {action:getname,id:5837586}

服务器：
发送消息
  {action:sendmsg,data:这是一条消息,id:用户id,name:用户名}
发送图片消息
  {action:sendimg,data:http://...,id:用户id}
发送xml消息（暂未实现）
  {action:sendxml,data:这是一条消息,id:用户id,name:用户名}
发送消息列表（暂未实现）
  {action:msgs,data:[
      {
          id:用户名id
          name:用户名
      }
  ]}
收到系统提示
  {action:prompt,data:这是一个提示}
收到成员退出信息
  {action:exit,id:758386576}
用户系统，用json保存
{
  userlist:[
    {
      qq:13738568,
      pass:34675767
    }
  ]
}
返回QQ昵称：
  {action:username,id:3566466,data:昵称}

 */
public class QQChatServerMain {

	public static void main(String[] args) {
		// 启动looper
		Looper.prepare(true);

		new Thread(new Runnable() {

			@Override
			public void run() {
				QQChatServer server = new QQChatServerMain().new QQChatServer();
				server.start();

			}
		}).start();

		Looper.loop();
	}

	public class UserItem {
		long qq;
		String pass;

	}

	public class MsgItem {
		String action;
		String data;
		long id;

		public MsgItem(String action, String data, long id) {
			this.action = action;
			this.data = data;
			this.id = id;
		}
	}

	public class ChatItem {
		private long qq;
		private WebSocketConnect con;

		public ChatItem(long qq, WebSocketConnect con) {
			this.qq = qq;
			this.con = con;
		}

		public WebSocketConnect getConnect() {
			return con;
		}

		public long getQQ() {
			return this.qq;
		}
	}

	public class QQChatServer {
		List<ChatItem> list_con;
		List<UserItem> list_user;
		List<MsgItem> list_msg;

		public QQChatServer() {
			this.list_con = new Vector<ChatItem>();
			this.list_user = new Vector<QQChatServerMain.UserItem>();
			this.list_msg = new Vector<QQChatServerMain.MsgItem>();
			getHistoryMsg();
			readUserList();
		}

		// 注册一个用户
		boolean regUser(long qq, String pass) {
			// for(int i=0;i<list_user.size();i++){
			// UserItem item = list_user.get(i);
			// if(item.qq == qq && item.pass.equals(pass)){
			// list_user.add(item);
			// return true;
			// }
			// }
			UserItem item = new UserItem();
			item.qq = qq;
			item.pass = pass;
			list_user.add(item);
			return true;

		}

		// 获取qq昵称
		String getQQName(final WebSocketConnect con, final long qq) {
			// print("获取QQ昵称：${qq}");
			XConnect connect = new XConnect("https://r.qzone.qq.com/fcg-bin/cgi_get_portrait.fcg?uins=" + qq,
					new XConnect.PostGetInfoListener() {

						@Override
						public void onPostGetText(String text) {
							System.out.println(text);
							int start = 0;
							int end = 0;
							int type = 0;

							for (int i = text.length() - 1; i > 0; i--) {
								if (type == 0) {
									if (text.charAt(i) == '\"') {
										end = i;
										type = 1;
									}
								} else {
									if (text.charAt(i) == '\"') {
										start = i + 1;
										break;
									}
								}
							}
							if (start == 0 || end == 0) {
								return;
							}
							String name = text.substring(start, end);
							sendName(con, qq, name);
						}
					});
			connect.setCoding("GBK");
			connect.start();

			return "";
		}

		// 读取用户列表
		void readUserList() {
			String text = null;
			try {
				text = FileUtils.read(new File("users.json"), "Utf-8");
			} catch (IOException e) {
				e.printStackTrace();
				return;

			}
			if (text == null || text.length() == 0) {
				return;
			}
			JSONObject jsonObject = new JSONObject(text);
			JSONArray jsonArray = jsonObject.getJSONArray("userlist");
			list_user.clear();
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject object = jsonArray.getJSONObject(i);
				UserItem item = new UserItem();
				item.qq = object.optLong("qq");
				item.pass = object.optString("pass");
				list_user.add(item);
			}

		}

		// 保存用户列表
		void saveUserList() {
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < list_user.size(); i++) {
				JSONObject object = new JSONObject();
				UserItem userItem = list_user.get(i);

				object.put("qq", userItem.qq);
				object.put("pass", userItem.pass);
				jsonArray.put(object);
			}
			jsonObject.put("userlist", jsonArray);
			FileUtils.writeText("users.json", jsonObject.toString(), "UTF-8");

		}

		// 获取历史消息
		List<MsgItem> getHistoryMsg() {
			String text;
			try {
				text = FileUtils.read(new File("msgs.json"), "UTF-8");
			} catch (IOException e) {
				return list_msg;
			}
			if (text == null || text.length() == 0) {
				return list_msg;
			}
			JSONObject jsonObject = new JSONObject(text);
			JSONArray jsonArray = jsonObject.getJSONArray("msglist");
			list_msg.clear();
			for (int i = 0; i < jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);
				MsgItem item = new MsgItem(jsonObject.optString("action"), jsonObject.optString("data"),
						jsonObject.optLong("id"));
				list_msg.add(item);
			}
			return list_msg;
		}

		// 发送历史消息
		void sendHistoryMsg(WebSocketConnect con) {
			for (int i = 0; i < list_msg.size(); i++) {
				MsgItem item = list_msg.get(i);
				if (item.action.equals("sendmsg")) {
					sendMsg(con, item.id, item.data);
				} else if (item.action.equals("sendimg")) {
					sendImg(con, item.id, item.data);
				}

			}
		}

		// 添加一条消息到数据库
		void addHistoryMsg(MsgItem item) {
			if (item.action.equals("sendmsg")) {
				list_msg.add(item);
			} else if (item.action.equals("sendimg")) {
				list_msg.add(item);
			}
			if (list_msg.size() > 200) {
				list_msg.remove(0);
			}
			if (list_msg.size() > 200) {
				list_msg.remove(0);
			}
			saveHistoryMsg();
		}

		// 保存消息列表
		void saveHistoryMsg() {
			JSONObject jsonObject = new JSONObject();
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < list_msg.size(); i++) {
				MsgItem item = list_msg.get(i);
				JSONObject object = new JSONObject();
				object.put("action", item.action);
				object.put("data", item.data);
				object.put("id", item.id);
				jsonArray.put(object);
			}
			jsonObject.put("msglist", jsonArray);
			FileUtils.writeText("msgs.json", jsonObject.toString(), "UTF-8");

		}

		// 检测消息是否正常
		boolean checkMsg(MsgItem msgitem) {
			int lines = 0;
			if (msgitem.action.equals("sendmsg")) {
				if (msgitem.data.length() > 1024 * 10) {
					return false;
				}
				for (int i = 0; i < msgitem.data.length(); i++) {
					if (msgitem.data.charAt(i) == '\n') {
						lines++;
						if (lines > 300) {
							return false;
						}
					}
				}
			} else if (msgitem.action.equals("sendimg")) {
				if (msgitem.data.length() > 512) {
					return false;
				}
			}
			return true;
		}

		// 判断用户密码是否可以登录
		boolean checkUser(long qq, String pass) {
			for (int i = 0; i < list_user.size(); i++) {
				UserItem item = list_user.get(i);
				if (item.qq == qq && item.pass.equals(pass)) {
					return true;
				}
			}
			return false;
		}

		// 判断用户是否存在
		boolean checkUserCon(long qq) {
			for (int i = 0; i < list_user.size(); i++) {
				UserItem item = list_user.get(i);
				if (item.qq == qq) {
					return true;
				}
			}
			return false;
		}

		// 发送消息给所有人
		public void sendMsgAll(Long qq, String msg) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", "sendmsg");
			jsonObject.put("data", msg);
			jsonObject.put("id", qq);
			for (ChatItem item : list_con) {
				try{
					item.con.sendMessage(jsonObject.toString());
				}catch(Exception e1){
					System.out.println("发送失败："+item.qq);
				}
				
			}
		}

		// 发送消息给所有人
		public void sendXmlAll(Long qq, String msg) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", "sendxml");
			jsonObject.put("data", msg);
			jsonObject.put("id", qq);
			for (ChatItem item : list_con) {
				try{
					item.con.sendMessage(jsonObject.toString());
				}catch(Exception e1){
					System.out.println("发送xml失败："+item.qq);
				}
				
			}
		}

		// 发送图片给所有人
		public void sendImgAll(Long qq, String msg) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", "sendimg");
			jsonObject.put("data", msg);
			jsonObject.put("id", qq);
			for (ChatItem item : list_con) {
				try{
					item.con.sendMessage(jsonObject.toString());
				}catch(Exception e1){
					System.out.print("发送图片失败："+item.qq);
				}
					
				
			}
		}

		public void sendMsg(WebSocketConnect con, Long id, String msg) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", "sendmsg");
			jsonObject.put("data", msg);
			jsonObject.put("id", id);
			con.sendMessage(jsonObject.toString());
		}

		public void sendImg(WebSocketConnect con, Long id, String msg) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", "sendimg");
			jsonObject.put("data", msg);
			jsonObject.put("id", id);
			con.sendMessage(jsonObject.toString());
		}

		public void sendName(WebSocketConnect con, Long id, String name) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", "username");
			jsonObject.put("data", name);
			jsonObject.put("id", id);
			con.sendMessage(jsonObject.toString());
		}

		public void sendPrompt(WebSocketConnect con, String msg) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", "prompt");
			jsonObject.put("data", msg);
			// jsonObject.put("id", id);
			try{
				con.sendMessage(jsonObject.toString());
			}catch(Exception e1){
				System.out.println("发送提示失败");
			}
			
		}

		public void sendErr(WebSocketConnect con, String msg) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("action", "err");
			jsonObject.put("data", msg);
			// jsonObject.put("id", id);
			con.sendMessage(jsonObject.toString());
		}

		public void start() {
			final WebSocketServer server = new WebSocketServer("127.0.0.1", 2024);

			server.setListener(new WebSocketServerListener() {

				@Override
				public void onOpen(WebSocketConnect con) {
					System.out.println("-------- onOpen");
					ChatItem item = new ChatItem(0, con);
					con.sendMessage("Hello, I am java server");

				}

				@Override
				public void onMessage(WebSocketConnect con, String msg) {
					System.out.println("-------- onMessage\n" + msg);

					if (msg.startsWith("{")) {
						ChatItem curItem = null;
						for (int i = 0; i < list_con.size(); i++) {
							ChatItem item = list_con.get(i);
							if (item.getConnect() == con) {
								curItem = item;
							}
						}
						JSONObject jsonObject = new JSONObject(msg);
						String action = jsonObject.optString("action");
						String data = jsonObject.optString("data");
						long id = jsonObject.optLong("id");

						if (action.equals("register")) {
							if (checkUserCon(id)) {
								sendErr(con, "用户已存在");
							} else {
								regUser(id, data);
								saveUserList();
								ChatItem userItem = new ChatItem(id, con);
								//发送提示给其他人
								for(int i=0;i<list_con.size();i++){
									sendPrompt(list_con.get(i).con, "用户"+userItem.getQQ()+"进入房间，当前在线"+(list_con.size()+1)+"人");
								}
								list_con.add(userItem);
								sendHistoryMsg(con);
								sendPrompt(con, "当前在线" + list_con.size() + "人");
							}
						} else if (action.equals("login")) {
							if (checkUser(id, data)) {

								ChatItem userItem = new ChatItem(id, con);
								//发送提示给其他人
								for(int i=0;i<list_con.size();i++){
									sendPrompt(list_con.get(i).con, "用户"+userItem.getQQ()+"进入房间，当前在线"+(list_con.size()+1)+"人");
								}
								list_con.add(userItem);
								sendHistoryMsg(con);
								sendPrompt(con, "登录成功");
								sendPrompt(con, "当前在线" + list_con.size() + "人");
								
							} else {
								sendErr(con, "登录失败");
							}

						} else if (action.equals("sendmsg")) {
							MsgItem msgItem = new MsgItem(action, data, id);
							if (curItem != null) {
								id = curItem.qq;
								if (checkMsg(msgItem)) {
									sendMsgAll(id, data);
									msgItem.id = curItem.qq;
									addHistoryMsg(msgItem);
								} else {
									sendPrompt(con, "消息发送失败");
								}
							}
						} else if (action.equals("sendxml")) {
							MsgItem msgItem = new MsgItem(action, data, id);
							if (curItem != null) {
								id = curItem.qq;
								if (checkMsg(msgItem)) {
									sendXmlAll(id, data);
									msgItem.id = curItem.qq;
									addHistoryMsg(msgItem);
								} else {
									sendPrompt(con, "消息发送失败");
								}
							}
						} else if (action.equals("sendimg")) {
							MsgItem msgItem = new MsgItem(action, data, id);
							if (curItem != null) {
								id = curItem.qq;
								if (checkMsg(msgItem)) {
									sendImgAll(id, data);
									msgItem.id = curItem.qq;
									addHistoryMsg(msgItem);
								} else {
									sendPrompt(con, "消息发送失败");
								}
							}

						} else if (action.equals("exit")) {

						} else if (action.equals("getname")) {
							if (curItem != null) {
								getQQName(con, id);
							}

						}
					}
				}

				@Override
				public void onError(WebSocketConnect con, int code, String error) {
					System.out.println("------- onError " + error);
					for (int i = 0; i < list_con.size(); i++) {
						if (con == list_con.get(i).getConnect()) {
							list_con.remove(i);
							break;
						}
					}
				}

				@Override
				public void onClose(WebSocketConnect con) {
					System.out.println("-------- onClose");
					for (int i = 0; i < list_con.size(); i++) {
						if (con == list_con.get(i).getConnect()) {
							list_con.remove(i);
							break;
						}
					}

				}
			});
			// 创建一个线程 检测是否掉线
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						for (int i = list_con.size()-1; i >=0; i--) {
							WebSocketConnect connect = list_con.get(i).con;
							if (connect.getEndTime() < System.currentTimeMillis() - 60000) {
								System.out.println("用户" + list_con.get(i).qq + "已掉线");
								long iqq = list_con.get(i).qq;
								ChatItem exitItem = list_con.get(i);
								exitItem.con.close();
								list_con.remove(i);
								for(int n=0;n<list_con.size();n++){
									ChatItem item = list_con.get(n);
									sendPrompt(item.con, "用户"+iqq + "已掉线");
								}
								break;
							}
						}
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

				}
			}).start();
			server.start();
		}

	}
}
