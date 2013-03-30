package com.havenskys.seashepherdfree;

import java.io.IOException;
import java.io.InputStream;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;
import android.widget.SimpleCursorAdapter;

public class Custom {

	// CUSTOM
	public static String APP = "SeaShepherdFree";
	public static String WHO = "SSCS";
	public static boolean PUBLISH = true;
	public static boolean FREEVERSION = true;
	public static String EMAIL = "info@seashepherd.org";
	public static int LITTLEICON = R.drawable.seashepherd;
	public static int TOPICON = R.drawable.contactseashepherd;
	public static String MAINURI = "com.havenskys.seashepherdfree";
	public static String BASEURL = "http://www.seashepherd.org/news-and-media/sea-shepherd-news/feed/rss.html";
	
	
	public static String TAG = "Custom";
	public static final String DATABASE_NAME = "blogitems.db";
    public static final String DATABASE_TABLE_NAME = "blogitems";
    public String SQL = "";
	public static int NOTIFY_ID = 1;
	public static int NOTIFY_ID_ARTICLE = 2;
	public int RESTARTMIN = 30;
	
    
	// CUSTOM
	//title, link, id, updated, summary, content
    public static final String ID           = "_id";
    public static final String TITLE        = "title";
    public static final String LINK      	= "link";
    public static final String DATE         = "date";
    //public static final String SUMMARY      = "summary";
    public static final String CONTENT      = "content";
    //public static final String DESCRIPTION  = "description";
    //public static final String AUTHOR       = "author";
    //public static final String SUBTITLE     = "subtitle";
    //public static final String CATEGORY     = "category";
    //public static final String MEDIA        = "media";
    //public static final String MEDIATYPE    = "mediatype";
    //public static final String MEDIADURATION = "mediaduration";
    //public static final String MEDIASIZE    = "mediasize";
    public static final String LAST_UPDATED = "lastupdated";
    public static final String CREATED      = "created";
    public static final String STATUS       = "status";
    public static final String DEFAULT_SORT_ORDER = CREATED + " DESC";
    
    
	// CUSTOM
	public String getContentSQL() {
		
		SQL = "CREATE TABLE " + DATABASE_TABLE_NAME + "(" +
        ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        TITLE + " TEXT," +
        LINK + " TEXT UNIQUE," +
        DATE + " TEXT," +
        //SUMMARY + " TEXT," +
        CONTENT + " TEXT," +
        //DESCRIPTION + " TEXT," +
        //AUTHOR + " TEXT," +
        //SUBTITLE + " TEXT," +
        //CATEGORY + " TEXT," +
        //MEDIA + " TEXT," +
        //MEDIATYPE + " TEXT," +
        //MEDIADURATION + " TEXT," +
        //MEDIASIZE + " TEXT," +
        STATUS + " INTEGER DEFAULT 1," +
        CREATED + " INTEGER DEFAULT 0," +
        LAST_UPDATED + " INTEGER DEFAULT 0);";
		
		return SQL;

	}
	
