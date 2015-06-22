package com.blinduck.Postalgia.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/23/13
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageDataSource {
    private SQLiteDatabase database;
    private SQLiteHelper dbhelper;
    public static final String[] allColumns = {
            SQLiteHelper._ID,
            SQLiteHelper.SESSION,
            SQLiteHelper.ORIGINAL_IMAGE_LOC,
            SQLiteHelper.NEW_IMAGE_LOC,
            SQLiteHelper.IMAGE_ID,
            SQLiteHelper.QUANTITY,
            SQLiteHelper.TYPE,
            SQLiteHelper.ORIENTATION,
            SQLiteHelper.EDITTED_STATUS,
            SQLiteHelper.UPLOAD_STATUS};

    public ImageDataSource (Context context) {
        dbhelper = new SQLiteHelper(context);
    }

    public void open () throws SQLException {
        database = dbhelper.getWritableDatabase();
    }

    public SQLiteDatabase getDatabase () {
        return dbhelper.getWritableDatabase();
    }


    public void close() {
        dbhelper.close();
    }

    public long createImage(String original_loc, int image_id, int session, int media_orientation) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(original_loc, options);
        int orientation = options.outHeight > options.outWidth ? 1 : 0;
        //1 is for portrait, 0 is for height

        /*int orientation = -1;

        switch (media_orientation) {
            case 0:

                break;
            case 180:
                orientation =  0;
                break;
            case 270:
            case 90:
                orientation = 1;
                break;
        }*/

        /*
        try {
            ExifInterface exif = new ExifInterface(original_loc);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.d("Postal", "Exif Orientation: " + exifOrientation);
        } catch (IOException e) {
            Log.d("Postal", "Exif error: " + e);
        }*/



        Log.d("Postal", String.format("Height:%s, Width:%s", options.outHeight, options.outWidth));
        Log.d("Postal", "Orientation based on height and width: " + orientation);
        Log.d("Postal", "Orientation from media store:" + media_orientation);

        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.IMAGE_ID, image_id);
        values.put(SQLiteHelper.ORIGINAL_IMAGE_LOC, original_loc);
        values.put(SQLiteHelper.NEW_IMAGE_LOC, "");
        values.put(SQLiteHelper.SESSION, session);
        values.put(SQLiteHelper.QUANTITY, 1);
        values.put(SQLiteHelper.EDITTED_STATUS,0);
        values.put(SQLiteHelper.UPLOAD_STATUS, 0);
        values.put(SQLiteHelper.TYPE , 0);
        values.put(SQLiteHelper.ORIENTATION, orientation);
        long insertId = database.insert(SQLiteHelper.IMAGE_TABLE, null, values);

        return insertId;
    }


    public void deleteImage (int _id) {

        database.delete(SQLiteHelper.IMAGE_TABLE,
                SQLiteHelper._ID + " = " + _id,
                null);
    }





    public void deleteAll () {
        database.delete(SQLiteHelper.IMAGE_TABLE, null, null);
    }



    public List<Image> getAllImages() {
        List <Image> images = new ArrayList<Image>();

        //Todo Implement session in shared preferences

        Cursor cursor = database.query(
                SQLiteHelper.IMAGE_TABLE,
                allColumns,
                SQLiteHelper.SESSION + "= 1", null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Image image = CursorToImage(cursor);
                images.add(image);

            } while (cursor.moveToNext());
        }
        cursor.close();

        return images;
    }

    private Image CursorToImage(Cursor cursor) {
        Image image = new Image();
        image.set_id(cursor.getInt(cursor.getColumnIndex(SQLiteHelper._ID)));
        image.setSession(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.SESSION)));
        image.setOrig_image_loc(cursor.getString(cursor.getColumnIndex(SQLiteHelper.ORIGINAL_IMAGE_LOC)));
        image.setNew_image_loc(cursor.getString(cursor.getColumnIndex(SQLiteHelper.NEW_IMAGE_LOC)));
        image.setImage_id(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.IMAGE_ID)));
        image.setQuantity(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.QUANTITY)));
        image.setEdit_status(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.EDITTED_STATUS)));
        image.setUpload_status(cursor.getInt(cursor.getColumnIndex(SQLiteHelper.UPLOAD_STATUS)));
        image.setOrientation((cursor.getInt(cursor.getColumnIndex(SQLiteHelper.ORIENTATION))));
        return image;
    }


}
