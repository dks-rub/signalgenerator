package de.rub.dks.meshchat.account;

import java.security.MessageDigest;
import java.util.Random;

import android.text.format.Time;
import android.util.Log;

public abstract class Account {
	
	private static int ID_color = 0;
	private static String ID = null;
	
	public static void createAccount(){
		Time time = new Time(Time.getCurrentTimezone());
		time.setToNow();
		Random r = new Random(time.toMillis(false));
		
		MessageDigest md = null;
		try{
			md = MessageDigest.getInstance("SHA-512");
		}catch(Exception E){
			Log.d("hash", E.toString());
		}
		if(md != null) {
			String hash = ""+r.nextLong();
			for(int i = 0; i < 100; i++){
				md.update(hash.getBytes());
				hash += md.digest()+""+r.nextLong();
			}
			
			ID = md.digest().toString();
			ID_color = Integer.parseInt(ID.substring(3), 16)+Integer.MAX_VALUE;
		}
		return;
	}
	
	public static int getColor(){
		return ID_color;
	}
	
	public static String getID(){
		return ID;
	}
	
	public static void setID(String newID){
		ID = newID;
	}
	
	public static void setColor(int color){
		ID_color = color;
	}

}
