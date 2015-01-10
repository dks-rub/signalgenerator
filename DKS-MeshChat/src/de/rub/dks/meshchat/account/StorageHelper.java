package de.rub.dks.meshchat.account;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import de.rub.dks.meshchat.Globals;

public class StorageHelper<h>{
	
	private String state = null;
	private ArrayList<h> list = null;
	private Context context = null;
	
	public StorageHelper(Context c){
		this.state = Environment.getExternalStorageState();
		this.context = c;
	}
	
	//Save ArrayList to external Storage
	public boolean write(ArrayList<h> element) throws IOException{
		this.state = Environment.getExternalStorageState();
		//Only readable
		if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			Log.d(Globals.TAG+".StorageHelper.write()", "Storage read only!");
			return false;
		}
		//Read and write
		else if(Environment.MEDIA_MOUNTED.equals(state)) {
			Log.d(Globals.TAG+".StorageHelper.write()", "Storage read and write!");
			
			FileOutputStream file_out= context.getApplicationContext().openFileOutput(Globals.CHAT_HISTORY_FN, Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(file_out);
			oos.writeObject(element);
			oos.close();
			file_out.close();
			
			return true;
		}
		else {
			Log.d(Globals.TAG+".StorageHelper.write()", "Storage not mounted!");
			return false;
		}
	}
	
	//Read and reset ArrayList to list
	public boolean read(String filename) throws StreamCorruptedException, IOException, ClassNotFoundException{
		this.state = Environment.getExternalStorageState();
		//Only readable
		if(Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
			Log.d(Globals.TAG+".StorageHelper.read()", "Storage read only!");
			return false;
		}
		//Read and write
		else if(Environment.MEDIA_MOUNTED.equals(state)) {
			Log.d(Globals.TAG+".StorageHelper.read()", "Storage read and write!");
			
			FileInputStream file_in= new FileInputStream (filename);
			ObjectInputStream ois = new ObjectInputStream(file_in);
			this.list = (ArrayList<h>)ois.readObject();
			ois.close();
			file_in.close();
			
			return true;
		}
		else {
			Log.d(Globals.TAG+".StorageHelper.read()", "Storage not mounted!");
			return false;
		}
	}
	
	public ArrayList<h> getList(){
		return this.list;
	}
}
