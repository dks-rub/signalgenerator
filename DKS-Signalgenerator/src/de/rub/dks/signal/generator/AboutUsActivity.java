package de.rub.dks.signal.generator;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.webkit.WebView;

public class AboutUsActivity extends ActionBarActivity{

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about_us);

		WebView web = new WebView(this);
		setContentView(web);
		
		web.loadData(getString(R.string.html_about_us), "text/html", "utf-8");
	}

}
