package com.blinduck.Postalgia.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

import com.blinduck.Postalgia.R;
import com.blinduck.Postalgia.WelcomeActivity;

public class SplashActivity extends Activity {
	private final String TAG = SplashActivity.class.getCanonicalName();
	private Handler mHandler;
	private int TIME_MAX = 5*1000;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_splash);
		Log.v(TAG, "onCreate");
		
		mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {			
			@Override
			public void run() {
				startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
				SplashActivity.this.finish();				
			}
		}, TIME_MAX);		
		
	}
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(mHandler!=null){
			mHandler.removeCallbacksAndMessages(null);
		}
	}
	

}
