package com.blinduck.Postalgia;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;
import com.blinduck.Postalgia.sqlite.SQLiteHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/26/13
 * Time: 7:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class SaveImage {

    private Context mContext;
    private SQLiteDatabase database;

    public SaveImage(Context context, SQLiteDatabase database ) {
        mContext = context;
        this.database = database;
    }




    public void storeImage(Bitmap image, int database_id, int order_number) {


        File pictureFile = getOutputMediaFile(database_id, order_number);
        if (pictureFile == null) {
            Log.d("FileSave", "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 50, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("FileSave", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("FileSave", "Error accessing file: " + e.getMessage());
        }
    }


    public  File getOutputMediaFile(int database_id, int orderNumber){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + mContext.getPackageName()
                + "/order_" + orderNumber);

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        String mImageName="order_"+ orderNumber + "_database_id_" + database_id +".jpg";
        String fullPath = mediaStorageDir.getAbsolutePath() + "/" + mImageName;
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.NEW_IMAGE_LOC, fullPath);
        values.put(SQLiteHelper.EDITTED_STATUS, 1);

        database.update(SQLiteHelper.IMAGE_TABLE, values, SQLiteHelper._ID + " = ?",
                new String[] {String.valueOf(database_id)});


        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public Bitmap drawOnBackground (Bitmap background, Bitmap imageBitmap,
                                  Rect sampledImageCoords, Rect canvasImageCoords) {
        Canvas canvas = new Canvas(background);
        canvas.drawColor(0xffffffff);
        canvas.drawBitmap(imageBitmap, sampledImageCoords, canvasImageCoords, null);
        Log.d("ImageResults", String.format("imageCoords: %s, canvasImageCoords: %s", sampledImageCoords.toShortString(), canvasImageCoords.toShortString()));
        return background;
    }



    public Rect getSampledCoordinates (Rect imageCoords, int inSampleSize) {
        Rect sampledImageCoords = new Rect();

        sampledImageCoords.left = imageCoords.left/inSampleSize;
        sampledImageCoords.right = imageCoords.right/inSampleSize;
        sampledImageCoords.top = imageCoords.top/inSampleSize;
        sampledImageCoords.bottom = imageCoords.bottom/inSampleSize;

        return sampledImageCoords;

    }




}