	public void loadlist(listView view) {
		i(TAG,"loadlist() ++++++++++++++++++++++++++++++++++");
		
		Cursor lCursor = null;
		
		// CUSTOM
		//String[] columns = new String[] {"_id", "title", "datetime(date,'localtime') as date", "summary"};
		String[] columns = new String[] {"_id", "title", "datetime(date,'localtime') as date" };
        String[] from = new String[]{"title", "date" };
        int[] to = new int[]{R.id.listrow_title, R.id.listrow_date };
        
		lCursor = SqliteWrapper.query(view, view.getContentResolver(), DataProvider.CONTENT_URI, 
        		columns,
        		"status > 0", 
        		null, 
        		"datetime(date) desc");
		
		view.startManagingCursor(lCursor);
        SimpleCursorAdapter entries = new SimpleCursorAdapter(view, R.layout.listrow, lCursor, from, to);
        view.setListAdapter(entries);
        view.getListView().setTextFilterEnabled(true);	
		
	}
	
	
	public void parseEntries(String who, String httpPage) {
		
		int successCount = 0;
		
		int syncLoad = mSharedPreferences.contains("syncload") ? mSharedPreferences.getInt("syncload",4) : 4;
		
		// CUSTOM
		String entryTag = "item";
		String[] parseColumns = new String[]{ Custom.TITLE, Custom.LINK, Custom.DATE, Custom.CONTENT };
		String[] parseTags = new String("title,link,pubDate,description").split(",");
		int[] parseFixDates = new int[]{2};
		
		ContentValues parsedValues = null;
		String[] parseTagParts = new String[2];
		int parseI = 0;
		int parseLen = parseTags.length;
		String[] input = httpPage.replaceAll("<"+entryTag+">", "\n<"+entryTag+">\n").replaceAll("</"+entryTag+">", "</"+entryTag+">\n").replaceAll("/>", "/>\n").replaceAll("\r", "\n").split("\n");
		
		for( int i = 0; i < input.length; i++){
			
			if( FREEVERSION && successCount >= 3 ){ return; }
			
			if( input[i].length() == 0 ){ continue; }
			w(TAG,"Line("+i+") " + input[i] );
			if( input[i].contains("<"+entryTag+">") ){
				parsedValues = new ContentValues();
				
				
				for(i++; i < input.length; i++){
					if( input[i].length() == 0 ){ continue; }
					w(TAG,"Item Line("+i+") " + input[i] );
					if( input[i].contains("<"+entryTag+">") ){ /*Go Back in the itteration, we've somehow missed the </item> tag */ i--;	break; }
					if( input[i].contains("</"+entryTag+">") ){ 	break; }
					
					for(parseI = 0; parseI < parseLen; parseI++){
						parseTagParts = parseTags[parseI].split(" ", 2);
						if( input[i].contains("<"+parseTagParts[0]+">") || input[i].contains("<"+parseTagParts[0]+" ") ){
							if( parseTagParts.length == 2 ){
								parsedValues.put(parseColumns[parseI], getTagValue(who + " parseEntries() 153 ", input[i], parseTagParts[0], parseTagParts[1]) );
							}else{
								if( !input[i].contains("</"+parseTagParts[0]+">") ){
									for(int c = i; c < input.length; c++){ 
										input[i] += input[c];
										if( input[c].contains("</"+parseTagParts[0]+">") ){ break; }
									}
								}
								parsedValues.put(parseColumns[parseI], getTagContent(who + " parseEntries() 161 ", input[i], parseTagParts[0]) );
							}
						}
					}
				}
				
				
				
				int parseVerifyCount = 0;
				String logline = "";
				for(parseI = 0; parseI < parseLen; parseI++){
					String s = parsedValues.getAsString(parseColumns[parseI]);
					if( s != null ){
						if( s.length() > 200 ){
							s = s.replaceAll("\n.*", "<<< SHORTENED from " + s.length() + ">>>");
						}
						logline += parseI + "(" + parseColumns[parseI] + ":"+ s +") ";
						if( s.length() > 0 ){
							parseVerifyCount ++;
						}
					}else{
						e(TAG,"Parse Failure? "+parseI+"("+parseColumns[parseI]+")");
					}
				}
				i(TAG,"Parsed verified("+parseVerifyCount+") " + logline);
				
				
				// CUSTOM
				if( parseVerifyCount == parseLen ){
					successCount++;
					long rowid = getId(DataProvider.CONTENT_URI.toString(), Custom.LINK + " = \""+parsedValues.getAsString(Custom.LINK)+"\"");
					if( rowid > 0 ){
						//update if required
						w(TAG,"Found blogitem("+rowid+") link(" + parsedValues.getAsString(Custom.LINK) + ")");
						// This will block until load is low or time limit exceeded
				        loadLimit(TAG + " getlatest() 194", syncLoad+2 , 10 * 1000, 30 * 1000);
					}else{
						// This will block until load is low or time limit exceeded
				        loadLimit(TAG + " getlatest() 194", syncLoad , 10 * 1000, 30 * 1000);
						
						// 0   1   2   3      4       5
						//day, D2 mon YEA4 HH:MM:SS +0000
						//YYYY-MM-DD HH:MM:SS
						// Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec
						//updated = fixDate(updated);
						for( int f = 0; f < parseFixDates.length; f++ ){
							String date = parsedValues.getAsString(parseColumns[parseFixDates[f]]);
							parsedValues.put(parseColumns[parseFixDates[f]], fixDate(date) );
							i(TAG,"Fixing Date("+parseColumns[parseFixDates[f]]+") PreviousValue("+date+") NewValue("+parsedValues.getAsString(parseColumns[parseFixDates[f]])+")");
						}
						
						parsedValues.put(Custom.LAST_UPDATED, System.currentTimeMillis());
						parsedValues.put(Custom.STATUS, 1);
						parsedValues.put(Custom.CREATED, System.currentTimeMillis());
						
						try {
							SqliteWrapper.insert(mContext, mResolver, DataProvider.CONTENT_URI, parsedValues);
						} catch (SQLiteException e){
							e(TAG,"SQLiteException " + e.getLocalizedMessage());
						}

						rowid = getId(DataProvider.CONTENT_URI.toString(), Custom.LINK + " = \""+parsedValues.getAsString(Custom.LINK)+"\"");
						if( rowid > 0 ){
							w(TAG,"Created blogitem("+rowid+") link("+parsedValues.getAsString(Custom.LINK)+")");
							if( successCount == 1 ){
								// CUSTOM
								if( FREEVERSION ){
									setEntryNotification(TAG + " getlatest()", rowid, Custom.LITTLEICON, Custom.APP + " (Upgrade $2)", parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim(), Custom.APP + ": " + parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim());
								}else{
									setEntryNotification(TAG + " getlatest()", rowid, Custom.LITTLEICON, Custom.APP, parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim(), Custom.APP + ": " + parsedValues.getAsString(Custom.TITLE).replaceAll("<.*?>", " ").trim());
								}
							}
						}
					}
				}
			}
		}
	}

	

