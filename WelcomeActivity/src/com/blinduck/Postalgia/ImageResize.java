package com.blinduck.Postalgia;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/23/13
 * Time: 3:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageResize extends Activity {

    ImageView imageView;
    TextView textView;
    Cursor cursor;

    String bucket = "1";
    String searchParams = MediaStore.Images.Media._ID +  " = \"" + bucket + "\"";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_resize);

        Log.e("SearchParams", searchParams);

        imageView = (ImageView) findViewById(R.id.resize_imageview);
        textView = (TextView) findViewById(R.id.resize_textview);


        String [] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA
        };

        cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, "", null, "" );

        Log.e("Cursor", "" + cursor.getCount());

        if (cursor.moveToFirst()) {
            String path =cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            String imageWidth = "" + options.outWidth;
            String imageHeight = "" + options.outHeight;

            imageView.setImageBitmap(bitmap);
            String text = String.format("Path: %s , Width: %s, Height: %s",
                    path,imageWidth, imageHeight );

            textView.setText(text);

        }

    }
}