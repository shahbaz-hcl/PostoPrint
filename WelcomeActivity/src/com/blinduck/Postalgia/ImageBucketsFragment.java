package com.blinduck.Postalgia;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/21/13
 * Time: 2:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageBucketsFragment extends SherlockFragment {

	//TODO See if the most common image buckets can be places first
	//i.e. anything starting with Media or the

	ListView buckets;
	private OnItemClickedListener listener;
	private View view;
	private DisplayImageOptions displayImageOptions;
	private ImageLoader imageLoader;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.image_buckets_fragment,
				container, false);


		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
		buckets = (ListView) view.findViewById(R.id.buckets_listview);
		//buckets.setBackgroundColor(Color.TRANSPARENT);
		//buckets.setCacheColorHint(Color.TRANSPARENT);

		ActionBar actionBar = getSherlockActivity().getSupportActionBar();
		actionBar.setTitle("Albums");

		imageLoader = ImageLoader.getInstance();


		displayImageOptions = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.white)
		//.cacheInMemory(true)
		//.cacheOnDisc(true)
		//.bitmapConfig(Bitmap.Config.RGB_565)
		.build();



		HashSet <String> bucketSet = new HashSet<String>();
		HashSet <String> imageSet = new HashSet<String>();

		HashMap <String, String>  imagesHash = new HashMap<String, String>();



		ArrayList<String> bucketList = new ArrayList<String>();
		ArrayList<String> imageList = new ArrayList<String>();

		String [] projection = new String[] {
				MediaStore.Images.Media._ID,
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
				MediaStore.Images.Media.DATA,
				MediaStore.Images.Media.DATE_TAKEN
		};

		Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


		Cursor cur = view.getContext().getContentResolver().query(images, projection, "", null, MediaStore.Images.Media.DATE_ADDED + " DESC");

		//come up with distinct bucket names
		if (cur.moveToFirst()) {
			String bucket;
			String imagePath;

			do {
				bucket = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
				if (!bucketSet.contains(bucket)) {

					bucketSet.add(bucket);
					//add the current image
					imagePath = cur.getString(cur.getColumnIndex(MediaStore.Images.Media.DATA));
					imagesHash.put(bucket, imagePath);
				}
			} while (cur.moveToNext());
		}

		bucketList.addAll(bucketSet);
		//imageList.addAll(imageSet);

		Log.d("Postal", bucketList.toString());
		Log.d("Postal", imageList.toString());


		cur.close();

		ImageBucketsAdapter adapter = new ImageBucketsAdapter(getActivity(), bucketList, imagesHash);

		//ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, bucketList);

		buckets.setAdapter (adapter);


		buckets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String selected = view.getTag().toString();
				listener.onBucketSelected(selected);
			}
		});

	}


	//listener that needs to be implemented by activity that hosts fragment
	public interface OnItemClickedListener {
		public void onBucketSelected (String bucketName);
	}

	@Override
	public void onAttach (Activity activity) {
		super.onAttach(activity);
		try {
			listener = (OnItemClickedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + "must implement listener");
		}
	}

	public class ImageBucketsAdapter extends ArrayAdapter<String> {
		private Context context;
		private ArrayList<String> bucketList;
		private HashMap<String, String> imageHash;

		public ImageBucketsAdapter(Context context, ArrayList<String> bucketList, HashMap<String, String> imageHash ) {
			super(context, R.layout.image_buckets_gridcell);
			this.bucketList = bucketList;
			this.imageHash = imageHash;
			this.context = context;

		}

		@Override
		public int getCount() {
			return bucketList.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = inflater.inflate(R.layout.image_buckets_gridcell, parent, false);
			TextView textView = (TextView)rowView.findViewById(R.id.image_buckets_textview);
			ImageView imageView = (ImageView)rowView.findViewById(R.id.image_buckets_image);

			String bucketName =bucketList.get(position);
			textView.setText(bucketName);
			String imagePath = imageHash.get(bucketName);
			Uri uri = Uri.fromFile(new File(imagePath));
			Picasso.with(context)
			.load(uri)
			.resize(90,90)
			.placeholder(R.drawable.white)
			.into(imageView);
			/*
            imageLoader.displayImage(uri, imageView, displayImageOptions, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String s, View view) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void onLoadingFailed(String s, View view, FailReason failReason) {
                    Log.d("Postal","Error: " + s);
                }

                @Override
                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void onLoadingCancelled(String s, View view) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }
            });
			 */
			rowView.setTag(bucketName);
			return rowView;
		}
	}

}