	private String getTagContent(String who, String lines, String tag) {
		String value = "";
		if( lines.contains("<"+tag+" ") ){
			long time = SystemClock.currentThreadTimeMillis();
			value = lines.replaceFirst(".*<"+tag+" ", "").replaceFirst(">", "SPLIT-"+time+">").replaceFirst(".*SPLIT-"+time+">", "").replaceFirst("</"+tag+">.*", "");
		}else{
			value = lines.replaceFirst(".*<"+tag+">", "").replaceFirst("</"+tag+">.*", "");
		}
		if( value.contains("CDATA[") ){
			value = value.replaceFirst("\\]\\].*", "").replaceFirst(".*CDATA\\[", "");
		}
		return value.trim();
	}
	
	private String getTagValue(String who, String line, String tag, String key) {
		String value = "";
		value = line.replaceFirst(".*<"+tag+" .*"+key+"=\"", "").replaceFirst("\".*", "").trim();
		return value;
	}



	private Context mContext;
    private ContentResolver mResolver;
	public Custom(Context ctx){
		w(TAG,"Custom() ++++++++++++++++++++++++++++++++++");
		mContext = ctx;
		mResolver = ctx.getContentResolver();
	}
	
    private NotificationManager mNM;
    public void setNotificationManager(NotificationManager notifmgr){
    	mNM = notifmgr;
    }
    
    private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	public void setSharedPreferences(SharedPreferences sharedPreferences, Editor preferencesEditor) {
		mSharedPreferences = sharedPreferences;
		mPreferencesEditor = preferencesEditor;
	}
	
	
	private String[] mLogLines = null;
	private int mLogLooper, mLogLen;
	public void i(String who, String data){
		if( PUBLISH ){ return; }
		mLogLines = data.split("\n");
		mLogLen = mLogLines.length;
		for (mLogLooper = 0; mLogLooper < mLogLen; mLogLooper++){ Log.i(APP +" "+ who,mLogLines[mLogLooper]); }
	}
	
	public void w(String who, String data){
		if( PUBLISH ){ return; }
		mLogLines = data.split("\n");
		mLogLen = mLogLines.length;
		for (mLogLooper = 0; mLogLooper < mLogLen; mLogLooper++){ Log.w(APP +" "+ who,mLogLines[mLogLooper]); }
	}
	
	public void e(String who, String data){
		mLogLines = data.split("\n");
		mLogLen = mLogLines.length;
		for (mLogLooper = 0; mLogLooper < mLogLen; mLogLooper++){ Log.e(APP +" "+ who,mLogLines[mLogLooper]); }
	}

	
	private String fixDate(String updated) {
		
		String[] dateparts = updated.split(" ");
		if( dateparts.length > 4 ){
			String[] month = new String("xxx Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec xxx").split(" ");
			int mon = 0;
			for(;mon < month.length; mon++){
				if( month[mon].equalsIgnoreCase(dateparts[2]) ){ break; } 
			}
			if( mon == 13 ){
				e(TAG,"Unable to determine month in fixDate("+updated+")");
				return updated;
			}
			if( mon < 10 ){
				updated = dateparts[3] + "-0" + mon + "-" + dateparts[1] + "T" + dateparts[4];
			}else{
				updated = dateparts[3] + "-" + mon + "-" + dateparts[1] + "T" + dateparts[4];
			}
			w(TAG,"Updated date to SQLite Format("+updated+")");
		}
		
		return updated;
	}

