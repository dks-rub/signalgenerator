package de.rub.dks.meshchat.IM;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;
import de.rub.dks.meshchat.Globals;

public class MessageSender {
	private DatagramSocket socket;
	private Context context;

	public MessageSender(Context context) {
		this.context = context;
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			Log.d(Globals.TAG, "There was a problem creating the sending socket. Aborting.");
			e.printStackTrace();
		}
	}

	public void sendMessage(final Message m) {
		new Thread() {
			public void run() {
				// Check for WiFi connectivity
				ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if (mWifi == null || !mWifi.isConnected()) {
					Log.d(Globals.TAG, "Sorry! You need to be in a WiFi network in order to send UDP multicast packets. Aborting.");
					return;
				}

				// Build the packet
				DatagramPacket packet = null;
				byte data[] = m.serialize();
				try {
					packet = new DatagramPacket(data, data.length, getMulticastIp(), Globals.PORT);
				} catch (Exception e1) {
					Log.d(Globals.TAG, "There was an error with the ip. Aborted.");
				}

				try {
					if (packet != null)
						socket.send(packet);
				} catch (IOException e) {
					Log.d(Globals.TAG, "There was an error sending the UDP packet. Aborted.");
					e.printStackTrace();
				}
			}
		}.start();
	}

	public InetAddress getMulticastIp() {
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface intf : interfaces) {
				List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
				for (InetAddress addr : addrs) {
					if (!addr.isLoopbackAddress()) {
						String sAddr = addr.getHostAddress().toUpperCase();
						boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
						if (isIPv4) {
							sAddr = sAddr.substring(0, sAddr.lastIndexOf('.') + 1) + "255";
							Log.d(Globals.TAG, "Multicast IP: " + sAddr);
							return InetAddress.getByName(sAddr);
						}
					}
				}
			}
		} catch (Exception e) {
			Log.d(Globals.TAG, "There was an error retrieving your IP. Aborted.");
		}
		return null;
	}

}
