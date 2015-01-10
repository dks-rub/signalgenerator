package de.rub.dks.meshchat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener{
	
	private SharedPreferences sPref;
	private OnPreferenceChangeListener nicklistener;
	private EditTextPreference p;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		sPref = this.getSharedPreferences(Globals.SHARED_PREF, Context.MODE_PRIVATE);
		
		p = (EditTextPreference) findPreference(Globals.NICKNAME_DATA);
		nicklistener = new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				SharedPreferences.Editor editor = sPref.edit();
				editor.putString(Globals.NICKNAME_DATA, (String) newValue);
				Log.d(Globals.TAG, ""+newValue.toString());
				editor.commit();
				return true;
			}
		};
	}
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
        nicklistener.onPreferenceChange(p, p.getText());
        // Registers a callback to be invoked whenever a user changes a preference.
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
	@Override
    protected void onPause() {
        super.onPause();
        nicklistener.onPreferenceChange(p, p.getText());
        // Unregisters the listener set in onResume().
        // It's best practice to unregister listeners when your app isn't using them to cut down on
        // unnecessary system overhead. You do this in onPause().
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
    
    @SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		
		if(key.equals(Globals.NICKNAME_DATA)){
			EditTextPreference p = (EditTextPreference) findPreference(Globals.NICKNAME_DATA);
			String nickname = p.getText();
			editor.putString(Globals.NICKNAME_DATA, nickname);
			editor.commit();
		}
		
	}
}
