package com.blinduck.Postalgia;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;


public class WelcomeActivity extends Activity implements OnClickListener{

	public static final String SESSION = "order";
	public static final String FIRSTRUN ="firstrun";
	public static final String USERCREATED  = "userCreated";
	public static final String ALLVALID = "ALLVALID";
	public static final String PREFS_NAME = "prefs-file";

	private SharedPreferences prefs;
	private String android_id;

	private RelativeLayout rlPhotoPrint;


	@Override
	public void onBackPressed() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.welcome);
		initUI();
		
		prefs = getSharedPreferences(PREFS_NAME,MODE_PRIVATE);

		android_id = "Android-" + Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);


		if (android_id ==  null || android_id.isEmpty()) {
			//TODO CATASTROPHIC ERROR
			Log.d("Postal", "Catastrophic Error");
		}

		//set up imageloader configuration singleton
		//ImageLoaderConfiguration config  = ImageLoaderConfiguration.createDefault(getApplicationContext());
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext()).build();
		ImageLoader.getInstance().init(config);


		//when app first launched set order number to 1
		//check if the preferences file has the order key

		//set the session = 1 and server_upload=true on first run
		if (getFirstRun()) {
			setRunned();
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt(SESSION, 1);
			editor.putBoolean(ServerUpload.SERVER_UPLOAD_COMPLETE, true);
			editor.commit();
		}

		if ( checkUserCreated() == false) createUser();

		if ( !prefs.getBoolean(ServerUpload.SERVER_UPLOAD_COMPLETE, true) ) {

			//TODO Check for existing service

			//TODO Restasrt service if does not exist

		}


	}
	
	@Override
	public void onClick(View view) {
		
		if(view == rlPhotoPrint){
			getStarted(rlPhotoPrint);
		}
	}



	private void initUI() {
		rlPhotoPrint = (RelativeLayout)findViewById(R.id.rlPhotoPrint);
		rlPhotoPrint.setOnClickListener(this);
		
	}

	public boolean getFirstRun () {
		return prefs.getBoolean(FIRSTRUN, true);
	}


	public boolean checkUserCreated ()  {
		return prefs.getBoolean(USERCREATED, false);
	}

	public void setUserCreated () {

		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(USERCREATED, true);
		editor.commit();
		Log.d("Async", "User created set to true");
	}



	public void setRunned () {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(FIRSTRUN, false);
		editor.commit();
	}



	public void createUser () {
		RequestParams params = new RequestParams();
		params.put("uid", android_id);


		PostalgiaRestClient.put("appUsers/" + android_id + "/", params, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(String s) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("uid", android_id);
				editor.commit();
				setUserCreated();
			}

			@Override
			public void onFailure(Throwable throwable, String s) {
			}
		});
	}



	public void getStarted (View v) {
		Intent intent = new Intent(this, ActionBarView.class);
		startActivity(intent);
	}

	





}