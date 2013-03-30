package com.havenskys.seashepherdfree;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class Start extends TabActivity {
	
	private static String TAG = "Start";
	
	private TabHost mTabHost;
	private Bundle mIntentExtras;
	private NotificationManager mNM;
	//private Handler mHandler;
	
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private Custom mLog;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLog = new Custom(this);
        setContentView(R.layout.start);
        
        mLog.w(TAG,"onCreate() ++++++++++++++++++++++++++++++++");

        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mLog.setNotificationManager(mNM);
        
        mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
  	  	mPreferencesEditor = mSharedPreferences.edit();
  	  	mLog.setSharedPreferences(mSharedPreferences, mPreferencesEditor);
        
        //mHandler = new Handler();
        
        mIntentExtras = getIntent().getExtras();
		long id = mIntentExtras != null ? mIntentExtras.getLong("id") : 0;
		int tab = mIntentExtras != null ? mIntentExtras.getInt("tab") : 0;
        boolean stoprequest = mIntentExtras != null ? mIntentExtras.getBoolean("stoprequest") : false;
        
        //long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",0) : 0;
        
		if( id > 0 ){
			mPreferencesEditor.putLong("id", id); 
		}
		
		if( stoprequest ){
			mPreferencesEditor.putBoolean("stoprequest", false);
		}else{
			serviceRestart("onCreate() 61");
		}
		
		mPreferencesEditor.commit();
	        
        mTabHost = getTabHost();
        
        Intent listView = new Intent(this, listView.class);
        	//listView.putExtra("id", id);
        TabSpec t1 = mTabHost.newTabSpec("list");
        	t1.setIndicator(null, getResources().getDrawable(mLog.TOPICON));
        	t1.setContent(listView);
        mTabHost.addTab(t1);
        
        mTabHost.setCurrentTab(tab);
        
        //Thread getlatestThread = new Thread(){ public void run(){ getlatest(); } };
        //getlatestThread.start();
    	
    }

	@Override
	protected void onStart() {
		mLog.w(TAG,"onStart() ++++++++++++++++++++++++++++++++");
		super.onStart();
	}

	@Override
	protected void onChildTitleChanged(Activity childActivity, CharSequence title) {
		mLog.w(TAG,"onChildTitleChanged() tabCount("+mTabHost.getTabWidget().getChildCount()+") childActivity("+childActivity.getClass().getName()+") title("+title+") ++++++++++++++++++++++++++++++++");
		String name = childActivity.getClass().getName();
		
		
		if( mTabHost.getTabWidget().getChildCount() == 1 ){
        	mNM.cancel(Custom.NOTIFY_ID_ARTICLE);
	        Intent browseView = new Intent(this, browseView.class);
	        	//browseView.putExtra("id", id);
	        TabSpec t2 = mTabHost.newTabSpec("browse");
	        t2.setIndicator(null, getResources().getDrawable(android.R.drawable.ic_menu_compass));
	        t2.setContent(browseView);
	        mTabHost.addTab(t2);
	        
	        /*
	        Intent commentView = new Intent(this, commentView.class);
	        	//commentView.putExtra("id", id);
	        TabSpec t3 = mTabHost.newTabSpec("comment");
	        t3.setIndicator(null, getResources().getDrawable(android.R.drawable.ic_menu_share));
	        t3.setContent(commentView);
	        mTabHost.addTab(t3);
	        //*/
		}
		
		if( name.contains("listView") ){
			long id = Long.parseLong(title.toString());
			mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();
			
			if( id > 0 ){
	        	mNM.cancel(Custom.NOTIFY_ID_ARTICLE);
		        //mTabHost.getChildAt(1).setTag(id);
		        mTabHost.setCurrentTab(1);
		        //mTabHost.getChildAt(2).setTag(id);
			}
		}
		
		super.onChildTitleChanged(childActivity, title);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onContextItemSelected() ");
		return super.onContextItemSelected(item);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onContextMenuClosed() ");
		super.onContextMenuClosed(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onCreateContextMenu() ");
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onCreateOptionsMenu() ");
		
		menu.add(Menu.NONE, 101, 0, "View Source Webpage")
			.setIcon(android.R.drawable.ic_menu_view);
		
		menu.add(Menu.NONE, 202, 1, "Email " + mLog.WHO)
			.setIcon(Custom.LITTLEICON);
		
		menu.add(Menu.NONE, 1, 2, "About DCS")
		.setIcon(R.drawable.icon);
		
		menu.add(Menu.NONE, 2, 3, "Help")
			.setIcon(android.R.drawable.ic_dialog_info);
		
		menu.add(Menu.NONE, 201, 4, "Email Support")
			.setIcon(android.R.drawable.ic_dialog_email);
		
		
		{
			int groupNum = 20;
			SubMenu sync = menu.addSubMenu(Menu.NONE, groupNum, 20, "Synchronization Interval"); //getItem().
			sync.setIcon(android.R.drawable.stat_notify_sync);
			sync.add(groupNum, 0, 0, "On Application Startup");
			sync.add(groupNum, 30, 2, "30 Minutes");
			sync.add(groupNum, 60, 3, "Hourly");
			sync.add(groupNum, 60 * 2, 4, "2 Hours");
			sync.add(groupNum, 60 * 3, 5, "3 Hours");
			sync.add(groupNum, 60 * 4, 6, "4 Hours");
			sync.add(groupNum, 60 * 6, 7, "6 Hours");
			sync.add(groupNum, 60 * 8, 8, "8 Hours");
			sync.add(groupNum, 60 * 12, 9, "12 Hours");
			sync.add(groupNum, 60 * 24, 10, "Daily");
			sync.add(groupNum, 60 * 24 * 2, 11, "2 Days");
			sync.add(groupNum, 60 * 24 * 4, 12, "4 Days");
			sync.add(groupNum, 60 * 24 * 7, 13, "Weekly");
			int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",30) : 30;
			sync.setGroupCheckable(groupNum, true, true);
			sync.setGroupEnabled(groupNum, true);
			
			MenuItem activeitem = null;
			activeitem = sync.findItem(syncInterval);
			if( activeitem == null ){
				if( syncInterval > 0 ){
					sync.add(groupNum, syncInterval, 1, syncInterval + " Minutes");
				}else{
					syncInterval = 30; // Must exist.
				}
				activeitem = sync.findItem(syncInterval);
			}
			activeitem.setChecked(true);
		}
		
		//sync.findItem(11).setChecked(true);
		
		//Intent intent = new Intent(null, getIntent().getData());
	    //intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		//sync.addIntentOptions(2, 12, 0, this.getComponentName(), null, intent, Menu.FLAG_PERFORM_NO_CLOSE | Menu.FLAG_APPEND_TO_GROUP, null);

		{
			int groupNum = 21;
			//int load = (int) mLog.getload();
			SubMenu submenu = menu.addSubMenu(Menu.NONE, groupNum, 20, "Acceptable Load"); //getItem().
			submenu.setIcon(android.R.drawable.stat_notify_sync);
			//syncload.add(groupNum, load, 0, "Your Load is at " + load);
			submenu.add(groupNum, 1, 2, "1 (Healthy)");
			submenu.add(groupNum, 2, 3, "2 (Alright)");
			submenu.add(groupNum, 3, 4, "3 (Okay)");
			submenu.add(groupNum, 4, 5, "4 (Acceptable)");
			submenu.add(groupNum, 5, 6, "5 (Busy)");
			submenu.add(groupNum, 6, 7, "6 (Extremely Busy) ");
			int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;
			submenu.setGroupCheckable(groupNum, true, true);
			submenu.setGroupEnabled(groupNum, true);
			
			MenuItem activeitem = null;
			activeitem = submenu.findItem(syncLoad);
			if( activeitem == null ){
				if( syncLoad > 0 ){
					submenu.add(2, syncLoad, 1, "Load " + syncLoad);
				}else{
					syncLoad = 4; // Must exist.
				}
				activeitem = submenu.findItem(syncLoad);
			}
			activeitem.setChecked(true);
		}

		{
			int groupNum = 22;
			SubMenu submenu = menu.addSubMenu(Menu.NONE, groupNum, 20, "Vibrate"); //getItem().
			submenu.setIcon(android.R.drawable.ic_dialog_alert);
			submenu.add(groupNum, 3, 2, "NO _");
			submenu.add(groupNum, 2, 3, "YES ++");
			submenu.add(groupNum, 1, 3, "YES ++_+");
			submenu.add(groupNum, 4, 4, "YES +_++");
			int syncvib = mSharedPreferences.contains("syncvib") ? mSharedPreferences.getInt("syncvib",1) : 1;
			submenu.setGroupCheckable(groupNum, true, true);
			submenu.setGroupEnabled(groupNum, true);
			
			MenuItem activeitem = null;
			activeitem = submenu.findItem(syncvib);
			if( activeitem == null ){
				syncvib = 1; // Must exist.
				activeitem = submenu.findItem(syncvib);
			}
			activeitem.setChecked(true);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		mLog.w(TAG, "onOptionsItemSelected() groupid(" + item.getGroupId() + ") itemid("+item.getItemId()+") title("+item.getTitle()+")");
		if( item.getGroupId() == 20 ){ // Synchronize Timeline
			int itemvalue = item.getItemId(); // minutes
			mLog.w(TAG, "Updating Synchronization Interval(" + itemvalue + ")");
			mPreferencesEditor.putInt("sync", itemvalue); mPreferencesEditor.commit();
			if( ! item.isChecked() ){ item.setChecked(true); serviceRestart("onOptionsItemSelected() sync[interval]");}
			return true;
		}
		if( item.getGroupId() == 21 ){ // Acceptable Load
			int itemvalue = item.getItemId(); // minutes
			mLog.w(TAG, "Updating Synchronization Interval(" + itemvalue + ")");
			mPreferencesEditor.putInt("syncload", itemvalue); mPreferencesEditor.commit();
			if( ! item.isChecked() ){ item.setChecked(true); serviceRestart("onOptionsItemSelected() syncload");}
			return true;
		}
		if( item.getGroupId() == 22 ){ // Vibrate
			int itemvalue = item.getItemId(); // minutes
			mLog.w(TAG, "Updating Vibrate(" + itemvalue + ")");
			mPreferencesEditor.putInt("syncvib", itemvalue); mPreferencesEditor.commit();
			if( ! item.isChecked() ){ item.setChecked(true); serviceRestart("onOptionsItemSelected() syncvib");}
			return true;
		}
		
		switch(item.getItemId()){
		case 1: //About
			{
				Intent jump = new Intent(this, About.class);
				startActivity(jump);
			}
			break;
		case 2: //Help
			{
				Intent jump = new Intent(this, Help.class);
				startActivity(jump);
			}
			break;
		case 101:
			{
				Intent jump = new Intent(Intent.ACTION_VIEW, Uri.parse(Custom.BASEURL));
				startActivity(jump);
			}
			break;
		case 201:
			{
				Intent jump = new Intent(Intent.ACTION_SEND);
				jump.putExtra(Intent.EXTRA_TEXT, "This is a request for special help.  Contained in this request are the only details to help diagnose and understand an issue.\n\n\n\n");
				jump.putExtra(Intent.EXTRA_EMAIL, new String[] {"\""+ Custom.APP + " Support\" <havenskys@gmail.com>"} ); 
				jump.putExtra(Intent.EXTRA_SUBJECT, "Support Request: " + Custom.APP);
				jump.setType("message/rfc822"); 
				startActivity(Intent.createChooser(jump, "Email"));
			}
			break;
		case 202:
			{
				Intent jump = new Intent(Intent.ACTION_SEND);
				jump.putExtra(Intent.EXTRA_TEXT, "");
				jump.putExtra(Intent.EXTRA_EMAIL, new String[] {"\""+Custom.WHO + "\" <"+Custom.EMAIL+">"});
				jump.putExtra(Intent.EXTRA_SUBJECT, "Hello");
				jump.setType("message/rfc822"); 
				startActivity(Intent.createChooser(jump, "Email"));
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void serviceRestart(String who) {
		
		final Context ctx = this;
		//mPreferencesEditor.putBoolean("servicerestart", true); mPreferencesEditor.commit();
		
		mLog.w(TAG, "serviceRestart() from " + who);
		Thread s = new Thread(){
			public void run(){
				SystemClock.sleep(1880);
				
			    Intent service = new Intent();
				service.setClass(ctx, SyncService.class);
			    stopService(service);
			    startService(service);
			}
		};
		s.setPriority(Thread.MIN_PRIORITY);
		s.start();
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		// TODO Auto-generated method stub
		mLog.w(TAG, "onOptionsMenuClosed()");
		super.onOptionsMenuClosed(menu);
	}



}










