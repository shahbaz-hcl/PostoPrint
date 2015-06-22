package com.blinduck.Postalgia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/28/13
 * Time: 7:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageObject {
    private Bitmap imageBitmap;
    private int sampleSize = 1;
    boolean again = true;

    public ImageObject (String imagePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;  //set up options

        do {
            try {
                Log.d("ImageResults", "sample size: " + sampleSize);
                imageBitmap = BitmapFactory.decodeFile(imagePath, options);
                again = false;
            } catch (OutOfMemoryError e) {
                sampleSize *= 2;
                options.inSampleSize = sampleSize;
                Log.d("ImageResults", "sample size: " + sampleSize);
            }

        } while (again);
    }

    public int getSampleSize () {
        return sampleSize;
    }

    public Bitmap getImageBitmap () {
        return imageBitmap;
    }


}
