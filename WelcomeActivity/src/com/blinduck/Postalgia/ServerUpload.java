package com.blinduck.Postalgia;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockFragment;
import com.blinduck.Postalgia.Services.CreateScaledImagesService;
import com.blinduck.Postalgia.sqlite.ImageDataSource;
import com.blinduck.Postalgia.sqlite.SQLiteHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/7/13
 * Time: 5:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class ServerUpload extends SherlockFragment {

    ServerUploadInterface listener;

    ImageDataSource dataSource;
    SQLiteDatabase database;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    TextView text;
    ImageButton button;
    int prevSession;

    public static final int UPLOAD_ERROR_DIALOG = 321;

    public static final String SERVER_UPLOAD_COMPLETE = "server_upload_complete";
    public static final String CUSTOMER_AUTHORIZATION  = "customerAuthorization";
    Cursor cursor;
    private String [] serverDatabaseSizeNames  = {"k4R", "kWallet", "kSquare"};

    public ProgressDialog progressDialog;
    public ProgressDialog imagesDialog;

    public ImageScalingNotice onNotice = new ImageScalingNotice();
    boolean serverUpload;

    String finishedText = "Your order has been successfully processed and will be posted out within a day.\n Thank you for using Postalgia Prints!";



    /*
    *  For an order to be considered complete
     *  1. Customer authorization must be given --> Set to true
     *  2. Upload status of every item  --> Set to true
    *
    * Then
    *   1. Increment the session number
    *   2. Delete all associated images
    *
    * */


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.server_upload , container, false);
        text = (TextView) view.findViewById(R.id.server_upload_textview);
        button = (ImageButton)view.findViewById(R.id.server_upload_button);


        return view;

    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        dataSource = new ImageDataSource(getActivity());
        database = dataSource.getDatabase();
        prefs = getActivity().getSharedPreferences(WelcomeActivity.PREFS_NAME, Context.MODE_PRIVATE);
        int session = prefs.getInt(WelcomeActivity.SESSION, 0);
        editor = prefs.edit();

        editor.putBoolean("customerAuthorization", false);
        editor.commit();


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.switchToWelcome();
            }
        });
        serverUpload = false;

        //check if  voucher code was used and update accordingly
        updateVoucherCode ();


        //start image scaling service again
        listener.startServiceAgain();
        //start a progress dialog here


    }


    private void authorizePayment() {
        //authorize payment
        RequestParams params = new RequestParams();
        params.put("orderId",  prefs.getString("orderId", ""));
        params.put(CUSTOMER_AUTHORIZATION, "1");

        PostalgiaRestClient.post("authorization/", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String s) {   //payment has successfully been authorized
                Log.d("Postal", "Update :" + s);
                prevSession = prefs.getInt(WelcomeActivity.SESSION, 0);
                editor.putBoolean(CUSTOMER_AUTHORIZATION, true);
                editor.putInt(WelcomeActivity.SESSION, (prevSession + 1));
                editor.commit();

                //Launch the service to upload all the images to the server from here.

                //check if scaled images have been created
                if (prefs.getBoolean(OrderSummary.SCALED_IMAGE_SERVICE_COMPLETE, false)) {
                    editor.putBoolean(SERVER_UPLOAD_COMPLETE, false);
                    editor.commit();
                    sendImages(prevSession);
                } else {

                }
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                Log.d("Async", "Fail:" + s);
            }
        });
    }

    public class ImageScalingNotice extends BroadcastReceiver  {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Postal", "Receiver called");
            int totalImages =  intent.getIntExtra("totalImages", 0);
            int currentImage = intent.getIntExtra("currentImage", 0);
            boolean complete = intent.getBooleanExtra("complete", false);

            if ((imagesDialog == null) || (!imagesDialog.isShowing())) {
                setupImageProcessingDialog(totalImages, currentImage);
            }

            if (!complete) imagesDialog.setProgress(currentImage);
            else {  //image scaling complete
                imagesDialog.dismiss();
                if (!serverUpload) {
                    authorizePayment();
                    serverUpload = true;
                }

            }
        }
    }


    private void updateVoucherCode() {
        String voucherCode = prefs.getString("voucherCode", "");
        if (voucherCode != null && !voucherCode.isEmpty()) {
            Log.d("Postal", "Voucher code :" + voucherCode);
            RequestParams voucherParams = new RequestParams();
            voucherParams.put("voucherCode", voucherCode);
            voucherParams.put("uid", prefs.getString("uid", ""));
            String url = String.format("orders/%s/", prefs.getString("orderId", ""));
            PostalgiaRestClient.put(url, voucherParams, new AsyncHttpResponseHandler(){
                @Override
                public void onSuccess(String s) {
                    Log.d("Postal", "Voucher success:" + s);
                    editor.remove("voucherCode");
                    editor.commit();
                }

                @Override
                public void onFailure(Throwable throwable, String s) {
                    Log.d("Postal", "Voucher failure: " + s);
                    updateVoucherCode();
                }
            });
        }
    }

    public interface ServerUploadInterface {
        public void startServiceAgain();
        public void switchToWelcome ();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (ServerUploadInterface) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + "must implement listener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();    //To change body of overridden methods use File | Settings | File Templates.
        if (!database.isOpen()) {
            dataSource = new ImageDataSource(getActivity());
            database = dataSource.getDatabase();
        }
        //register for broadcast receiver
        Log.d("Postal", "registering receiver");
        IntentFilter iff = new IntentFilter(CreateScaledImagesService.BROADCAST);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(onNotice,  iff);


    }


    @Override
    public void onPause() {
        super.onPause();
        database.close();
        Log.d("Postal", "unregister receiver");
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(onNotice);
    }

    public void sendImages (int prevSession) {
        //get images from previous session that have not been uploaded yet
        cursor = database.query(SQLiteHelper.IMAGE_TABLE, new String[] {SQLiteHelper._ID, SQLiteHelper.NEW_IMAGE_LOC, SQLiteHelper.QUANTITY, SQLiteHelper.TYPE},
                SQLiteHelper.SESSION + "=? AND " + SQLiteHelper.UPLOAD_STATUS + "=?" , new String[] {String.valueOf(prevSession), String.valueOf(0)},
                null,null, null);

        int totalImages = cursor.getCount();
        setUpProgressDialog(totalImages);
        Log.d("Postal", "Total Images:" + totalImages);

        if (cursor.moveToFirst()) sendNext();

    }



    private void setupImageProcessingDialog (int totalImages, int currentImage) {
        imagesDialog = new ProgressDialog(getActivity());
        imagesDialog.setCancelable(false);
        imagesDialog.setMessage("Processing Files");
        imagesDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        imagesDialog.setProgress(currentImage);
        imagesDialog.setMax(totalImages);
        imagesDialog.show();


    }



    private void setUpProgressDialog(int totalImages) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Uploading Files");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(totalImages);
        progressDialog.show();
    }

    public void sendNext () {
        try {
            sendImage(
                    cursor.getString(cursor.getColumnIndex(SQLiteHelper.NEW_IMAGE_LOC)),
                    cursor.getInt(cursor.getColumnIndex(SQLiteHelper.QUANTITY)),
                    cursor.getInt(cursor.getColumnIndex(SQLiteHelper.TYPE))
            );
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        incrementProgress();
    }

    public void sendImage(String path, int quantity, int type) throws FileNotFoundException {

        File image = new File(path);

        RequestParams params = new RequestParams();
        params.put("order", prefs.getString("orderId", ""));
        params.put("image", image);
        params.put("size", serverDatabaseSizeNames[type]);
        params.put("quantity", String.valueOf(quantity));

        PostalgiaRestClient.post("pictures/", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String s) {
                Log.d("Postal", "Image transfer success: " + s);
                if (cursor.moveToNext()) sendNext();
                else uploadFinish();
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                Log.d("Postal", "Image transfer failure: " + s);
                uploadError();

            }
        });


    }

    public void incrementProgress () {
        int currentVal = progressDialog.getProgress();
        int newVal = currentVal + 1;
        Log.d("Postal", "Progress Bar value:" + newVal);
        progressDialog.setProgress(newVal);
    }

    private void uploadFinish() {
        database.close();
        progressDialog.dismiss();
        text.setText(finishedText);
        button.setEnabled(true);
        button.setVisibility(View.VISIBLE);
        //listener.switchToWelcome();
    }


    private void uploadError () {
        progressDialog.dismiss();
        showUploadErrorDialog();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UPLOAD_ERROR_DIALOG:
                sendImages(prevSession);

        }
    }

    public void showUploadErrorDialog () {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        UploadErrorDialog dialog = new UploadErrorDialog();
        dialog.setTargetFragment(this, UPLOAD_ERROR_DIALOG);
        dialog.show(fm, "");

    }




}