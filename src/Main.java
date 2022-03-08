import java.awt.EventQueue;

import com.xl.window.SocketTestWindow;


import android.os.Looper;

public class Main {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		Looper.prepare(true);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					
					SocketTestWindow window = new SocketTestWindow();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		Looper.loop();
	}
}
