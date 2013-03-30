package com.havenskys.seashepherdfree;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;

public class HTTPClient {

	final static String TAG = "HTTP";

	private Context mContext;
	private ContentResolver mResolver;
	
	
	private DefaultHttpClient mHttpClient;
	private HttpResponse mHttpResponse;
	private HttpEntity mHttpEntity;
    private String mHttpPage;
    private String mSessionId, mDestination, mPostpath;
    private List<Cookie> mHttpCookie;
    private String mUrl, mContentType, mContentEncoding;
    
    private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	
	private Custom mLog;
    
	
	
	public HTTPClient(Context ctx){
		mContext = ctx;
		mResolver = ctx.getContentResolver();
		mHttpClient = new DefaultHttpClient();
		mLog = new Custom(ctx);
	}
	
	public void setSharedPreferences(SharedPreferences sharedPreferences){
		mSharedPreferences = sharedPreferences;
		mPreferencesEditor = sharedPreferences.edit();
		//mPreferencesEditor.putString("destination", destination); mPreferencesEditor.commit();
		//String destination = mSharedPreferences.contains("destination") ? mSharedPreferences.getString("destination", "") : "";
	}
	
	
    /** Cannot access the calendar */
    public static final int NO_ACCESS = 0;
    /** Can only see free/busy information about the calendar */
    public static final int FREEBUSY_ACCESS = 100;
    /** Can read all event details */
    public static final int READ_ACCESS = 200;
    public static final int RESPOND_ACCESS = 300;
    public static final int OVERRIDE_ACCESS = 400;
    /** Full access to modify the calendar, but not the access control settings */
    public static final int CONTRIBUTOR_ACCESS = 500;
    public static final int EDITOR_ACCESS = 600;
    /** Full access to the calendar */
    public static final int OWNER_ACCESS = 700;
    public static final int ROOT_ACCESS = 800;

    // attendee relationship
    public static final int RELATIONSHIP_NONE = 0;
    public static final int RELATIONSHIP_ATTENDEE = 1;
    public static final int RELATIONSHIP_ORGANIZER = 2;
    public static final int RELATIONSHIP_PERFORMER = 3;
    public static final int RELATIONSHIP_SPEAKER = 4;

    // attendee type
    public static final int TYPE_NONE = 0;
    public static final int TYPE_REQUIRED = 1;
    public static final int TYPE_OPTIONAL = 2;

    // attendee status
    public static final int ATTENDEE_STATUS_NONE = 0;
    public static final int ATTENDEE_STATUS_ACCEPTED = 1;
    public static final int ATTENDEE_STATUS_DECLINED = 2;
    public static final int ATTENDEE_STATUS_INVITED = 3;
    public static final int ATTENDEE_STATUS_TENTATIVE = 4;

    
	
    public String safeHttpPost(String who, HttpPost httpPost, List<NameValuePair> nvps) {
    	mLog.w(TAG,"safeHttpPost() 972 getURI("+httpPost.getURI()+") for " + who);
        //HttpParams params = httpclient.getParams();
        //params.setParameter("Cookies", "logondata=acc=1&lgn=DDDD0/uuuu; expires="+expiredate.toString());
        //httpclient.set
    	
    	String responseCode = ""; mHttpPage = "";
        try {
        	mLog.w(TAG,"safeHttpPost() 979 UrlEncodeFormValues for " + who);
			httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
			mLog.w(TAG,"safeHttpPost() 981 httpclient.execute() for " + who);
	        mHttpResponse = mHttpClient.execute(httpPost);

	        if( mHttpResponse != null ){
		        mLog.w(TAG,"safeHttpPost() " + mHttpResponse.getStatusLine());
		        
		        mLog.w(TAG,"safeHttpPost() response.getEntity()");
		        mHttpEntity = mHttpResponse.getEntity();
	
		        if (mHttpEntity != null) {
		        	
		        	Header[] list = mHttpResponse.getAllHeaders();
		        	for( int i = 0; i < list.length; i++ ){
		        		mLog.w(TAG,"Header "+i+" name("+list[i].getName()+") value("+list[i].getValue()+") ");
		        	}
		        	
			        //byte[] bytes = ;
			        mHttpPage = new String(EntityUtils.toByteArray(mHttpEntity));
			        mLog.w(TAG,"safeHttpPost() 993 Downloaded " + mHttpPage.length() + " bytes. for " + who);
			        
			        mHttpCookie = mHttpClient.getCookieStore().getCookies();
			        //
			        // Print Cookies
			        if ( !mHttpCookie.isEmpty() ) { for (int i = 0; i < mHttpCookie.size(); i++) { mLog.w(TAG,"safeHttpPost() Cookie: " + mHttpCookie.get(i).toString()); } }
	
			        //
			        // Print Headers
			        Header[] h = mHttpResponse.getAllHeaders(); for( int i = 0; i < h.length; i++){ mLog.w(TAG,"safeHttpPost() Header: " + h[i].getName() + ": " + h[i].getValue()); }
			        
			        // Clear memory
			        mHttpEntity.consumeContent();
		        }
		        
		        // Get response code string
		        responseCode = mHttpResponse.getStatusLine().toString();
	        }
		} catch (UnsupportedEncodingException e) {
			mLog.w(TAG,"safeHttpPost() 1012 UnsupportedEncodingException for " + who);
			e.printStackTrace();
			responseCode = "HTTPERRORTHROWN " + e.getLocalizedMessage();
		} catch (ClientProtocolException e) {
			mLog.w(TAG,"safeHttpPost() 1015 ClientProtocolException for " + who);
			e.printStackTrace();
			responseCode = "HTTPERRORTHROWN " + e.getLocalizedMessage();
		} catch (IOException e) {
			mLog.w(TAG,"safeHttpPost() 1018 IOException for " + who);
			e.printStackTrace();
			responseCode = "HTTPERRORTHROWN " + e.getLocalizedMessage();
		} catch (IllegalStateException e) {
			mLog.w(TAG,"safeHttpPost() 1021 IllegalState Exception for " + who);
			e.printStackTrace();
		}

		return responseCode;
		
    }
    


