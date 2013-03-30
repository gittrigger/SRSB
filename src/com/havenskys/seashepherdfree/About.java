package com.havenskys.seashepherdfree;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

public class About extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
		this.setContentView(R.layout.about);
		this.getWindow().setFeatureDrawable(Window.FEATURE_LEFT_ICON, getResources().getDrawable(R.drawable.icon));
		this.setTitle("About");
		//this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		
		
		
		
		
		
		
		WebView view = (WebView) findViewById(R.id.about_view);
		view.getSettings().setJavaScriptEnabled(true);
		view.getSettings().setSupportZoom(true);
		
		
		
		Custom mLog = new Custom(this);
		double load = mLog.getload();
		
		String body = "";
		body += "<html><body>";
		body += "<h2>Doc Chomps Software</h2>\n";
		body += "This software was written by Haven Skys in Seattle, WA and Northern Minnesota.  You may contact me for a custom solution at havenskys@gmail.com, mention 'Custom Solution Request' in your email subject.  There is more information about this application in 'Help'.";
		body += "<br>Doc Chomps is my American Pitbull/Boxer companion.  Super smart, tiger stripped, and more friendly than I am.";
		body += "<h2>System Info</h2>\n";
		body += "Current Load: " + load;
		body += "</body></html>";
		view.loadData(body, "text/html", "UTF-8");
	}

	
}
