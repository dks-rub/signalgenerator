package de.rub.dks.meshchat;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import de.rub.dks.meshchat.IM.Message;
import de.rub.dks.meshchat.IM.MessageReceiver;
import de.rub.dks.meshchat.IM.MessageSender;
import de.rub.dks.meshchat.account.Account;

public class ChatActivity extends Activity {

	// Persistent Settings
	private SharedPreferences sPref;
	
	// Account
	private String ID = null;
	private int ID_color = 0;
	private String nickname = null;
	 
	//Shows if Activity is watched
//	private boolean activity_active = true;
	
	//Intents for notification
//	private Intent intent = null;
//	private PendingIntent pIntent = null;
	
	//Chat internals
	private MessageReceiver receiver;
	private MessageSender sender;
	private EditText msg;
	private ArrayList<Message> messages;
	private Button snd_bt;
	private Time time = new Time(Time.getCurrentTimezone());
	private ScrollView chat;
	
	//Init for checking whether the account is created or not
	private void init(SharedPreferences.Editor editor) {
		//Account is not created
		if (sPref.getBoolean(Globals.FIRST_STARTUP, true)) {
			//Create the account
			Account.createAccount();
			this.ID = Account.getID();
			this.ID_color = Account.getColor();
			editor.putInt(Globals.ID_COLOR_DATA , this.ID_color);
			editor.putBoolean(Globals.FIRST_STARTUP, false);
			editor.apply();
		}
		//Restore default or previously saved values
		this.ID = sPref.getString(Globals.ID_DATA, ID);
		this.ID_color = sPref.getInt(Globals.ID_COLOR_DATA, ID_color);
		this.nickname = sPref.getString(Globals.NICKNAME_DATA, this.ID);
		editor.putString(Globals.ID_DATA, ID);
		editor.putString(Globals.NICKNAME_DATA, nickname);
		editor.putInt(Globals.ID_COLOR_DATA, ID_color);
		editor.commit();
		
		try {
			findViewById(R.id.profil).setBackgroundColor(ID_color);
			((TextView) findViewById(R.id.nickname)).setText(nickname);
		} catch (Exception e) {
			Log.d(Globals.TAG, e.toString());
		}
		return;
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		if(resultCode == Globals.INIT_SUCCESS && requestCode == Globals.FIRST_INIT){
			if(data.hasExtra("nickname")){
				SharedPreferences.Editor editor = sPref.edit();
				
				Message m = new Message(data.getExtras().getString("nickname")+getString(R.string.new_person), time.format("%d.%m.%y, %k:%M:%S"), ID_color);
				
				editor.putString(Globals.NICKNAME_DATA, data.getExtras().getString("nickname"));
				editor.commit();
				init(editor);
				
				//Broadcast new person is in the chat
				messages.add(m);
				sender.sendMessage(m);
			}
			else {
				finish();
			}
		}
		else if(resultCode == Globals.INIT_SUCCESS && requestCode == Globals.RE_INIT){
			if(data.hasExtra("nickname")){
				SharedPreferences.Editor editor = sPref.edit();
				
				Message m = new Message(this.nickname+getString(R.string.change_nickname)+data.getExtras().getString("nickname"), time.format("%d.%m.%y, %k:%M:%S"), ID_color);
				
				editor.putString(Globals.NICKNAME_DATA, data.getExtras().getString("nickname"));
				editor.putBoolean(Globals.FIRST_STARTUP, true);
				editor.commit();
				init(editor);
				
				//Broadcast name change to chat
				messages.add(m);
				sender.sendMessage(m);
			}
		} else if(resultCode == Globals.INIT_FAIL){
			Log.d(Globals.TAG, Globals.INIT_FAIL+": Intent failed");
		}
	}

	private View getMessageView(Message m) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		//Creates different views, for own and received messages
		View layout_msg = null;
		if(m.isBroadcast()) {
			layout_msg = inflater.inflate(R.layout.brc_msg, null);
			layout_msg.findViewById(R.id.brc_container).setBackgroundColor(m.getColor());
			((TextView) layout_msg.findViewById(R.id.brc_message)).setText(m.getText());
			return layout_msg;
		}
		
		if (m.getID().equals(ID)) {
			layout_msg = inflater.inflate(R.layout.snd_msg, null);
		} else {
			layout_msg = inflater.inflate(R.layout.rec_msg, null);
		}
		//Adding content to the view
		layout_msg.findViewById(R.id.profil).setBackgroundColor(m.getColor());
		((TextView) layout_msg.findViewById(R.id.nickname)).setText(m.getNickname());
		((TextView) layout_msg.findViewById(R.id.text)).setText(m.getText());
		((TextView) layout_msg.findViewById(R.id.date)).setText(m.getDate());

