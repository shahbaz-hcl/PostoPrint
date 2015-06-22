package com.blinduck.Postalgia;

import android.app.Activity;
import android.content.*;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
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
import com.blinduck.Postalgia.sqlite.SQLiteHelper;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/22/13
 * Time: 6:54 PM
 * To change this template use File | Settings | File Templates.
 */


//TODO Use a lazy loader to load the images

public class EdittingGridFragment extends SherlockFragment {
    public static String DATABASE_ID;

    private GridView mGridView;
    private ImageButton summaryButton;
    private EdittingGridCursorAdapter mGridAdapter;
    public static final int DIALOG_FRAGMENT = 1;
    public static final int DELETE_IMAGE_FRAGMENT = 2;
    public static final int EDIT_ALL = 3;
    public static final int DELETE_ALL = 4;
    private OnEdittingImageSelectedListener listener;
    private  ImageDataSource imageSource;

    public static final int EDITMENUITEM = 2234;

    private DisplayImageOptions displayImageOptions;

    private SQLiteDatabase database;
    private SharedPreferences prefs;

    //optons for the select size dialog
    public static final String [] options = {"4R (4\"x6\")", "Wallet (2.125\"x3.375\")", "Square (3\"x3\")"};
    public static final String [] displayOptions = {"Size: 4R", "Size: Wallet", "Size: Square"};

    //refer to the position of the options in the the dialog fragment
    public static final int SIZE4R = 0;
    public static final int SIZEWALLET = 1;
    public static final int SIZESQUARE = 2;

    public static final int LANDSCAPE = 0;
    public static final int PORTRAIT = 1;
    private Cursor cursor;

