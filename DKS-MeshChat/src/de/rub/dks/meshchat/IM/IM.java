package de.rub.dks.meshchat.IM;

import java.util.ArrayList;
import java.util.zip.Inflater;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import de.rub.dks.meshchat.R;

public abstract class IM {
	
	private static ArrayList<Message> messageList = new ArrayList<Message>();
	
	public static boolean pushMessage(Message m){
		try{
			messageList.add(m);
			return true;
		}catch(Exception e){
			Log.d("pushMessage", e.toString());
		}
		return false;
	}
	
	public static Message popMessage(){
		Message m = messageList.get(0);
		messageList.remove(0);
		return m;
	}

	// Queue for Messaging
	public static boolean send_msg(Message m){
		//TODO
		return true;
	}
	
	public static Message rec_msg(){
		//TODO
		return null;
	}
	
//	public static boolean showMessage(Context c, View v, IMessage m, int layout){
//		if(!m.getText().equals("")){
//			LayoutInflater inflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			final View rootView = inflater.inflate(R.layout.activity_chat, null);
//			Log.d("message", (rootView.findViewById(R.id.chat_container)).toString());
//			LinearLayout msg_container = (LinearLayout) rootView.findViewById(R.id.msg_container);
//			
//			//Open layout
//			View msg_layout = inflater.inflate(layout, null);
//			
//			//Edit settings
//			msg_layout.findViewById(R.id.profil).setBackgroundColor(m.getColor());
//			((TextView)msg_layout.findViewById(R.id.nickname)).setText(m.getNickname());
//			((TextView)msg_layout.findViewById(R.id.text)).setText(m.getText());
//			
//			((TextView)msg_layout.findViewById(R.id.date)).setText(m.getDate());
//			
//			//Adding next message and scroll down
//			((EditText)rootView.findViewById(R.id.chat_field)).setText("");
//			msg_container.addView(msg_layout);
//			((ScrollView)rootView.findViewById(R.id.chat_container)).post(new Runnable() {
//				
//				@Override
//				public void run() {
//					((ScrollView)rootView.findViewById(R.id.chat_container)).fullScroll(ScrollView.FOCUS_DOWN);
//					
//				}
//			});
//			Log.d("message", m.getText());
////			try{
////			}catch(Exception e){}
////			return true;
//		}
//		return false;
//	}
	
}
