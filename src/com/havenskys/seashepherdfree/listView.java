package com.havenskys.seashepherdfree;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class listView extends ListActivity {

	private static String TAG = "List";
	//private Bundle mIntentExtras;
	private SharedPreferences mSharedPreferences;
	private Editor mPreferencesEditor;
	private long mCurrentID = 0;
	private Custom mLog;
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		
		mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();
		this.setTitle("" + id);
		
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.listview);
	  mLog = new Custom(this);
	
	  //mIntentExtras = getIntent().getExtras();
	  //long id = mIntentExtras != null ? mIntentExtras.getLong("id") : 0;

	  mSharedPreferences = getSharedPreferences("Preferences", MODE_WORLD_WRITEABLE);
	  mPreferencesEditor = mSharedPreferences.edit();
      long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",0) : 0;
      //mPreferencesEditor.putLong("id", id); mPreferencesEditor.commit();
		
	  mLog.loadlist(this);
	  loadRecord(id);
	
	}

	private void loadRecord(long id) {
		if( mCurrentID == id ){return;}
		mCurrentID = id;
		if( id > 0 ){
			int cnt = getListView().getCount();
			int position = 0;
			for( position = cnt; position > 0; position--){
				if( getListView().getItemIdAtPosition(position) == id ){
					break;
				}
			}
			getListView().setSelectionFromTop(position, 1);
			getListView().setSelected(true);
		}
	}


	
	
	@Override
	protected void onResume() {
		mLog.w(TAG,"onResume() ++++++++++++++++++++++++++++++++");
		
		super.onResume();
		long id = mSharedPreferences.contains("id") ? mSharedPreferences.getLong("id",0) : 0;
		loadRecord(id);
		
	}

}
