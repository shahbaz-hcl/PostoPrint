package com.blinduck.Postalgia;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.blinduck.Postalgia.Services.CreateScaledImagesService;
import com.blinduck.Postalgia.Services.SendImagesService;


/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/20/13
 * Time: 5:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActionBarView extends SherlockFragmentActivity
implements ImageBucketsFragment.OnItemClickedListener,
EdittingGridFragment.OnEdittingImageSelectedListener,
OrderSummary.OrderSummaryInterface,
PaypalWebview.PaypalInterface,
ServerUpload.ServerUploadInterface {

	ActionBarDrawerToggle mDrawerToggle;
	DrawerLayout mDrawerLayout;
	FragmentManager fm;
	public static String BUCKETTAG = "buckettag";

	public static String IMAGE_ID = "image_id";
	public static String IMAGE_TYPE = "image_type";
	public static String IMAGE_ORIENTATION = "image_orientation";
	public static String DATABASE_ID = "database_id";



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu,  menu);
		return super.onCreateOptionsMenu(menu);
	}

	//Menu button opens and closes the drawer menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle menu item selection

		if(item.getItemId() == R.id.menu_threebar) {
			if (mDrawerLayout.isDrawerVisible(GravityCompat.END)) {
				mDrawerLayout.closeDrawer(GravityCompat.END);
			} else {
				mDrawerLayout.openDrawer(GravityCompat.END);
			}
		} else if (item.getItemId() ==  ImagesGridFragment.NEXTMENUITEM) {
			//Editting grid fragment
			switchTo(EdittingGridFragment.class, null);
		} else if (item.getItemId() == android.R.id.home) {
			//home button pressed
			if (!fm.popBackStackImmediate()) startActivity(new Intent(this, WelcomeActivity.class));
		}



		return super.onOptionsItemSelected(item);
	}




	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_holder);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);

		fm = getSupportFragmentManager();

		FragmentTransaction ft = fm.beginTransaction();
		ImageBucketsFragment fragment = new ImageBucketsFragment();
		ft.add(R.id.layout_container, fragment);
		ft.commit();

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		mDrawerToggle = new ActionBarDrawerToggle(
				this,
				mDrawerLayout,
				R.drawable.back_caret,
				R.string.drawer_open,
				R.string.drawer_close
				) {
			public void onDrawerClosed (View v) {}
			public void onDrawerOpened (View v) {}
		};


		String [] menuOptions = getResources().getStringArray(R.array.menu_options_array);
		ListView  mDrawerList = (ListView) findViewById(R.id.left_drawer);
		//mDrawerList.setAdapter(new ArrayAdapter<String>(this , android.R.layout.simple_list_item_1 , menuOptions));
		String [] values = new String [] {"Choose", "Customize", "Checkout","FAQ", "Settings", "Pricing"};

		mDrawerList.setAdapter(new DrawerListArrayAdapter(this, values));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
				case 0:
					//choose
					switchTo(ImageBucketsFragment.class, null);
					mDrawerLayout.closeDrawer(GravityCompat.END);
					break;
				case 1:
					//customize
					switchTo(EdittingGridFragment.class, null);
					mDrawerLayout.closeDrawer(GravityCompat.END);
					break;
				case 2:
					//order summary
					switchTo(OrderSummary.class, null);
					mDrawerLayout.closeDrawer(GravityCompat.END);
					break;
				case 3:
					//about us
					mDrawerLayout.closeDrawer(GravityCompat.END);
					Intent intent2 = new Intent (getBaseContext(), StaticWebview.class);
					intent2.putExtra("type", 1);
					startActivity(intent2);
					break;

				case 4:
					//settings
					mDrawerLayout.closeDrawer(GravityCompat.END);
					Intent intent = new Intent (getBaseContext(), SettingsActivity2.class);
					startActivity(intent);
					break;

				case 5:
					//Pricing tab
					mDrawerLayout.closeDrawer(GravityCompat.END);
					Intent intent3 = new Intent (getBaseContext(), StaticWebview.class);
					intent3.putExtra("type", 2);
					startActivity(intent3);
					break;


				}

			}
		});

		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}


	//Interface for selecting image bucket
	//When a bucket is selected swap out the fragment for the gridview
	@Override
	public void onBucketSelected(String bucketName) {

		Bundle bundle = new Bundle();
		bundle.putString("buckettag", bucketName);

		switchTo(ImagesGridFragment.class, bundle);

	}

	public void switchTo (Class className, Bundle bundle) {
		FragmentTransaction ft = fm.beginTransaction();
		Fragment mFragement = SherlockFragment.instantiate(this, className.getName(), bundle);
		ft.replace(R.id.layout_container, mFragement);
		ft.addToBackStack(null);
		ft.commit();
	}

	public void switchTo(Class className, Bundle bundle, String tag) {
		FragmentTransaction ft = fm.beginTransaction();
		Fragment mFragement = SherlockFragment.instantiate(this, className.getName(), bundle);
		ft.replace(R.id.layout_container, mFragement, tag);
		ft.addToBackStack(null);
		ft.commit();
	}


	//Launch image processing activity
	@Override
	public void edittingImageSelected(int image_id, int imageType, int imageOrientation, int database_id) {
		//Launch editting activity
		Intent intent = new Intent(this, ImageProcessing.class);

		Log.d("Edit", String.format("Actionbar Values: %s %s %s", image_id, imageType, imageOrientation));

		Bundle bundle = new Bundle();
		bundle.putInt(IMAGE_ID, image_id);
		bundle.putInt(IMAGE_TYPE, imageType );
		bundle.putInt(IMAGE_ORIENTATION, imageOrientation);
		bundle.putInt(DATABASE_ID, database_id);

		intent.putExtras(bundle);

		startActivity(intent);
	}

	public class DrawerListArrayAdapter extends ArrayAdapter <String> {
		private Context mContext;
		private String [] values;
		private int[] images =  new int[] {R.drawable.choose, R.drawable.customize, R.drawable.checkout,R.drawable.about, R.drawable.settings, R.drawable.pricing};


		public DrawerListArrayAdapter(Context context, String[] values) {
			super(context, R.layout.drawer_list_item, values);
			mContext = context;
			this.values = values;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.drawer_list_item, parent, false);
			ImageView imageView = (ImageView)view.findViewById(R.id.drawer_icon);
			TextView textView = (TextView)view.findViewById(R.id.drawer_text);
			imageView.setImageResource(images[position]);
			textView.setText(values[position]);
			return view;
		}
	}


	@Override
	public void launchSummaryFragment() {
		switchTo(OrderSummary.class, null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//handle returning from image processing view.
		super.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public void startPaypalFragment() {
		switchTo(PaypalWebview.class, null);
		//switchTo(ServerUpload.class, null);
	}

	@Override
	public void updateParticulars() {
		Intent intent = new Intent (getBaseContext(), SettingsActivity2.class);
		startActivity(intent);
	}

	@Override
	public void startCreateImagesService() {
		Intent intent = new Intent(this, CreateScaledImagesService.class);
		intent.putExtra(WelcomeActivity.SESSION, getSharedPreferences(WelcomeActivity.PREFS_NAME, MODE_PRIVATE).
				getInt(WelcomeActivity.SESSION, 0));
		startService(intent);
	}

	@Override
	public void dialogCancelPressed() {
		switchTo(EdittingGridFragment.class, null);
	}


	//changing manually to skip adding to back stack
	@Override
	public void paymentSuccessful() {
		FragmentTransaction ft = fm.beginTransaction();
		Fragment mFragement = SherlockFragment.instantiate(this, ServerUpload.class.getName(), null);
		ft.replace(R.id.layout_container, mFragement, "SERVER_UPLOAD");
		ft.commit();
	}

	@Override
	public void paymentUnsucessful() {
		switchTo(OrderSummary.class, null);
		Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
	}

	//changing manually to skip adding to back stack
	@Override
	public void paypalNetworkUnavailable() {
		FragmentTransaction ft = fm.beginTransaction();
		Fragment mFragement = SherlockFragment.instantiate(this, OrderSummary.class.getName(), null);
		ft.replace(R.id.layout_container, mFragement);
		ft.commit();
	}


	@Override
	public void startServiceAgain() {
		startCreateImagesService();
	}

	@Override
	public void switchToWelcome() {
		for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
			fm.popBackStack();
		}

		Intent intent = new Intent(this, WelcomeActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		ServerUpload  serverFragment = (ServerUpload)fm.findFragmentByTag("SERVER_UPLOAD");
		if (serverFragment != null) {
			if (serverFragment.isVisible()) switchToWelcome();
		} else {
			super.onBackPressed();    //To change body of overridden methods use File | Settings | File Templates.
		}


	}
}