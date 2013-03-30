package com.havenskys.seashepherdfree;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class IntentReceiver extends BroadcastReceiver {

	private final static String TAG = "IntentReceiver";
	
	private Custom mLog;

	private static Object mStartingServiceSync = new Object();
	private static WakeLock mWakeService;
	private static Context mContext;

	public void onReceive(Context context, Intent intent) {
		mLog = new Custom(context);
		mLog.i(TAG,"onReceive(Action Received:"+intent.getAction()+") ++++++++++++++++++++++++++++++++++++++++++++++++");
		mContext = context;
		
		intent.setClass(mContext, SyncService.class);
		intent.putExtra("result", getResultCode());
		
		beginHostingService(context,intent);
	}

	public static void beginHostingService(Context context, Intent intent) {
		Custom mLog = new Custom(context);
		//android.intent.action.BOOT_COMPLETED
		mLog.i(TAG,"beginHostingService() ++++++++++++++++++++++++++++++++++++++++++++++++");
		mContext = context;
		synchronized (mStartingServiceSync){
			mLog.i(TAG,"beginHostingService() synchronized() ++++++++++++++++++++++++++++++++++++++++++++++++");
			if(mWakeService == null){
				mLog.i(TAG,"beginHostingService() PowerManager ++++++++++++++++++++++++++++++++++++++++++++++++");
				PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
				mWakeService = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StartingAlertService");
				mWakeService.setReferenceCounted(false);
			}
			mLog.i(TAG,"beginHostingService() acquire() ++++++++++++++++++++++++++++++++++++++++++++++++");
			mWakeService.acquire();

			mLog.i(TAG,"beginHostingService() startService() ++++++++++++++++++++++++++++++++++++++++++++++++");
			context.startService(intent);
		}
	}
	
	public static void finishHostingService(SyncService service, int serviceId) {
		
		Custom mLog = new Custom(service.getApplicationContext());
		mLog.i(TAG,"finishHostingService() ++++++++++++++++++++++++++++++++++++++++++++++++");

		synchronized (mStartingServiceSync){
			if(mStartingServiceSync != null){
				if( service.stopSelfResult(serviceId) ){
					mWakeService.release();
				}
			}
		}
		
	}


}