    //private byte[] mLoadBytes = new byte[1024 * 1024]; // 1 meg limit
	public String safeHttpGet(String who, HttpGet httpget) {
		
		mLog.w(TAG,"safeHttpGet() 1033 getURI("+httpget.getURI()+") for " + who);
		if( httpget.getURI().toString() == "" ){
			mLog.e(TAG,"safeHttpGet 1035 Blocked empty request for " + who);
			return "";
		}
		
		String responseCode = ""; mHttpPage = "";
		
		try {

			mLog.w(TAG,"safeHttpGet() 1044 httpclient.execute() for " + who);
			mHttpClient.setRedirectHandler(new RedirectHandler() {

				public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
					mLog.w(TAG,"RedirectHandler() getLocationURI ++++++++++++++++++++++");
					
					/*/
					Header[] list = response.getAllHeaders();
		        	for( int i = 0; i < list.length; i++ ){ mLog.w(TAG,"Header "+i+" name("+list[i].getName()+") value("+list[i].getValue()+") "); }
		        	String httpPage = "";
					try {
						httpPage = new String(EntityUtils.toByteArray(response.getEntity()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        	String[] lines = httpPage.split("\n");
		        	for( int i = 0; i < lines.length; i++ ){
		        		mLog.w(TAG,"Page "+i+" name("+lines[i]+") ");
		        	}
		        	//*/
					
		        	URI u;
		        	
		        	if( mUrl.length() > 0 ){
		        		try {
							u = new URI(mUrl);
							return u;
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		        	}
					
					return null;
				}

				public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
					mLog.w(TAG,"RedirectHandler() isRedirectRequested ++++++++++++++++++++++");

					
					Header[] list = response.getAllHeaders();
		        	for( int i = 0; i < list.length; i++ ){ mLog.w(TAG,"Header "+i+" name("+list[i].getName()+") value("+list[i].getValue()+") "); }
		        	
		        	if( response.containsHeader("Location") ){
		        		String prev = mUrl;
						Header loc = response.getFirstHeader("Location");
						
						String n = loc.getValue();
						if( n.contains("http") ){
							mUrl = n;
						}else{
							String[] urlp = mUrl.split("/");
							if( urlp.length > 3 ){ mUrl = urlp[0] + "//" + urlp[2] + n; }
							mLog.w(TAG,"FORCED LOCAL URL TO GLOBAL FORMAT " + mUrl + " from("+n+")");
						}
						if (prev == mUrl){
							mLog.i(TAG,"Masking redirect because lastUrl("+mUrl+") didn't change.");
							return false;
						}
						Header contenttype = response.getFirstHeader("Content-Type");
						mContentType = contenttype.getValue().replaceAll(";.*", "");
						if( contenttype.getValue().contains("charset=") ){
							mContentEncoding = contenttype.getValue().replaceAll(".*charset=", "").replaceAll(";.*", "");
						}
						mLog.i(TAG,"Setting lastUrl("+mUrl+") and returning true to redirection query.");
						return true;
					}
		        	
		        	/*/
		        	String httpPage = "";
		        	try {
						httpPage = new String(EntityUtils.toByteArray(response.getEntity()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String[] lines = httpPage.split("\n");
		        	for( int i = 0; i < lines.length; i++ ){
		        		mLog.w(TAG,"Page "+i+" name("+lines[i]+") ");
		        	}
		        	//*/
					return false;
				}
				
			});
			
			mUrl = httpget.getURI().toURL().toString();
			mLog.i(TAG,"Setting lastUrl("+mUrl+") because it's what is requested.");
			mHttpResponse = mHttpClient.execute(httpget);
			
			
			if( mHttpResponse != null ){
		        mLog.w(TAG,"safeHttpGet() 1048 " + mHttpResponse.getStatusLine() + " for " + who);
		        
		        mLog.w(TAG,"safeHttpGet() 1050 response.getEntity() for " + who);
		        mHttpEntity = mHttpResponse.getEntity();
	
		        
		        //mHttpClient.
	
		        if (mHttpEntity != null) {
			        //byte[] bytes = ;
		        	
		        	//Header[] list = mHttpResponse.getAllHeaders();
		        	//for( int i = 0; i < list.length; i++ ){ mLog.w(TAG,"Header "+i+" name("+list[i].getName()+") value("+list[i].getValue()+") "); }
		        	
		        	
		        	mLog.w(TAG,"safeHttpGet() 295 read page for " + who);
			        mHttpPage = new String(EntityUtils.toByteArray(mHttpEntity));
			        /*
			        InputStream data = mHttpEntity.getContent();
			        
			        String httpPage = new String();
			        mLog.w(TAG,"safeHttpGet() 304 read manually for " + who);
					int readSize = data.read(mLoadBytes, 0, (1024 * 1024) - 1);
					mLog.w(TAG,"safeHttpGet() 305 read manually returned size("+readSize+") for " + who);
					mLog.w(TAG,"safeHttpGet() 305 read manually captured size("+mLoadBytes.length+") for " + who);
			        //mHttpPage = generateString("safeHttpGet() 306", data);
					byte[] newbuff = new byte[readSize];
					System.arraycopy(mLoadBytes, 0, newbuff, 0, readSize);
					//mLog.w(TAG,"safeHttpGet() 309 read manually converting to ByteBuffer for " + who);
					//ByteBuffer bb = ByteBuffer.allocate(newbuff.length);
					//bb.put(newbuff);
					mLog.w(TAG,"safeHttpGet() 309 read manually converting to string for " + who);
					//httpPage = new String(bb.array());
					httpPage = new String(newbuff);
					mLog.w(TAG,"safeHttpGet() 309 read manually converting to string size("+httpPage.length()+") for " + who);
			        mLog.w(TAG,"safeHttpGet() 308 read content-type for " + who);
			        mHttpPage = httpPage;
			        //*/
			        
					Header contenttype = mHttpResponse.getFirstHeader("Content-Type");
					if( contenttype != null ){
						mContentType = contenttype.getValue();
						if( mContentType != null ){
							if( mContentType.contains("charset=") ){ 
								mContentEncoding = mContentType.replaceAll(".*charset=", "").replaceAll(";.*", "");
							}
							if( mContentType.contains(";") ){
								mContentType = mContentType.replaceAll(";.*", "");
							}
						}
					}
					
			        mLog.w(TAG,"safeHttpGet() 321 Downloaded " + mHttpPage.length() + " bytes. for " + who);

			        mHttpCookie = mHttpClient.getCookieStore().getCookies();
			        //
			        // Print Cookies
			        //if ( !mHttpCookie.isEmpty() ) { for (int i = 0; i < mHttpCookie.size(); i++) { mLog.w(TAG,"safeHttpGet() Cookie: " + mHttpCookie.get(i).toString()); } }
			        
			        //
			        // Print Headers
		        	//Header[] h = mHttpResponse.getAllHeaders(); for( int i = 0; i < h.length; i++){ mLog.w(TAG,"safeHttpGet() Header: " + h[i].getName() + ": " + h[i].getValue()); }
			        
			        mHttpEntity.consumeContent();
				}
			}
	        responseCode = mHttpResponse.getStatusLine().toString();
			
		} catch (ClientProtocolException e) {
			mLog.w(TAG,"safeHttpGet() 1121 ClientProtocolException for " + who);
			mLog.w(TAG,"safeHttpGet() 1122 IO Exception Message " + e.getLocalizedMessage());
			e.printStackTrace();
			responseCode = "HTTPERRORTHROWN " + e.getLocalizedMessage();
		} catch (NullPointerException e) {
			mLog.w(TAG,"safeHttpGet() 1126 NullPointer Exception for " + who);
			mLog.w(TAG,"safeHttpGet() 1127 IO Exception Message " + e.getLocalizedMessage());
			e.printStackTrace();
		} catch (IOException e) {
			mLog.w(TAG,"safeHttpGet() 1130 IO Exception for " + who);
			//if( e.getLocalizedMessage().contains("Host is unresolved") ){ SystemClock.sleep(1880); }
			mLog.w(TAG,"safeHttpGet() 1132 IO Exception Message " + e.getLocalizedMessage());
			StackTraceElement[] err = e.getStackTrace();
			for(int i = 0; i < err.length; i++){
				mLog.w(TAG,"safeHttpGet() 1135 IO Exception Message " + i + " class(" + err[i].getClassName() + ") file(" + err[i].getFileName() + ") line(" + err[i].getLineNumber() + ") method(" + err[i].getMethodName() + ")");
			}
			responseCode = "HTTPERRORTHROWN " + e.getLocalizedMessage();
		} catch (IllegalStateException e) {
			mLog.w(TAG,"safeHttpGet() 1139 IllegalState Exception for " + who);
			mLog.w(TAG,"safeHttpGet() 1140 IO Exception Message " + e.getLocalizedMessage());
			e.printStackTrace();
			//if( responseCode == "" ){
				//responseCode = "440"; //440 simulates a timeout condition and recreates the client.
			//}
		}
		
