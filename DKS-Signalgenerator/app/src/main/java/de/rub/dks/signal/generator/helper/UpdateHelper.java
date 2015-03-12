package de.rub.dks.signal.generator.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import de.rub.dks.signal.generator.R;

public class UpdateHelper {
	private final static String SERVERNAME = "http://pgfplots.dks.ruhr-uni-bochum.de/update_android.php"; // Update webservice
	private final static String SERVER = "https://www.dks.ruhr-uni-bochum.de/de/"; // Update website
	private final static String VALUE_KEY = "version";
	
	private Context context;
	private String appName;
	private Handler mHandler;

	public UpdateHelper(Context context, String appName, Handler mHandler) {
		this.mHandler = mHandler;
		this.appName = appName;
		this.context = context;
	}

	// Update function: POST-Request to server, server answers with newest
	// version of app.
	private String updateNeed() {
		String error = "error";
		try {
			// setup POST-Request
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(SERVERNAME);
			List<NameValuePair> pairs = new ArrayList<NameValuePair>(1);
			pairs.add(new BasicNameValuePair(VALUE_KEY, appName));
			post.setEntity(new UrlEncodedFormEntity(pairs));
			try {
				HttpResponse response = client.execute(post);
				// Parse for body element
				if (response.getEntity().getContentLength() != 0) {
					StringBuilder sb = new StringBuilder();
					try {
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(response.getEntity()
										.getContent()), 65728);
						String line = null;

						while ((line = reader.readLine()) != null) {
							sb.append(line);
						}
					} catch (IOException e) {
						Log.d("Error", e.toString());
						return error;
					} catch (Exception e) {
						Log.d("Error", e.toString());
						return error;
					}
					// return body element
					return sb.toString();
				}
			} catch (Exception e) {
				Log.d("Error", e.toString());
				return error;
			}
		} catch (Exception e) {
			Log.d("Error", e.toString());
			return error;
		}
		return error;
	}
	
	//Start the thread for the update check
	public void startThread(){
		try {
			new Thread(new Runnable() {
				String serverVersion = context.getString(R.string.update_error);
				//get version number
				String versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
				@Override
				public void run() {
					//get update
					serverVersion = updateNeed();
					mHandler.post(new Runnable() {
						
						//Work on UI thread
						@Override
						public void run() {
							if(!serverVersion.equals("error")){
								double server_version = Double.parseDouble(serverVersion);
								double current_version = Double.parseDouble(versionName);
								if(current_version < server_version){
									try {
										AlertDialog.Builder aDialogBuilder = new AlertDialog.Builder(context);
										aDialogBuilder.setTitle(context.getString(R.string.update_title)+" "+appName);
										//Accept
										aDialogBuilder.setMessage(context.getString(R.string.update_needed)).setPositiveButton(context.getString(R.string.alert_update), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												//Open Activity for Update Website
												Intent i = new Intent(Intent.ACTION_VIEW);
												i.setData(Uri.parse(SERVER));
												context.startActivity(i);
											}
										//Decline
										}).setNegativeButton(context.getString(R.string.alert_decline), new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												//close dialog
												dialog.cancel();
											}
										});
										AlertDialog aDialog = aDialogBuilder.create();
										aDialog.show();
									} catch (Exception e) {
										Log.d("Error", e.toString());
									}
								}
							}else{
								Toast.makeText(context, context.getString(R.string.update_error), Toast.LENGTH_LONG).show();
							}
						}
					});
				}
			}).start();
		} catch (NameNotFoundException e) {
			Log.d("Error", e.toString());
		}
	}
	
}
