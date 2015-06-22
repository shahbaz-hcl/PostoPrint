package com.blinduck.Postalgia.Services;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.blinduck.Postalgia.*;
import com.blinduck.Postalgia.sqlite.ImageDataSource;
import com.blinduck.Postalgia.sqlite.SQLiteHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/9/13
 * Time: 8:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class CreateScaledImagesService extends IntentService {

    private final IBinder mBinder = new LocalBinder();
    private ImageDataSource dataSource;
    private SQLiteDatabase database;
    public static final String BROADCAST ="broadcast";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * Name used to name the worker thread, important only for debugging.
     */
    public CreateScaledImagesService() {
        super("CreateScaledImagesService");
    }

    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    public void onDestroy() {
        if (database.isOpen()) database.close();
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /*Creates scaled images, and updates database editted status to be 1  */
    @Override
    protected void onHandleIntent(Intent intent) {
        int session = intent.getIntExtra(WelcomeActivity.SESSION, 0);
        Log.d("Postal", "Service: " + session);


        dataSource = new ImageDataSource(getApplicationContext());
        database = dataSource.getDatabase();

        Cursor cursor = database.query(SQLiteHelper.IMAGE_TABLE,
                new String[] {SQLiteHelper._ID, SQLiteHelper.ORIGINAL_IMAGE_LOC, SQLiteHelper.TYPE, SQLiteHelper.ORIENTATION},
                SQLiteHelper.EDITTED_STATUS + "=?  AND " + SQLiteHelper.SESSION + "=?",
                new String[]{String.valueOf(0), String.valueOf(session)},
                null, null, null);



        if (cursor.moveToFirst()) {
            int imageCount = cursor.getCount();
            int currentImage = 1;
            Log.d("Postal", "imageCount: " + imageCount);


            do {
                String location =  cursor.getString(cursor.getColumnIndex(SQLiteHelper.ORIGINAL_IMAGE_LOC));
                int type = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.TYPE));
                int orientation = cursor.getInt(cursor.getColumnIndex(SQLiteHelper.ORIENTATION));
                int database_id = cursor.getInt(cursor.getColumnIndex(SQLiteHelper._ID));

                createScaledImage(location, type, orientation, session, database_id);
                Log.d("Postal", "Finished id: " + database_id);
                Intent intent2 = new Intent(BROADCAST);
                intent2.putExtra("totalImages", imageCount);
                intent2.putExtra("currentImage", currentImage);
                intent2.putExtra("complete", false);


                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent2);
                currentImage +=1;
            }while (cursor.moveToNext()) ;

        }

        SharedPreferences prefs = getSharedPreferences(WelcomeActivity.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(OrderSummary.SCALED_IMAGE_SERVICE_COMPLETE, true);
        editor.commit();

        Intent completeIntent =  new Intent(BROADCAST);
        completeIntent.putExtra("complete", true);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(completeIntent);



    }

    public class LocalBinder extends Binder {
        CreateScaledImagesService getService () {
            return CreateScaledImagesService.this;
        }

    }





    //TODO THIS METHOD NEEDS TO BE FIXED TO BE CONSISTENT

    public void createScaledImage(String location, int type, int orientation, int session, int database_id) {

        // find portrait or landscape image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(location, options);
        final int imageHeight = options.outHeight;    //find the width and height of the original image.
        final int imageWidth = options.outWidth;

        int outputHeight = 0;
        int outputWidth = 0;


        //set the output size depending on type of image
        switch (type) {
            case EdittingGridFragment.SIZE4R :
                if (orientation == EdittingGridFragment.LANDSCAPE) {
                    outputWidth = 1800;
                    outputHeight = 1200;
                } else if (orientation == EdittingGridFragment.PORTRAIT) {
                    outputWidth = 1200;
                    outputHeight = 1800;
                }
                break;
            case EdittingGridFragment.SIZEWALLET:
                if (orientation == EdittingGridFragment.LANDSCAPE) {
                    outputWidth = 953;
                    outputHeight = 578;
                } else if (orientation ==EdittingGridFragment.PORTRAIT ) {
                    outputWidth = 578;
                    outputHeight = 953;
                }

                break;
            case EdittingGridFragment.SIZESQUARE:
                outputWidth = 840;
                outputHeight = 840;
                break;
        }

        assert outputHeight != 0  && outputWidth != 0;


        //fit  images
        //FitRectangles rectangles = new FitRectangles((int) outputWidth, (int) outputHeight, imageWidth, imageHeight);

        //scaled images
        ScaledRectangles rectangles = new ScaledRectangles((int) outputWidth, (int) outputHeight, imageWidth, imageHeight);

        Rect canvasSize = rectangles.getCanvasSize();
        Rect canvasImageCoords = rectangles.getCanvasImageCoords();
        Rect imageCoords = rectangles.getImageCoords();



        /*
        //set the canvas size based on the type of image
        Rect canvasSize = new Rect(0, 0, (int) outputWidth, (int) outputHeight);
        Rect canvasImageCoords = new Rect();
        //Rect canvasImageCoords = new Rect (0, 0, outputWidth, outputHeight);  //set to use the entire canvas
        Rect imageCoords = new Rect(0, 0, imageWidth, imageHeight);
        //Rect imageCoords = new Rect();


        // 3 cases, exactfit, canvas width larger, canvas height larger
        if ((float) outputHeight/outputWidth ==  (float) imageHeight/imageWidth) {
            canvasImageCoords.set(canvasSize);
            //imageCoords.set(0, 0, imageWidth, imageHeight); //map the entire image to the entire canvas
            Log.d("Async", "Proportionas Equal");

        }



        else if ( (float) outputHeight/outputWidth >  (float) imageHeight/imageWidth) {
            //blank space above and below image
            //find vdiff


            //code that fits the image without whitespace
            Log.d("Async", "blank space above and below");

            float scaleFactor = (float)imageHeight / (float)  outputHeight; //amount to scale the canvas by to match the height of the image.
            int scaledCanvasWidth = (int) (outputWidth * scaleFactor);
            int hDiff = (imageWidth - scaledCanvasWidth)/2;
            imageCoords.set (hDiff, 0 , imageWidth - hDiff, imageHeight);



            //code fits image with whitespace
            float scaleFactor = (float) outputWidth / (float) imageWidth;
            int scaledImageHeight = (int) (imageHeight * scaleFactor);
            assert scaledImageHeight < outputHeight;

            int vDiff = (outputHeight - scaledImageHeight)/2;
            canvasImageCoords.set(0, vDiff, outputWidth, outputHeight - vDiff);



        } else if ((float) outputHeight/outputWidth < (float) imageHeight/imageWidth) {
            //blank space to left and right of image


            //fits the image without whitespace
            float scaleFactor = (float) imageWidth / (float) outputWidth;
            int scaledCanvasHeight = (int) (outputHeight * scaleFactor);
            int vDiff = (imageHeight - scaledCanvasHeight)/2;
            imageCoords.set(0, vDiff, imageWidth, imageHeight - vDiff);

            //fits image with whitespace

            Log.d("Async", "blank space left and right");
            float scaleFactor = (float) outputHeight / (float) imageHeight;
            int scaledImageWidth = (int) (imageWidth * scaleFactor);
            assert scaledImageWidth < outputWidth;

            int hDiff = (outputWidth - scaledImageWidth)/2;

            canvasImageCoords.set(hDiff, 0, outputWidth - hDiff, outputHeight);
        }

        */

        Log.d("Async", "Canvas Image Coords:" + canvasImageCoords.toShortString());

        SaveImage imageSaver = new SaveImage(getApplicationContext(), database);
        ImageObject imageObject = new ImageObject(location);
        Bitmap imageBitmap = imageObject.getImageBitmap();
        int sampleSize = imageObject.getSampleSize();

        Rect sampledImageCoords = imageSaver.getSampledCoordinates(imageCoords, sampleSize);

        System.gc();
        BackgroundObject backgroundObject = new BackgroundObject(outputWidth, outputHeight);
        Bitmap background = backgroundObject.getBackground();


        background = imageSaver.drawOnBackground(background, imageBitmap,
                sampledImageCoords, canvasImageCoords);

        imageSaver.storeImage(background, database_id, session);
        background.recycle();

    }

    public void storeImage(Bitmap image, int session, int database_id) {
        File pictureFile = getOutputMediaFile(session, database_id);
        if (pictureFile == null) {
            Log.d("FileSave","Error creating media file, check storage permissions:");// e.getMessage());
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

    public  File getOutputMediaFile(int session, int database_id){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/order_" + session);

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
        String mImageName="order_"+ session + "_database_id_" + database_id +".jpg";
        String fullPath = mediaStorageDir.getAbsolutePath() + "/" + mImageName;
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.NEW_IMAGE_LOC, fullPath);
        values.put(SQLiteHelper.EDITTED_STATUS, 1);

        database.update(SQLiteHelper.IMAGE_TABLE, values, SQLiteHelper._ID + " = ?",
                new String[] {String.valueOf(database_id)});



        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    /*
    * Generates the rectangles for scaling the images to fit with no whitespace
    * */
    class ScaledRectangles {

        Rect canvasSize = new Rect ();
        Rect canvasImageCoords = new Rect();
        Rect imageCoords = new Rect();

        public ScaledRectangles (int outputWidth, int outputHeight, int imageWidth, int imageHeight) {
            canvasSize.set(0, 0, outputWidth, outputHeight);   //canvas size always the same
            canvasImageCoords.set (0, 0, outputWidth, outputHeight);  //set to use the entire canvas


            // 3 cases, exactfit, canvas width larger, canvas height larger
            if ((float) outputHeight/outputWidth ==  (float) imageHeight/imageWidth) {
                imageCoords.set(0, 0, imageWidth, imageHeight); //map the entire image to the entire canvas
                Log.d("Async", "Proportionas Equal");
            }

            else if ( (float) outputHeight/outputWidth >  (float) imageHeight/imageWidth) {
                //code that fits the image without whitespace
                Log.d("Async", "blank space above and below");

                float scaleFactor = (float)imageHeight / (float)  outputHeight; //amount to scale the canvas by to match the height of the image.
                int scaledCanvasWidth = (int) (outputWidth * scaleFactor);
                int hDiff = (imageWidth - scaledCanvasWidth)/2;
                imageCoords.set (hDiff, 0 , imageWidth - hDiff, imageHeight);
            }

            else if ((float) outputHeight/outputWidth < (float) imageHeight/imageWidth) {

                //fits the image without whitespace
                float scaleFactor = (float) imageWidth / (float) outputWidth;
                int scaledCanvasHeight = (int) (outputHeight * scaleFactor);
                int vDiff = (imageHeight - scaledCanvasHeight)/2;
                imageCoords.set(0, vDiff, imageWidth, imageHeight - vDiff);
            }

        }

        Rect getCanvasSize() {
            return canvasSize;
        }

        Rect getCanvasImageCoords() {
            return canvasImageCoords;
        }

        Rect getImageCoords() {
            return imageCoords;
        }

    }



    /*
    * Generates the rectangles for fitting images in the canvas with whitespace
    * */
    class FitRectangles {
        Rect canvasSize = new Rect ();
        Rect canvasImageCoords = new Rect();
        Rect imageCoords = new Rect();

        public FitRectangles (int outputWidth, int outputHeight, int imageWidth, int imageHeight) {
            canvasSize.set(0, 0, outputWidth, outputHeight);   //canvas size always the same
            imageCoords.set(0, 0, imageWidth, imageHeight);    //use the entire image for this

            if ((float) outputHeight/outputWidth ==  (float) imageHeight/imageWidth) {
                canvasImageCoords.set(canvasSize);
                Log.d("Async", "Proportionas Equal");
            }

            else if ( (float) outputHeight/outputWidth >  (float) imageHeight/imageWidth) {
             //blank space above and below image
             //find vdiff

                float scaleFactor = (float) outputWidth / (float) imageWidth;
                int scaledImageHeight = (int) (imageHeight * scaleFactor);
                assert scaledImageHeight < outputHeight;

                int vDiff = (outputHeight - scaledImageHeight)/2;
                canvasImageCoords.set(0, vDiff, outputWidth, outputHeight - vDiff);



            } else if ((float) outputHeight/outputWidth < (float) imageHeight/imageWidth) {
                //blank space to left and right of image

                Log.d("Async", "blank space left and right");
                float scaleFactor = (float) outputHeight / (float) imageHeight;
                int scaledImageWidth = (int) (imageWidth * scaleFactor);
                assert scaledImageWidth < outputWidth;

                int hDiff = (outputWidth - scaledImageWidth)/2;

                canvasImageCoords.set(hDiff, 0, outputWidth - hDiff, outputHeight);
            }

        }

        Rect getCanvasSize() {
            return canvasSize;
        }

        Rect getCanvasImageCoords() {
            return canvasImageCoords;
        }

        Rect getImageCoords() {
            return imageCoords;
        }
    }








}
