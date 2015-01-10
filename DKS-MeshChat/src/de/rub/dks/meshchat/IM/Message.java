package de.rub.dks.meshchat.IM;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable, Comparable<Message> {
	private static final long serialVersionUID = 957528886230338376L;

	private String text;
	private String ID;
	private String nickname;
	private String date;
	private int ID_color;
	private boolean broadcast = false;

	public Message(String text, String ID, String nickname, String date, int color) {
		this.text = text;
		this.ID = ID;
		this.nickname = nickname;
		this.date = date;
		this.ID_color = color;
	}
	
	public Message(String text, String date, int color){
		this.date = date;
		this.ID_color = color;
		this.broadcast = true;
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public String getID() {
		return this.ID;
	}

	public String getNickname() {
		return this.nickname;
	}

	public String getDate() {
		return this.date;
	}

	public int getColor() {
		return this.ID_color;
	}
	
	public boolean isBroadcast() {
		return this.broadcast;
	}
	
	public void setBroadcast() {
		this.broadcast = true;
	}
	
	public byte[] serialize() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream os = new ObjectOutputStream(out);
			os.writeObject(this);
		} catch (IOException e) {
		}
		return out.toByteArray();
	}

	public static Message deserialize(byte[] data) {
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is;
		try {
			is = new ObjectInputStream(in);
			return (Message) is.readObject();
		} catch (Exception e) {
		}
		return null;
	}

	public String toString() {
		return nickname + "[" + ID + "]: " + text + " (" + date + ")";
	}

	public int compareTo(Message another) {
		return date.compareTo(another.date);
	}

}
