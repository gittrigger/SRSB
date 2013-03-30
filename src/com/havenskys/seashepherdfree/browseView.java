package com.havenskys.seashepherdfree;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class browseView extends Activity {

	private static String TAG = "Browse";
	
	private WebView mSummary, mContent;
	private TextView mTitle, mDate;
	private LinearLayout mLinearLayout;
	private String mLink;
	//private Bundle mIntentExtras;
	private long mCurrentID = 0;
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private Custom mLog;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLog = new Custom(this);
		
		setContentView(R.layout.browser);
		
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
        mPreferencesEditor = mSharedPreferences.edit();
        long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",0) : 0;
        //mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();


		//mIntentExtras = getIntent().getExtras();
		//long id = mIntentExtras != null ? mIntentExtras.getLong("id") : 0;
		
		mLinearLayout = (LinearLayout) this.findViewById(R.id.browser);
		mTitle = (TextView) this.findViewById(R.id.browser_title);
		mDate = (TextView) this.findViewById(R.id.browser_date);
		mContent = (WebView) this.findViewById(R.id.browser_viewer);
		
		loadRecord(id);
		
	}


    private void loadRecord(long id) {
    	
    	
    	if( id == mCurrentID ){ return; }
    	
    	mTitle.setText("");
		mDate.setText("");
    	mContent.loadData("<html><body bgcolor=#FFFFFF><font size=4><center>Loading</center></font></body></html>", "text/html", "UTF-8");
    	
    	mCurrentID = id;
		Cursor lCursor = SqliteWrapper.query(this, getContentResolver(), DataProvider.CONTENT_URI, 
        		//new String[] { "_id", "address", "body", "strftime(\"%Y-%m-%d %H:%M:%S\", date, \"unixepoch\", \"localtime\") as date" },
        		//strftime("%Y-%m-%d %H:%M:%S"
        		new String[] {"_id", "title", "link", "datetime(date,'localtime')", "content"  },
				//new String[] { "_id", "address", "body", "date" },
        		"_id = " + id,
        		null, 
        		null);
		
		if( lCursor != null ){
			startManagingCursor(lCursor);
			if ( lCursor.moveToFirst() ){
				String title = null;
				String link = null;
				String date = null;
				String content = null;
				
				if( lCursor.getColumnCount() == 5 ){/// <<<<<<<<<<<<<<<<<  LOOK HERE
					title = lCursor.getString(1) != null ? lCursor.getString(1) : "";
					link = lCursor.getString(2) != null ? lCursor.getString(2) : "";
					date = lCursor.getString(3) != null ? lCursor.getString(3) : "";
					content = lCursor.getString(4) != null ? lCursor.getString(4) : "";
					
					mLog.w(TAG,"Found rowid("+id+") title("+title+")");
					mTitle.setText(title);
					mDate.setText(date);

					//mContent.getSettings().supportMultipleWindows();
					mContent.getSettings().setJavaScriptEnabled(true);
					mContent.loadDataWithBaseURL(link, content, "text/html", "UTF-8", link);
					mContent.getSettings().setSupportZoom(true);
					
					mLink = link;

					ContentValues cv = new ContentValues();
					cv.put("status", 2);
					SqliteWrapper.update(this, getContentResolver(), DataProvider.CONTENT_URI, cv, "_id = " + id, null);
				}
			}
			//mBrowser.addJavascriptInterface(new AndroidBridge(), "android");
		}
	}


	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onCreatePanelMenu(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, 401, 0, "View Article Link")
			.setIcon(android.R.drawable.ic_menu_view);
        menu.add(0, 402, 0, "Forward")
			.setIcon(android.R.drawable.ic_dialog_email);
		return super.onCreatePanelMenu(featureId, menu);
	}


	@Override
	public View onCreatePanelView(int featureId) {
		// TODO Auto-generated method stub
		return super.onCreatePanelView(featureId);
	}

	


	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		mLog.w(TAG,"onMenuItemSelected()");
		return super.onMenuItemSelected(featureId, item);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mLog.w(TAG,"onOptionsItemSelected()");
		
		final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		final String link = mLink; 
		final String title = mTitle.getText().toString(); 

    	
		switch(item.getItemId()){
		case 401:
			Intent d = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
			startActivity(d);
			break;
		case 402:
			{
				Intent jump = new Intent(Intent.ACTION_SEND);
				jump.putExtra(Intent.EXTRA_TEXT, "I found something of interest.\n\n" + title + "\n" +link + "\n\n\n"); 
				jump.putExtra(Intent.EXTRA_SUBJECT, "FW: " + mLog.APP + " (" + title + ")");
				jump.setType("message/rfc822"); 
				startActivity(Intent.createChooser(jump, "Email"));
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		mLog.w(TAG,"onConfigurationChanged() ++++++++++++++++++++++++++++++++");
		super.onConfigurationChanged(newConfig);
	}


	@Override
	protected void onRestart() {
		mLog.w(TAG,"onRestart() ++++++++++++++++++++++++++++++++");
		super.onRestart();
	}


	@Override
	protected void onResume() {
		mLog.w(TAG,"onResume() ++++++++++++++++++++++++++++++++");
		super.onResume();
		long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",0) : 0;
		loadRecord(id);
	}


	@Override
	protected void onStart() {
		mLog.w(TAG,"onStart() ++++++++++++++++++++++++++++++++");
		super.onStart();
	}

    
}

