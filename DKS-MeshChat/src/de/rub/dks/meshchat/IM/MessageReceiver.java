package de.rub.dks.meshchat.IM;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Handler;
import android.util.Log;
import de.rub.dks.meshchat.Globals;

public class MessageReceiver {
	private Thread thread;
	private Runnable processRunnable;
	private Handler handler;
	private Message lastMsg;
	private Context context;

	public MessageReceiver(Context context, Handler handler, Runnable onNewMessage) {
		processRunnable = onNewMessage;
		this.handler = handler;
		this.context = context;
	}

	public void start() {
		if (thread != null)
			thread.interrupt();
		startThread();
	}

	public void stop() {
		if (thread != null)
			thread.interrupt();
		thread = null;
	}

	private void startThread() {
		thread = new Thread() {
			public void run() {
				WifiManager wim = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				if (wim != null) {
					MulticastLock mcLock = wim.createMulticastLock(Globals.TAG);
					mcLock.acquire();
				}
				byte[] buffer = new byte[Globals.BUFFER_SIZE];
				DatagramPacket rPacket = new DatagramPacket(buffer, buffer.length);
				MulticastSocket rSocket;
				try {
					rSocket = new MulticastSocket(Globals.PORT);
				} catch (IOException e) {
					Log.d("MessageReceiver", "Impossible to create a new MulticastSocket on port " + Globals.PORT);
					e.printStackTrace();
					return;
				}

				while (!isInterrupted()) {
					Log.d("MessageReceiver", "Waiting for Messages...");
					try {
						rSocket.receive(rPacket);
					} catch (IOException e1) {
						Log.d("MessageReceiver", "There was a problem receiving the incoming message.");
						continue;
					}
					if (isInterrupted())
						break;
					Log.d("MessageReceiver", "Received Message! Processing...");
					byte data[] = rPacket.getData();
					try {
						lastMsg = Message.deserialize(data);
					} catch (IllegalArgumentException ex) {
						Log.d("MessageReceiver", "There was a problem processing the message " + Arrays.toString(data));
						continue;
					}
					handler.post(processRunnable);
				}
				rSocket.close();
			}
		};
		thread.start();
	}
	
	
	//TODO: change to queue to avoid message lost
	public Message getMessage() {
		return lastMsg;
	}
	
	public void receive(Message m){
		lastMsg = m;
		handler.post(processRunnable);
	}
}
