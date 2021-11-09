import com.xl.websocket.WebSocketServer;
import com.xl.websocket.WebUpgradleUtil;

public class SocketServerMain {

	public static void main(String[] args) {
		String re = WebUpgradleUtil.WebSocketKey("kMgvb6KivsYVl2EHinJHZg==","258EAFA5-E914-47DA-95CA-C5AB0DC85B11");
		System.out.println(re);
		WebSocketServer server = new WebSocketServer("127.0.0.1", 2024);
		server.start();
		
		
		
	}
	
	public static byte[] maskBytes(byte[] data, byte[] maskKey) {

		int ptr = 0;
		byte temp1;
		byte temp2;
//		data[ptr++] = maskKey[0];
//		data[ptr++] = maskKey[1];
//		data[ptr++] = maskKey[2];
//		data[ptr++] = maskKey[3];
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
