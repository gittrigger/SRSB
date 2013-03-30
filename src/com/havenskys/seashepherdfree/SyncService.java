package com.havenskys.seashepherdfree;

import org.apache.http.client.methods.HttpGet;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;

public class SyncService extends Service implements Runnable {

	private static String TAG = "Service";
	
	private Handler mHandler;
	private NotificationManager mNM;
	private Custom mLog;
	
	@Override
	public void onCreate() {
		super.onCreate();
		mLog = new Custom(this);
		mLog.w(TAG,"onCreate() ++++++++++++++++++++++++++++++++++");
		
        mHandler = new Handler();
        
        Thread thr = new Thread(null, this, Custom.APP + "_service_thread");
        thr.start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		mLog.w(TAG,"onBind() ++++++++++++++++++++++++++++++++++");
		return null;
	}

	public void run() {
		mLog.w(TAG,"run() ++++++++++++++++++++++++++++++++++");
		getlatest();
	}

	@Override
	public void onDestroy() {
		mLog.w(TAG,"onDestroy() ++++++++++++++++++++++++++++++++++");
		mNM.cancel(Custom.NOTIFY_ID);
		super.onDestroy();
	}


	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mLog.w(TAG,"onStart() ++++++++++++++++++++++++++++++++++");
	}

	
	
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	
	private void getlatest() {
		mLog = new Custom(this);
		mLog.w(TAG,"getlatest() ++++++++++++++++++++++++++++++++++");
		
		
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mLog.setNotificationManager(mNM);
		mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
		mPreferencesEditor = mSharedPreferences.edit();
		mLog.setSharedPreferences(mSharedPreferences,mPreferencesEditor);
  	  	
		mNM.cancel(Custom.NOTIFY_ID);
		
        //String mUuid = UUID.randomUUID().toString();
        //content://settings/system/notification_sound
        //for (Account account1 : accountsWithNewMail.keySet()) { if (account1.isVibrate()) vibrate = true; ringtone = account1.getRingtone(); }

        //BASEURL = "http://www.seashepherd.org/news-and-media/sea-shepherd-news/feed/rss.html";
        //BASEURL = "http://www.whitehouse.gov/blog/";
        // This will block until load is low or time limit exceeded
		
	
		

  	  	
		int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;
		int syncInterval = mSharedPreferences.contains("sync") ? mSharedPreferences.getInt("sync",30) : 30;
		
        mLog.loadLimit(TAG + " getlatest() 107", syncLoad, 5 * 1000, 30 * 1000);
        
        	
		String httpPage = getBasePage(Custom.BASEURL);
		if( httpPage.length() == 0 ){
			
			
			int failcnt = mSharedPreferences.contains("syncfail") ? mSharedPreferences.getInt("syncfail",0) : 0;
			failcnt++;
			
			int wait = failcnt * 2;
			if( wait > syncInterval ){ wait = syncInterval; }
			mLog.i(TAG,"getBasePage("+Custom.BASEURL+") failed count("+failcnt+"), waiting "+wait+" minutes before retrying.");
			mLog.setServiceNotification(TAG + " getlatest() 107", android.R.drawable.presence_offline, Custom.APP + " (Press here to Stop)", "Retry in "+wait+" minutes.", Custom.APP + " Download Failure, I will retry in "+wait+" minutes.");
			mHandler.postDelayed( this, 1000 * 60 * wait );
			
			mPreferencesEditor.putInt("syncfail", failcnt).commit();
		}else{
			mLog.parseEntries(TAG + " getlatest() 90", httpPage);
			mLog.i(TAG,"getlatest() 83 returned from parseEntries(). stopping NOTIFY");
			mNM.cancel(Custom.NOTIFY_ID);
			mLog.i(TAG,"getlatest() 85 setting postDelay");
			if( syncInterval > 0 ){ mHandler.postDelayed( this, 1000 * 60 * syncInterval ); }
			mPreferencesEditor.putInt("syncfail", 0).commit();
		}
		mLog.i(TAG,"getlatest() 88 done");
	
	}
	
	

	private String getBasePage(String baseurl) {
		HTTPClient sp = new HTTPClient(SyncService.this);
		String httpStatus = sp.safeHttpGet(TAG + " getlatest() 49", new HttpGet(baseurl) );
		String httpPage = sp.getHttpPage();
		if( httpStatus.contains("200") && httpPage.length() > 0 ){
			mLog.i(TAG,"Download Successful");
		}else{
			mLog.e(TAG,"Download Failed, Trying one more time.");
			httpStatus = sp.safeHttpGet(TAG + " getlatest() 49", new HttpGet(baseurl) );
			httpPage = sp.getHttpPage();
			if( httpStatus.contains("200") && httpPage.length() > 0 ){
				mLog.i(TAG,"Download Successful");
			}else{
				if( httpStatus == null ){ httpStatus = "NULL"; }
				mLog.e(TAG,"Download Failed. httpStatus("+httpStatus+")" );
				return "";
			}
		}
		return httpPage;		
	}
	


	
}
