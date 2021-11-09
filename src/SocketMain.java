import java.awt.EventQueue;

import com.xl.window.SocketTestWindow;
import com.xl.window.WebSocketTestWindow;

import android.os.Looper;

public class SocketMain {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Looper.prepare(true);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					WebSocketTestWindow window = new WebSocketTestWindow();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Looper.loop();
	}
}
