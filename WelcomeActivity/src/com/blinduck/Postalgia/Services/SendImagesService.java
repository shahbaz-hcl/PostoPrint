package com.blinduck.Postalgia.Services;

import android.app.*;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;
import com.blinduck.Postalgia.PostalgiaRestClient;
import com.blinduck.Postalgia.R;
import com.blinduck.Postalgia.ServerUpload;
import com.blinduck.Postalgia.WelcomeActivity;
import com.blinduck.Postalgia.sqlite.ImageDataSource;
import com.blinduck.Postalgia.sqlite.SQLiteHelper;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/11/13
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class SendImagesService extends IntentService {
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */

    private SQLiteDatabase database;
    private ImageDataSource dataSource;
    private SharedPreferences prefs;

    private String [] serverDatabaseSizeNames  = {"k4R", "kWallet", "kSquare"};


    public SendImagesService() {
        super("CreateSendImageService");
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int prevSession = intent.getIntExtra("prevSession", 0);

        prefs = getSharedPreferences(WelcomeActivity.PREFS_NAME, MODE_PRIVATE);

        dataSource = new ImageDataSource(getApplicationContext());
        database = dataSource.getDatabase();

        //get images from previous session that have not been uploaded yet
        Cursor cursor = database.query(SQLiteHelper.IMAGE_TABLE, new String[] {SQLiteHelper._ID, SQLiteHelper.NEW_IMAGE_LOC, SQLiteHelper.QUANTITY, SQLiteHelper.TYPE},
                SQLiteHelper.SESSION + "=? AND " + SQLiteHelper.UPLOAD_STATUS + "=?" , new String[] {String.valueOf(prevSession), String.valueOf(0)},
                null,null, null);

        if (cursor.moveToFirst()) {
            do {
                Log.d("Postal", "Image upload total: " + cursor.getCount());
                try {
                    sendImage(
                            cursor.getString(cursor.getColumnIndex(SQLiteHelper.NEW_IMAGE_LOC)),
                            cursor.getInt(cursor.getColumnIndex(SQLiteHelper.QUANTITY)),
                            cursor.getInt(cursor.getColumnIndex(SQLiteHelper.TYPE))
                    );
                } catch (FileNotFoundException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            } while (cursor.moveToNext());
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(ServerUpload.SERVER_UPLOAD_COMPLETE, true);
        editor.commit();
        createNotification();

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
            }

            @Override
            public void onFailure(Throwable throwable, String s) {
                Log.d("Postal", "Image transfer failure: " + s);
            }
        });


    }


    private void createNotification() {
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        Notification noti = new Notification(R.drawable.ic_launcher, "Image Upload Complete!", System.currentTimeMillis());
        PendingIntent intent = PendingIntent.getActivity(this, 0, new Intent(this, WelcomeActivity.class), 0);
        noti.setLatestEventInfo(this, "Postalgia Prints", "All images have been uploaded!", intent);
        noti.flags = Notification.FLAG_AUTO_CANCEL;



        notificationManager.notify(0, noti);

    }




}
