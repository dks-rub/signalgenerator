package de.rub.dks.meshchat;

public class Globals {
	//Multicast Constants
	public static final int PORT = 6666;
	public static final Integer BUFFER_SIZE = 4096;
	public static final String TAG = "DKS.MULTICAST_APP";
	
	//Shared Prefereces Tags
	public static final String SHARED_PREF = "de.rub.dks.meshchat.sharedPrefereces";
	public static final String ID_DATA = "de.rub.dks.meshchat.ID";
	public static final String ID_COLOR_DATA = "de.rub.dks.meshchat.ID_color";
	public static final String NICKNAME_DATA = "de.rub.dks.meshchat.nickname";
	public static final String FIRST_STARTUP = "de.rub.dks.meshchat.first_startup";
	
	//Status Codes
	public static final Integer FIRST_INIT = 0001;
	public static final Integer RE_INIT = 0002;
	public static final Integer INIT_SUCCESS = 0003;
	public static final Integer INIT_FAIL = 0004;
	
	//Filenames
	public static final String CHAT_HISTORY_FN = "de.rub.dks.meshchat.chat_history.db";
}