		// Adding next message and scroll down
		return layout_msg;

	}

	//TODO: Build a database for all messages stored, for persistent data sets
	public void refreshMessages() {
		LinearLayout msg_container = (LinearLayout) findViewById(R.id.msg_container);
		
//		boolean check_read=false, check_write=false;
//		StorageHelper<Message> storage = new StorageHelper<Message>(getApplicationContext());
//
//		try {
//			check_read = storage.read(Globals.CHAT_HISTORY_FN);
//		} catch (StreamCorruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		try {
//			check_write = storage.write(messages);
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		if(check_read && check_write){
//			Log.d(Globals.TAG, "messages!");
//			messages = storage.getList();
//		}
		
		
		
		if (messages.size() < 2 || (messages.get(messages.size() - 1).compareTo(messages.get(messages.size() - 2)) >= 0)) {
			msg_container.addView(getMessageView(messages.get(messages.size() - 1)));
		} else {
			Collections.sort(messages);
			msg_container.removeAllViews();
			for (Message m : messages)
				msg_container.addView(getMessageView(m));
		}
		
		chat.post(new Runnable() {
			public void run() {
				chat.fullScroll(ScrollView.FOCUS_DOWN);
			}
		});
	}

	//Main window
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		//List of all messages
		messages = new ArrayList<Message>();
		
		Handler handler = new Handler(Looper.getMainLooper());
		
		//Intents for notification
//		intent = new Intent(this, ChatActivity.class);
//	    pIntent = PendingIntent.getActivity(this, 0, intent, 0);
	    
	    //Receiver checks for new incoming messages
		receiver = new MessageReceiver(getApplicationContext(), handler, new Runnable() {
			public void run() {
				Message m = receiver.getMessage();
				if (m == null)
					return;
				Log.d(Globals.TAG, "Received: " + m.toString());
				if (ID.equals(m.getID())) {
					Log.d(Globals.TAG, "Dropped own message");
					return;
				}
				messages.add(m);
				refreshMessages();
				
				//Should notify the user, when a message arrives.
				//TODO: Make sure that only triggered when window not active
//				if(!activity_active){
//				Notification noti = new Notification.Builder(getApplicationContext())
//			        .setContentTitle("Neue Nachricht von "+m.getNickname())
//			        .setContentText(m.getText()).setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent).build();
//			    NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//			    // hide the notification after its selected
//			    noti.flags |= Notification.FLAG_AUTO_CANCEL;
//
//			    notificationManager.notify(0, noti);
//				}
				
			}
		});
		receiver.start();
		sender = new MessageSender(getApplicationContext());

		//Toast.makeText(getApplicationContext(), "Multicast IP: " + sender.getMulticastIp(), Toast.LENGTH_LONG).show();
		setTitle(getTitle().toString()+" - "+sender.getMulticastIp());
		
		//setting up the sharedPreferences
		sPref = this.getSharedPreferences(Globals.SHARED_PREF, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sPref.edit();
		
		if(sPref.getBoolean(Globals.FIRST_STARTUP, true)){
			//Open the dialog to create a new account
			Intent i = new Intent(this, AccountActivity.class);
		    startActivityForResult(i,Globals.FIRST_INIT);
		}
		// Init or restore current profile settings
		init(editor);
		
		
		msg = (EditText) findViewById(R.id.chat_field);
		snd_bt = (Button) findViewById(R.id.send_bt);
		chat = (ScrollView) findViewById(R.id.chat_container);

		snd_bt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (msg.getText().toString().trim().equals(""))
					return;
				time.setToNow();
				Message m = new Message(msg.getText().toString().trim(), ID, nickname, time.format("%d.%m.%y, %k:%M:%S"), ID_color);
				messages.add(m);
				msg.setText("");
				Log.d(Globals.TAG, "Sending: " + m.toString());
				refreshMessages();
				sender.sendMessage(m);
			}
		});
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
	   super.onRestoreInstanceState(savedState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
	   super.onSaveInstanceState(outState);
	}
	
	//Create menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.chat, menu);
		return true;
	}
	//Menu internals
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			Intent i = new Intent(this, SettingsActivity.class);
//		    startActivity(i);
//			return true;
//		} else 
		if(id == R.id.action_account) {
			Intent i = new Intent(this, AccountActivity.class);
			startActivityForResult(i, Globals.RE_INIT);
			return true;
		} else if(id == R.id.action_leave){
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void onResume(){
//		activity_active = true;
//		Message m = new Message(this.nickname+" is now in the chat.", time.format("%d.%m.%y, %k:%M:%S"), ID_color);
//		sender.sendMessage(m);
		super.onResume();
	}
	
	public void onPause(){
//		activity_active = false;
//		Message m = new Message(this.nickname+getString(R.string.leave), time.format("%d.%m.%y, %k:%M:%S"), ID_color);
//		sender.sendMessage(m);
		super.onPause();
	}
	
	public void finish(){
		receiver.stop();
		if(!sPref.getBoolean(Globals.FIRST_STARTUP, true)){
			Message m = new Message(this.nickname+getString(R.string.leave), time.format("%d.%m.%y, %k:%M:%S"), ID_color);
			messages.add(m);
			sender.sendMessage(m);
		} 
		super.finish();
	}
	
	public void onDestroy() {
		receiver.stop();
//		activity_active = false;
		super.onDestroy();
	}
}