	public void setEntryNotification(String who, long rowid, int icon, String title, String details, String topscroll){
		
		int syncvib = mSharedPreferences.contains("syncvib") ? mSharedPreferences.getInt("syncvib",1) : 1;
		
		Notification notif = new Notification(icon, topscroll, System.currentTimeMillis()); // This text scrolls across the top.
		Intent intentJump2 = new Intent(mContext, com.havenskys.seashepherdfree.Start.class);
		intentJump2.putExtra("id", rowid);
		intentJump2.putExtra("tab", 1);
        //PendingIntent pi2 = PendingIntent.getActivity(this, 0, intentJump2, Intent.FLAG_ACTIVITY_NEW_TASK );
        PendingIntent pi2 = PendingIntent.getActivity(mContext, 0, intentJump2, Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_MULTIPLE_TASK );
        
        //if( syncvib != 3 ){ // NOT OFF
//        	notif.defaults = Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE;
        //}else{
        	notif.defaults = Notification.DEFAULT_LIGHTS;
        //}
        
		notif.setLatestEventInfo(mContext, title, details, pi2); // This Text appears after the slide is open
		
		switch (syncvib) {
		case 1: // ++_+
			notif.vibrate = new long[] { 100, 200, 100, 200, 500, 100 };
			break;
		case 3: // None _
			break;
		case 2: // ++
			notif.vibrate = new long[] { 100, 200, 100, 200 };
			break;
		case 4: // +_++
			notif.vibrate = new long[] { 100, 200, 500, 200, 100, 200 };
			break;
		}
        mNM.notify(Custom.NOTIFY_ID_ARTICLE, notif);
	}
	