		mLog.i(TAG,"safeHttpGet() 346 Returning for " + who);
		return responseCode;
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
        	}

            dataCursor.close();
        }
        
        
		return reply;
	}
	

	public String getHttpPage() {
		if( mHttpPage == null ){ return ""; }
		return new String(mHttpPage);
	}
	
	public String getUrl() {
		if( mUrl == null ){ return ""; }
		return mUrl;
	}
	
	public String getContentType() {
		if( mContentType == null ){ return ""; }
		return mContentType;
	}

	public String getContentEncoding() {
		if( mContentEncoding == null ){ return ""; }
		return mContentEncoding;
	}
	
	/*
	 *         
	 *  List <NameValuePair> values = new ArrayList <NameValuePair>();
        values.add(new BasicNameValuePair("destination",mDestination));
		updateAddContactRecord(String who, String remoteid, List<NameValuePair> values)
	 */
	

	
	
	

	public void androidDataPrint(String basePath){
		androidDataPrint(basePath,"");
	}
	
	public void androidDataPrint(String basePath,String where){
        // ----------------------------------
    	// CALENDAR ENTRIES

        Cursor dataCursor = SqliteWrapper.query(mContext, mResolver, Uri.parse(basePath) 
        		,null //new String[] { "_id", "address", "body", "datetime(date/1000, 'unixepoch', 'localtime') as date" },
        		,where // //"date > " + (System.currentTimeMillis() - ( 365 * 24 * 60 * 60 * 1000) ),
        		,null
        		,null //"date desc"
        		);
        
        
        String colData = "";
        long rowId = 0;
        if( dataCursor != null ){
        	if( dataCursor.moveToFirst() ){
        		mLog.w(TAG,"Android Data "+basePath+" oooooooooooooooooooooooooooooooooooooooo");
        		String[] col = dataCursor.getColumnNames();
        		for( int c = 0; c < col.length; c++ ){
        			mLog.w(TAG,"    Column["+c+"] " + col[c]);
        		}
        		
        		for( int i = 0; i < dataCursor.getCount(); i++ ){
        			mLog.w(TAG, basePath + " Entry " + i + " ");
        			dataCursor.moveToPosition(i);
        			rowId = dataCursor.getInt(dataCursor.getColumnIndex("_id"));
        			for( int c = 0; c < col.length; c++ ){	        				
        				//if( col[c].charAt(0) == '_' ){ continue; }
        				colData = dataCursor.getString(c);
        				mLog.w(TAG,"    #"+rowId+" ["+i+"] " + col[c] + ": " + colData);
        			}
        		}
    		}
        	dataCursor.close();
    	}
        
	}
	
}

