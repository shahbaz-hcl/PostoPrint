package com.blinduck.Postalgia;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.blinduck.Postalgia.sqlite.ImageDataSource;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/21/13
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImagesGridFragment extends SherlockFragment {

    //TODO use a lazy loader to load images.

    //TODO remove the use of the image object

    private String bucketName;
    private GridView mGridView;
    private View view;
    private CheckBoxAdapter mCheckBoxAdapter;



    protected ArrayList <String> list = new ArrayList <String> ();
    protected ArrayList<Boolean> itemChecked = new ArrayList<Boolean>();

    private int session;
    final Uri sourceUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    final Uri thumbUri = MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
    final String thumb_data = MediaStore.Images.Thumbnails.DATA;
    final String thumb_image_id = MediaStore.Images.Thumbnails.IMAGE_ID;
    private ImageDataSource imageSource;

    public static final int NEXTMENUITEM =    1234;
    private DisplayImageOptions displayImageOptions;
    private ImageLoader imageLoader;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.images_grid_fragment, container, false);
        mGridView = (GridView) view.findViewById(R.id.image_select_grid_view);
        bucketName =  getArguments().getString("buckettag");

        SharedPreferences prefs = getActivity().getSharedPreferences(WelcomeActivity.PREFS_NAME ,Context.MODE_PRIVATE);
        session = prefs.getInt(WelcomeActivity.SESSION, 0);
        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);


        MenuItem next = menu.add(Menu.NONE, NEXTMENUITEM, Menu.NONE, "");
        next.setIcon(R.drawable.tick);
        next.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.menu_threebar).setVisible(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.

        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setTitle(bucketName);

        Activity activity = getActivity();

        imageSource = new ImageDataSource(activity);
        try {
            imageSource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }



        //ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getApplicationContext()).build();

        imageLoader = ImageLoader.getInstance();


        displayImageOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.white)
                .cacheInMemory(true)
                .cacheOnDisc(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();

        CursorLoader cursorLoader = new CursorLoader(getActivity(),
                sourceUri, new String[] {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA, MediaStore.Images.Media.ORIENTATION},
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?",
                new String[] {bucketName},
                MediaStore.Images.Media.DATE_ADDED + " DESC");
        //cursor has all images in the relevant bucket
        Cursor cursor = cursorLoader.loadInBackground();

        mCheckBoxAdapter = new CheckBoxAdapter(getActivity(), cursor);
        mGridView.setAdapter(mCheckBoxAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
                // id in this case should refer to id of the thumbnails
                // Still need to query to get the original image

                CheckBox cb = (CheckBox) view.findViewById(R.id.thumbnail_checkbox);

                cb.toggle();
                if (cb.isChecked()) {
                    itemChecked.set(pos, true);
                    String path = (String) view.getTag(R.integer.image_path);
                    int image_id = (Integer)view.getTag(R.integer.image_id);
                    int orientation = (Integer)view.getTag(R.integer.orientation);

                    View overlay = (View)view.findViewById(R.id.thumbnail_overlay);
                    int opacity = 150;
                    overlay.setBackgroundColor(opacity * 0x1000000);


                    Log.d("Image", "Path is: " + path + " & image_id is: " + image_id);

                    int databaseId = (int) imageSource.createImage(path, image_id, session, orientation);
                    view.setTag(R.integer.database_id, databaseId);
                    Log.d ("Image", " database id:" + databaseId);




                } else if (!cb.isChecked()) {
                    View overlay = (View)view.findViewById(R.id.thumbnail_overlay);
                    int opacity = 0;
                    overlay.setBackgroundColor(opacity * 0x1000000);

                    itemChecked.set(pos, false);
                    int databaseId =  (Integer)view.getTag(R.integer.database_id);
                    imageSource.deleteImage(databaseId);
                    Log.d("Image", "removed from database at position: " + databaseId );
                    //remove the id of the picture from global storage


                }
            }
        });


    }

    @Override
    public void onResume() {
        imageSource = new ImageDataSource(getActivity());
        try {
            imageSource.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        super.onResume();

    }

    @Override
    public void onStop() {
        imageSource.close();
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public class CheckBoxAdapter extends SimpleCursorAdapter  {
        Context mContext;
        BitmapFactory.Options options;


        public CheckBoxAdapter(Context context, Cursor c) {
            super(context, R.layout.thumbnail_grid_cell, c,
                    new String[]{MediaStore.Images.Media._ID},
                    new int[]{android.R.layout.simple_list_item_1}, 0);
            mContext = context;

            //set all to be false
            for (int i = 0; i < this.getCount(); i++) {
                itemChecked.add(i, false);
            }

            options = new BitmapFactory.Options();
            options.inSampleSize = 2;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater =  (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.thumbnail_grid_cell, null);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final CheckBox cBox =  (CheckBox) view.findViewById(R.id.thumbnail_checkbox);
            final ImageView iView = (ImageView) view.findViewById(R.id.thumbnail);
            final View overlay = view.findViewById(R.id.thumbnail_overlay);

            int pos = cursor.getPosition();
            long origId = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Uri uri = Uri.fromFile(new File(imagePath));

            view.setTag(R.integer.image_path, imagePath);
            view.setTag(R.integer.image_id, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
            view.setTag(R.integer.orientation, cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION)));

            //set the checkbox status
            cBox.setChecked(itemChecked.get(pos));

            if (cBox.isChecked()) {
                int opacity = 150;
                overlay.setBackgroundColor(opacity * 0x1000000);
            } else {
                int opacity = 0;
                overlay.setBackgroundColor(opacity * 0x1000000);
            }

            Picasso.with(context)
                    .load(uri)
                    .resize(150,100)
                    .centerInside()
                    .placeholder(R.drawable.editting_gridview_border)
                    .into(iView);

            //imageLoader.displayImage(uri, iView, displayImageOptions);


            /*
            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                    getActivity().getContentResolver(),
                    origId,
                    MediaStore.Images.Thumbnails.MINI_KIND,
                    options
            );
            */

            //iView.setImageBitmap(bitmap);


        }
    }



}