	public void setServiceNotification(String who, int icon, String title, String details, String topscroll){
		//NotificationManager notifMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		Notification notif = new Notification(icon, topscroll, System.currentTimeMillis());
		Intent intentJump = new Intent(mContext, com.havenskys.seashepherdfree.Stop.class);
		intentJump.putExtra("stoprequest", true);
        PendingIntent pi = PendingIntent.getActivity(mContext, 0, intentJump, Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NO_HISTORY);
        notif.setLatestEventInfo(mContext, title,details, pi);
        //notif.defaults = Notification.DEFAULT_LIGHTS;
        //notif.vibrate = new long[] { 100, 100, 100, 200, 100, 300 };
        mNM.notify(Custom.NOTIFY_ID, notif);
	}
	
	
	// This will block until load is low or time limit exceeded
    // loadLimit(TAG + " getlatest() 107", 1 , 5 * 1000, 3 * 60 * 1000);
	public void loadLimit(String who, int loadMin, int waitms, int waittimemax) {
		
		double load = 0;
		double lastload = 0;
        int waitloopmax = waittimemax / waitms;
        for(int lc = 1; lc <= waitloopmax; lc++){
        	load = getload();
        	if( load > loadMin ){
        		
        		//if( lc == 2 ){ // second loop, notify user app has paused.
        			//setServiceNotification(TAG + " loadLimit() 340", android.R.drawable.ic_media_pause, Custom.APP + " (Press to Stop)", "Waiting for device CPU load to decrease.", Custom.APP + " synchronizing service is paused, waiting for CPU load to decrease.");
        		//}
        		
        		SystemClock.sleep(waitms);
        	}else{
        		break;
        	}
        	if( lc == waitloopmax ){
        		if( load > lastload ){
        			// Load is going up, let's hold off till this isn't true.
        			// First available chance though, I'm on it.
        			lc--;
        		}else{
        			w(TAG,"Waited for maximum limit("+waittimemax+"ms), running anyway. for " + who);
        		}
        	}
        	lastload = load;
        }
        
        //setServiceNotification(TAG + " loadLimit() 350", android.R.drawable.stat_notify_sync, Custom.APP + " (Press to Stop)", "Synchronizing updates.", Custom.APP + " synchronizing updates.");
	}
	
	
	private Process mLoadProcess;
	private InputStream mLoadStream;
	private byte[] mLoadBytes;
	private String[] mLoadParts;
	private long mLoadStart;
	private int mLoadReadSize;
	private double mLoadDouble;
	public double getload(){
		mLoadStart = System.currentTimeMillis();
		mLoadDouble = 1.1; // if something goes wrong, best to error on the side of shy
		try {
			mLoadProcess = Runtime.getRuntime().exec("cat /proc/loadavg");
			mLoadProcess.waitFor();
			mLoadStream = mLoadProcess.getInputStream();
			mLoadBytes = new byte[100];
			mLoadReadSize = mLoadStream.read(mLoadBytes, 0, 99);
			mLoadParts = new String(mLoadBytes).trim().replaceAll("\\s+", " ").split(" ");
			mLoadDouble = new Double(mLoadParts[0]);
			w(TAG,"Load size("+mLoadReadSize+") load("+mLoadDouble+") ms("+(System.currentTimeMillis() - mLoadStart)+") loadavg("+new String(mLoadBytes).trim()+")");
			
			
			/*
			w(TAG,"Getting MEMINFO");
			Process top = Runtime.getRuntime().exec("cat /proc/meminfo");
			top.waitFor();
			InputStream topstream = top.getInputStream();
			mLoadBytes = new byte[1024];
			mLoadReadSize = topstream.read(mLoadBytes, 0, 1023);
			w(TAG,"MEMINFO " + new String(mLoadBytes).trim() );
			//*/
			
			/*
			w(TAG,"Getting PROC");
			Process top = Runtime.getRuntime().exec("ls /proc");
			top.waitFor();
			InputStream topstream = top.getInputStream();
			mLoadBytes = new byte[1024];
			mLoadReadSize = topstream.read(mLoadBytes, 0, 1023);
			String[] proclist = new String(mLoadBytes).trim().split("\n");
			for(int i = 0; i < proclist.length; i++){
				w(TAG,"PROC: " +  proclist[i].trim() );
				if( proclist[i].trim().contains("self") ){
					for(i++; i < proclist.length; i++){
						Process file = Runtime.getRuntime().exec("ls /proc/"+proclist[i].trim());
						file.waitFor();
						InputStream filestream = file.getInputStream();
						mLoadBytes = new byte[1024];
						mLoadReadSize = filestream.read(mLoadBytes, 0, 1023);
						String[] filelist = new String(mLoadBytes).trim().split("\n");
						for(int c = 0; c < filelist.length; i++){
							w(TAG,"FILE /proc/"+proclist[i].trim() + ": " +  filelist[c].trim() ); 
						}
					}
					
					break;
				}
			}
			//*/
			
			mLoadBytes = null;

			
		} catch (InterruptedException e) {
			e(TAG,"Load InterruptedException");
			e.printStackTrace();
		} catch (IOException e) {
			e(TAG,"Load IOException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mLoadDouble;
	}
	
	
	public long getId(String path, String where){
		Object[][] data = getAndroidData(path,"_id",where,null);
		if( data == null ){
			return 0;
		}else{
			return new Long(data[0][0].toString());
		}
	}
	
	public Object[][] getAndroidData(String path, String columns, String where, String orderby){
		Object[][] reply = null;

		//mLog.w(TAG,"getAndroidData("+path+") columns("+columns+") where("+where+")");
		
		if( orderby == null ){
			orderby = "created desc";
		}
		
        Cursor dataCursor = SqliteWrapper.query(mContext, mResolver, Uri.parse(path) 
        		,columns.split(",")
        		,where
        		,null
        		,orderby //"date desc"
        		);
        
        
        if( dataCursor != null ){
        	if( dataCursor.moveToFirst() ){
        		int len = dataCursor.getCount();
        		int clen = dataCursor.getColumnCount();
        		reply = new Object[len][clen];
        		for(int r = 0; r < len ;r++){
        			dataCursor.moveToPosition(r);
	        		for(int c = 0; c < clen ;c++){
	        			reply[r][c] = dataCursor.getString(c);
	        		}
        		}
        	}else{
        		//mLog.w(TAG,"getAndroidData empty");
        	}

            dataCursor.close();
        }else{
        	//mLog.w(TAG,"getAndroidData null");
        }
        
        
		return reply;
	}

	

	

	
	

	
	
}