    private static Context mContext;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);    //To change body of overridden methods use File | Settings | File Templates.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.editting_gridview_fragment, container, false);
        mGridView = (GridView) view.findViewById(R.id.editting_gridview);
        summaryButton = (ImageButton)view.findViewById(R.id.editting_summary_button);
        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem edit = menu.add (Menu.NONE,EDITMENUITEM, Menu.NONE, "Edit");
        edit.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.findItem(R.id.menu_threebar).setVisible(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();

        ActionBar actionBar = getSherlockActivity().getSupportActionBar();
        actionBar.setTitle("Customize");

        imageSource = new ImageDataSource(getActivity());
        database = imageSource.getDatabase();

        //cursorLoader = new GridCursorLoader(getActivity());
        //Cursor cursor = cursorLoader.loadInBackground();

        cursor = getAllData();
        mGridAdapter = createAdapter(cursor);
        mGridView.setAdapter(mGridAdapter);

        summaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.launchSummaryFragment();
            }
        });
        /*
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getActivity().getApplicationContext()).build();

        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);

        displayImageOptions = new DisplayImageOptions.Builder()
                .showStubImage(R.drawable.white)
                .build();

        */

    }


    public EdittingGridCursorAdapter createAdapter (Cursor cursor) {
        /*return new EdittingGridCursorAdapter(getActivity(),
                R.layout.editting_grid_cell,
                cursor,
                new String[]{SQLiteHelper.ORIGINAL_IMAGE_LOC}, new int[]{R.id.editting_gridcell_image}, 0);*/
        return new EdittingGridCursorAdapter(getActivity(), cursor);
    }


    @Override
    public void onResume() {
        if (!database.isOpen())database = imageSource.getDatabase();
        mGridAdapter.changeCursor(getAllData());
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void onStop() {
        if (database.isOpen()) database.close();
        if (cursor != null) cursor.close();
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
    }



    public Cursor getAllData () {
        //return  cursorLoader.loadInBackground();
        prefs = getActivity().getSharedPreferences(WelcomeActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int session = prefs.getInt(WelcomeActivity.SESSION, 0);

        return  database.query(SQLiteHelper.IMAGE_TABLE, ImageDataSource.allColumns,
                SQLiteHelper.SESSION + "=?",
                new String[] {String.valueOf(session)}, "", "", "");
    }




    public class EdittingGridCursorAdapter extends CursorAdapter {
        private Context mContext;
        private HashMap <Integer, Integer> quantityMap = new HashMap<Integer, Integer>(cursor.getCount());

        public EdittingGridCursorAdapter(Context context, Cursor c) {
            super(context, c, FLAG_REGISTER_CONTENT_OBSERVER);
            mContext = context;

        }






        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater =  (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.editting_grid_cell, null);
            ViewHolder holder = new ViewHolder();
            holder.imageView =(ImageView) view.findViewById(R.id.editting_gridcell_image);
            holder.typeButton = (Button) view.findViewById(R.id.editting_gridcell_pick_size);
            holder.reduceButton =(ImageButton) view.findViewById(R.id.editting_gridcell_reduce);
            holder.increaseButton =(ImageButton) view.findViewById(R.id.editting_gridcell_increase);
            holder.rotateButton =(ImageButton)view.findViewById(R.id.editting_gridcell_rotate);
            holder.quantityText =(EditText) view.findViewById(R.id.editting_gridcell_quantity);
            holder.position = cursor.getPosition();
            view.setTag(holder);
            return view;


        }

        @Override
        public void changeCursor(Cursor cursor) {
            Log.d("Postal", "change cursor called");
            super.changeCursor(cursor);    //To change body of overridden methods use File | Settings | File Templates.

        }

        private  class ViewHolder {
            ImageView imageView;
            Button typeButton;
            ImageButton reduceButton, increaseButton, rotateButton;
            EditText quantityText;
            int position;


        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            final int position = holder.position;
            ImageView imageView = holder.imageView;
            //imageView.setId(position);
            Button typeButton = holder.typeButton;
            ImageButton reduceButton = holder.reduceButton;
            ImageButton  increaseButton = holder.increaseButton;
            ImageButton rotateButton = holder.rotateButton;
            final EditText quantityText = holder.quantityText;

            //ImageView imageView = (ImageView) view.findViewById(R.id.editting_gridcell_image);

            //Button typeButton = (Button) view.findViewById(R.id.editting_gridcell_pick_size);
            //ImageButton reduceButton = (ImageButton) view.findViewById(R.id.editting_gridcell_reduce);
            //ImageButton increaseButton = (ImageButton)view.findViewById(R.id.editting_gridcell_increase);
            //ImageButton rotateButton = (ImageButton)view.findViewById(R.id.editting_gridcell_rotate);
            //final EditText quantityText = (EditText) view.findViewById(R.id.editting_gridcell_quantity);

            /* Setting of Image view */

            int width = (int) convertToPixels(100);
            int height = (int) convertToPixels(100);


            final int imageType = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.TYPE));
            final int imageOrientation = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.ORIENTATION));
            int editStatus = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.EDITTED_STATUS));
            final int database_id =  cursor.getInt(cursor.getColumnIndex(SQLiteHelper._ID));
            final int currentImageQuantity = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.QUANTITY));
            final int image_id = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.IMAGE_ID));

            if (!quantityMap.containsKey(database_id)) {  //does not contain the key, add it
                quantityMap.put(database_id,  currentImageQuantity);
                Log.d("Postal", String.format("Added database_id: %s, quantity: %s", database_id, currentImageQuantity));
            } else {
                Log.d("Postal", "Already exists in map");
            }


            switch (imageType) {
                case SIZE4R :
                    if (imageOrientation == LANDSCAPE) {
                        width = (int) convertToPixels(150);
                        height = (int) convertToPixels(100);
                    } else if (imageOrientation ==PORTRAIT ) {
                        width = (int) convertToPixels(66);
                        height = (int) convertToPixels(100);
                    }
                    break;


                case SIZEWALLET:
                    if (imageOrientation == LANDSCAPE) {
                        width = (int) convertToPixels(150);
                        height = (int) convertToPixels(91);

                    } else if (imageOrientation ==PORTRAIT ) {
                        width = (int) convertToPixels(61);
                        height = (int) convertToPixels(100);
                    }
                    break;
                case SIZESQUARE:
                    width = (int) convertToPixels(100);
                    height = (int) convertToPixels(100);
                    break;
            }

            ViewGroup.LayoutParams params = imageView.getLayoutParams();
            params.height = height;
            params.width = width;
            imageView.setLayoutParams(params);

            //imageView.getLayoutParams().height = height;
            //imageView.getLayoutParams().width = width;

            //frame.setBackgroundColor(Color.BLACK);

            String path = "";

            if (editStatus == 0 ) path = cursor.getString(cursor.getColumnIndex(SQLiteHelper.ORIGINAL_IMAGE_LOC));
            else if (editStatus == 1) {
                Log.d("Editting", "Editstatus = 1");
                path = cursor.getString(cursor.getColumnIndex(SQLiteHelper.NEW_IMAGE_LOC));
            }

            //String uri = Uri.fromFile(new File(path)).toString();
            //imageLoader.displayImage(uri, imageView, displayImageOptions);

            //imageView.setImageBitmap(decodeSampledBitmapFromPath(path, width,height));
            imageView.setId(database_id);
            imageView.setImageResource(R.drawable.white);
            ImageLoader imageLoader = new ImageLoader(imageView, database_id);
            imageLoader.execute(path, width, height);


            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d ("Edit", "image id:" + image_id + " image type: " + imageType + " image orientation: " + imageOrientation );
                    listener.edittingImageSelected(image_id, imageType, imageOrientation, database_id);
                }
            });

            /*Set the size of image*/

            typeButton.setText(displayOptions[imageType]);
            typeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Bundle bundle  = new Bundle();
                    bundle.putInt(DATABASE_ID, database_id);
                    launchSelectSizeDialog(bundle);
                }
            });

            /* Edit text value changes triggers an async task to changed quantity values in the databse*/
            //quantityText.setText(String.valueOf(currentImageQuantity));
            quantityText.setText(String.valueOf(quantityMap.get(database_id)));


            /*
            quantityText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                @Override
                public void afterTextChanged(Editable s) {

                    int newVal = Integer.parseInt(s.toString());
                    if (newVal !=  quantityMap.get(database_id)) {
                        quantityMap.put(database_id, newVal);
                        Log.d("Postal", String.format("database_id:%s, new val:%s", database_id, newVal));
                    }
                }


            });
            */




            /*Set quantity of image*/

            increaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentVal = Integer.valueOf(quantityText.getText().toString());
                    int newVal = currentVal +1;
                    quantityText.setText(String.valueOf(newVal));
                    quantityMap.put(database_id, newVal);

                    UpdateImageQuantityTask task = new UpdateImageQuantityTask();
                    task.execute(database_id, newVal);

                    //ContentValues values = new ContentValues();
                    //values.put(SQLiteHelper.QUANTITY, currentImageQuantity + 1);
                    //databaseUpdate(values, database_id);
                }
            });

            reduceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int currentVal = Integer.valueOf(quantityText.getText().toString());
                    int newVal = currentVal-1;

                    if (newVal == 0 ) {
                        showDeleteImageDialog(database_id);

                    } else {
                        quantityText.setText(String.valueOf(newVal));
                        quantityMap.put(database_id, newVal);
                        UpdateImageQuantityTask task = new UpdateImageQuantityTask();
                        task.execute(database_id, newVal );
                    }

                    //ContentValues values = new ContentValues();
                    //values.put(SQLiteHelper.QUANTITY, currentImageQuantity - 1);
                    //databaseUpdate(values, database_id);

                }
            });


            /*Set orientation of image*/

            rotateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Postal", "Rotate Pressed");
                    ContentValues values = new ContentValues();
                    if (imageOrientation == LANDSCAPE) values.put(SQLiteHelper.ORIENTATION, PORTRAIT);
                    else values.put(SQLiteHelper.ORIENTATION, LANDSCAPE);
                    values.put(SQLiteHelper.EDITTED_STATUS, 0);
                    DatabaseUpdateTask updateTask =  new DatabaseUpdateTask();
                    updateTask.execute(values, database_id);

                    //databaseUpdate(values, database_id);
                    //mGridAdapter.changeCursor(getAllData());
                }
            });

        }
    }

    public class ImageLoader extends AsyncTask <Object, Void, Bitmap> {

        private WeakReference<ImageView> mImageViewWeakReference;
        private int mPosition;


        public ImageLoader(ImageView imageView, int position) {
            mImageViewWeakReference = new WeakReference<ImageView>(imageView);
            mPosition = position;

        }

        @Override
        protected Bitmap doInBackground(Object... params) {
            String path  = (String) params[0];
            int width = (Integer) params[1];
            int height =  (Integer) params[2];



            Bitmap bitmap =  decodeSampledBitmapFromPath(path, width, height);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (mImageViewWeakReference !=null
                    && mImageViewWeakReference.get() != null
                        && mPosition == mImageViewWeakReference.get().getId()) {
                mImageViewWeakReference.get().setImageBitmap(bitmap);
            }
        }
    }




    private void changeImageType(int newImageType, int database_id) {
        Log.d("Edit", "database id: " + database_id);
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.TYPE, newImageType);
        values.put(SQLiteHelper.EDITTED_STATUS, 0);
        databaseUpdate(values, database_id);
        //mGridAdapter.notifyDataSetChanged();
    }

    //TODO use async task to do updates
    private void databaseUpdate (ContentValues values, int database_id) {
        database.update(SQLiteHelper.IMAGE_TABLE, values, SQLiteHelper._ID + " = ?",
                new String[]{String.valueOf(database_id)});
    }

    public void showDeleteImageDialog (int database_id) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        DeleteImageDialog dialog = new DeleteImageDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("database_id", database_id);
        dialog.setArguments(bundle);
        dialog.setTargetFragment(this, DELETE_IMAGE_FRAGMENT);
        dialog.show(fm, "");
    }



    public float convertToPixels (int dp) {
        Resources r = getResources();
        return  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    public void launchSelectSizeDialog(Bundle bundle) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        SelectSizeDialog editNameDialog = new SelectSizeDialog();
        editNameDialog.setArguments(bundle);
        editNameDialog.setTargetFragment(this, DIALOG_FRAGMENT);
        editNameDialog.show(fm, "fragment_edit_name");
    }

    private void launchEditAllDialogFragment () {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        EditAllDialogFragment dialog = new EditAllDialogFragment();
        dialog.setTargetFragment(this, EDIT_ALL);
        dialog.show(fm, "" );
    }

    private void launchDeleteAllDialogFragment () {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        DeleteAllDialogFragment dialog = new DeleteAllDialogFragment();
        dialog.setTargetFragment(this, DELETE_ALL);
        dialog.show(fm, "");

    }



    @Override
    public void onActivityResult(int requestCode, int result, Intent data) {
        switch (requestCode) {
            case DIALOG_FRAGMENT:    //change image type
                Bundle bundle = data.getExtras();
                int database_id = bundle.getInt(DATABASE_ID);
                changeImageType(result , database_id);
                mGridAdapter.changeCursor(getAllData());
                //mGridAdapter.notifyDataSetChanged();
                //mGridView.invalidateViews();
                break;
            case DELETE_IMAGE_FRAGMENT:  //delete image
                //result = database_id
                database.delete(SQLiteHelper.IMAGE_TABLE, SQLiteHelper._ID + "=?",
                        new String[]{String.valueOf(result)});
                mGridAdapter.changeCursor(getAllData());
                break;
            case EDIT_ALL:
                switch (result) {
                    case 0:
                    case 1:
                    case 2:
                        changeAllImageTypes(result);
                        break;
                    case 3:
                        launchDeleteAllDialogFragment();
                        //deleteAll();
                        break;
                } break;
            case DELETE_ALL:
                switch (result) {
                    case 1:
                        deleteAll();
                        break;
                }
        }
    }

    private void changeAllImageTypes(int result) {
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.TYPE, result);
        database.update(SQLiteHelper.IMAGE_TABLE, values, null, null);
        mGridAdapter.changeCursor(getAllData());
    }

    private void deleteAll() {
        database.delete(SQLiteHelper.IMAGE_TABLE, SQLiteHelper.SESSION + "=?",
                new String[] {String.valueOf(prefs.getInt(WelcomeActivity.SESSION, 0))});
        mGridAdapter.changeCursor(getAllData());
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  EDITMENUITEM) {
            launchEditAllDialogFragment();
        }

        return super.onOptionsItemSelected(item);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromPath(String path,int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);


        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }


    public interface OnEdittingImageSelectedListener {
        public void edittingImageSelected (int image_id, int imageType, int imageOrientation, int database_id);
        public void launchSummaryFragment ();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnEdittingImageSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement listener");
        }
    }

    private class UpdateImageQuantityTask extends AsyncTask <Integer, Void, Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            //xmGridAdapter.changeCursor(getAllData());
            //mGridAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            int database_id = params[0];
            int new_value = params [1];
            ContentValues values = new ContentValues();

            values.put(SQLiteHelper.QUANTITY, new_value);

            database.update(SQLiteHelper.IMAGE_TABLE, values, SQLiteHelper._ID + " = ?",
                    new String[]{String.valueOf(database_id)});

            return null;
        }
    }


    public class GridCursorLoader extends SimpleCursorLoader {

        public GridCursorLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            prefs = getActivity().getSharedPreferences(WelcomeActivity.PREFS_NAME, Context.MODE_PRIVATE);
            int session = prefs.getInt(WelcomeActivity.SESSION, 0);

            return  database.query(SQLiteHelper.IMAGE_TABLE, ImageDataSource.allColumns,
                    SQLiteHelper.SESSION + "=?",
                    new String[] {String.valueOf(session)}, "", "", "");
        }
    }

    //takes in content values as first object and database_id as second
    public class DatabaseUpdateTask extends AsyncTask<Object, Void, Void> {
        @Override
        protected Void doInBackground(Object... params) {
            ContentValues values = (ContentValues) params[0];
            int database_id = (Integer) params[1];
            database.update(SQLiteHelper.IMAGE_TABLE, values, SQLiteHelper._ID + " = ?",
                    new String[]{String.valueOf(database_id)});
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Log.d("Postal", "update complte, swap cursor");
            mGridAdapter.changeCursor(getAllData());
            mGridAdapter.notifyDataSetChanged();

        }
    }






}


