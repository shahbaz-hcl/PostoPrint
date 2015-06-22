package com.blinduck.Postalgia;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/28/13
 * Time: 7:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class BackgroundObject {
    private Bitmap background;

    //produce a bitmap of required

    public BackgroundObject (int outputWidth, int outputHeight) {
        try {
            background = Bitmap.createBitmap((int)outputWidth, (int) outputHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError e) {
            Log.d("ImageResults", "1st failure creating background");
            System.gc();
            try {
                background = Bitmap.createBitmap((int)outputWidth, (int) outputHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError e2) {
                System.gc();
                Log.d("ImageResults", "2nd failure creating background, changing bitmap config to RGB 565");
                background = Bitmap.createBitmap((int)outputWidth, (int) outputHeight, Bitmap.Config.RGB_565);
            }
        }


    }

    public Bitmap getBackground () {
        return background;
    }


}
