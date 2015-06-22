package com.blinduck.Postalgia;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.*;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.blinduck.Postalgia.sqlite.ImageDataSource;
import com.blinduck.Postalgia.sqlite.SQLiteHelper;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import it.sephiroth.android.library.imagezoom.utils.DecodeUtils;

import java.io.*;


public class ImageProcessing extends SherlockActivity {

    private static final String LOG_TAG = "image-test";


    protected final float[] mMatrixValues = new float[9];




    private String imagePath;
    private Uri  imageUri;
    private int image_id;
    private int imageType;
    private int imageOrientation;
    private int database_id;


    private int orderNumber;

    private Rect canvasBaseSize;
    private Rect canvasImageCoords;
    private Rect imageCoords;



    private float origImageHeight;
    private float origImageWidth;
    private int viewHeight;
    private int viewWidth;
    private float outputHeight;
    private float outputWidth;
    private float offset;
    private float maxSupportMatrixScale;

    private float imageXCoord, imageYCoord;
    private float resultImageWidth, resultImageHeight;
    private SQLiteDatabase database;

    private int IMAGE_MAX_SIZE = 1000;

    private Bitmap imageBitmap;
    private Bitmap background;
    private Bitmap bitmap1;

    ImageViewTouch mImage;
    static int displayTypeCount = 0;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        com.actionbarsherlock.view.MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.image_processing_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);


        orderNumber =  getSharedPreferences(WelcomeActivity.PREFS_NAME, MODE_PRIVATE).getInt(WelcomeActivity.SESSION, 0);

        ImageDataSource dataSource = new ImageDataSource(this);
        database = dataSource.getDatabase();
        Log.e("", "Database Opened");


        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        image_id = bundle.getInt(ActionBarView.IMAGE_ID);
        imageType = bundle.getInt(ActionBarView.IMAGE_TYPE);
        imageOrientation = bundle.getInt(ActionBarView.IMAGE_ORIENTATION);
        database_id = bundle.getInt(ActionBarView.DATABASE_ID);

        //TODO image location can directly be passed in here, no need to requery database.



        Log.d("Edit", String.format ("Id: %s, imagetype %s , orientation: %s from image processing.", image_id, imageType, imageOrientation));

        setContentView( R.layout.image_processing_relative);
        setImage();
    }

    //this is called after the image has been set
    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mImage = (ImageViewTouch) findViewById( R.id.image );

        // set the default image display type
        mImage.setDisplayType( ImageViewTouchBase.DisplayType.FIT_TO_SCREEN );
    }

    //change image orientation in database and reload image
    public void imageProcessingRotate (View v) {
        ImageDataSource imageSource = new ImageDataSource(this);
        SQLiteDatabase database = imageSource.getDatabase();


        ContentValues values = new ContentValues();


        if (imageOrientation == EdittingGridFragment.PORTRAIT) imageOrientation = EdittingGridFragment.LANDSCAPE;
        else if (imageOrientation == EdittingGridFragment.LANDSCAPE) imageOrientation = EdittingGridFragment.PORTRAIT;

        values.put(SQLiteHelper.ORIENTATION, imageOrientation);


        int res = database.update(SQLiteHelper.IMAGE_TABLE, values, SQLiteHelper._ID + " = ?",
                new String[] {String.valueOf(database_id)});

        setImage();

    }


    public void printMatrices () {
        mImage.printBaseMatrix();
        mImage.printSuppMatrix();
    }


    //calculated coordinates of how the image maps to canvas.
    public void calculateCoordinates() {

        printMatrices();
        canvasBaseSize = new Rect();
        canvasBaseSize.set(0, 0, (int) outputWidth, (int) outputHeight);
        canvasImageCoords = new Rect();
        imageCoords = new Rect();
        Rect sampledImageCoords = new Rect();

        Matrix baseMatrix = mImage.getmBaseMatrix();
        Matrix supportMatrix = mImage.getmSuppMatrix();

        float bScale= Math.min(viewHeight/origImageHeight, viewWidth/origImageWidth);
        Log.d("ImageResults", "Calculated bScale:" + bScale);
        printMatrices();


        float baseMatrixX = getValue( baseMatrix, Matrix.MTRANS_X );
        float baseMatrixY = getValue( baseMatrix, Matrix.MTRANS_Y );

        float supportScale = getValue( supportMatrix, Matrix.MSCALE_X );
        float supportMatrixX = getValue( supportMatrix, Matrix.MTRANS_X );
        float supportMatrixY = getValue( supportMatrix, Matrix.MTRANS_Y );

        float zoomedHeight = origImageHeight * supportScale * bScale;
        float zoomedWidth = origImageWidth * supportScale * bScale;

        //Size of the full zoomed image
        Log.d("ImageResults", "Zoomed Size: " + zoomedWidth + "," + zoomedHeight);


        float windowX =  Math.abs(supportMatrixX);
        float windowY =  Math.abs(supportMatrixY);



        imageXCoord = windowX / zoomedWidth * origImageWidth;
        imageYCoord = windowY / zoomedHeight * origImageHeight;

        Log.d("ImageResults", "ImageCoordinates: " + imageXCoord +"," + imageYCoord);

        //Result image size is a theoretical size.
        resultImageWidth = viewWidth / zoomedWidth * origImageWidth;
        resultImageHeight = viewHeight /zoomedHeight * origImageHeight;

        Log.d("ImageResults", "Resultant Image Width: " + resultImageWidth +"," + resultImageHeight);

        //deal with the case of image larger then window.
        if(baseMatrixX == 0 && resultImageHeight < origImageHeight) {
            //calculate Y offset
            float offsetY = ( origImageWidth * viewHeight /viewWidth ) - origImageHeight;
            offsetY = offsetY/2;
            Log.d("ImageResults", "offset Y: " + offsetY);
            imageYCoord -= offsetY;

        } else if(baseMatrixY == 0 && resultImageWidth < origImageWidth) {
            //calculate x offset
            float offsetX = (origImageHeight * viewWidth / viewHeight) - origImageWidth;
            offsetX = offsetX/2;
            Log.d("ImageResults", "offset x: " + offsetX);
            imageXCoord -= offsetX;
        }

        Log.d("ImageResults", "Offset Image Coordinates: " + imageXCoord +"," + imageYCoord);


        if (resultImageWidth < origImageWidth && resultImageHeight < origImageHeight) {
            Log.d("ImageResults", "Case 1");
            //image scaled
            canvasImageCoords.set(canvasBaseSize);
            imageCoords.set((int) imageXCoord, (int) imageYCoord, (int) (imageXCoord + resultImageWidth), (int) (imageYCoord + resultImageHeight));


        }

        else if (resultImageWidth > origImageWidth && resultImageHeight <= origImageHeight) {
            Log.d("ImageResults", "Case 2");
            //result taller or equal, center horizontally
            int hOffset = (int) (outputWidth - ((outputWidth/viewWidth) * zoomedWidth))/2;
            canvasImageCoords.set(hOffset, 0, (int)outputWidth -hOffset, (int)outputHeight);

            int vDiff =(int)(origImageHeight - resultImageHeight)/2;
            assert  vDiff > 0;
            imageCoords.set(0, vDiff, (int) origImageWidth, (int)(origImageHeight - vDiff));
            /*
            int hDiff = (int) (resultImageWidth - origImageWidth)/2;
            float shrinkRatio = outputHeight / resultImageHeight;
            canvasImageCoords.set((int) (hDiff * shrinkRatio), 0, (int) ((resultImageWidth - hDiff) * shrinkRatio), (int) outputHeight);
            */


        }

        else if (resultImageWidth <= origImageWidth && resultImageHeight > origImageHeight) {
            Log.d("ImageResults", "Case 3");
            //result wider or equal, centre vertically
            int vOffset = (int) (outputHeight - ((outputHeight/viewHeight) * zoomedHeight))/2;
            canvasImageCoords.set(0, vOffset, (int)outputWidth, (int)outputHeight - vOffset);

            int hDiff = (int)(origImageWidth - resultImageWidth)/2;
            assert hDiff > 0;
            imageCoords.set(hDiff, 0, (int) origImageWidth - hDiff, (int) origImageHeight);

            /*
            int vDiff =  (int) (resultImageHeight - origImageHeight)/2;
            assert  vDiff > 0;
            float shrinkRatio = outputWidth/resultImageWidth;
            assert shrinkRatio < 1;
            canvasImageCoords.set(0, (int) (vDiff * shrinkRatio), (int) outputWidth, (int) ((resultImageHeight - vDiff) * shrinkRatio));
            */
        }

        else if (resultImageWidth >= origImageWidth && resultImageHeight >= origImageHeight) {
            Log.d("ImageResults", "Case 4 ");
            //result smaller or equal, center
            imageCoords.set(0, 0, (int) origImageWidth, (int)origImageHeight); //use the entire image
            int hOffset = (int) (outputWidth - ((outputWidth/viewWidth) * zoomedWidth))/2;
            int vOffset = (int) (outputHeight - ((outputHeight/viewHeight) * zoomedHeight))/2;

            canvasImageCoords.set (hOffset, vOffset,(int)outputWidth - hOffset , (int)outputHeight - vOffset);

            /*
            int hDiff =  (int) (resultImageWidth - origImageWidth)/2;
            int vDiff = (int) (resultImageHeight - origImageHeight)/2;

            Log.d("ImageResults", String.format("hDiff:  %s  vDiff: %s",  hDiff, vDiff));

            //shrink the theoretical result image to the required size
            float shrinkRatio =  outputWidth / resultImageWidth;
            assert shrinkRatio < 1;

            int hOffset =  (int) (hDiff * shrinkRatio);
            int vOffset =  (int) (vDiff * shrinkRatio);

            //canvasImageCoords.set (hOffset, vOffset,(int)outputWidth - hOffset , (int)outputHeight - vOffset);

            canvasImageCoords.set ((int) (hDiff * shrinkRatio), (int) (vDiff * shrinkRatio),
                    (int) ( (resultImageWidth - hDiff) * shrinkRatio), (int) ( (resultImageHeight - vDiff) * shrinkRatio) );*/
        }

        int imageWidth = imageCoords.right - imageCoords.left;
        int imageHeight = imageCoords.top - imageCoords.bottom;

        int canvasImageWidth = canvasImageCoords.right - canvasImageCoords.left;
        int canvasImageHeight = canvasImageCoords.top - canvasImageCoords.bottom;

        Log.d("ImageResults", "canvasBaseSize: " + canvasBaseSize.toShortString());
        Log.d("ImageResults", "Canvas Image Coords: " + canvasImageCoords.toShortString());
        Log.d("ImageResults", "ImageCoords: " + imageCoords.toShortString());
        Log.d("ImageResults", "Image Dimensions: " + imageWidth + "," + imageHeight );
        Log.d("ImageResults", "Canvas image dimensions: " + canvasImageWidth + "," + canvasImageHeight);

        SaveImage imageSaver = new SaveImage(getApplicationContext(), database);

        //get the image and the sample size
        ImageObject imageObject = new ImageObject(imagePath);
        imageBitmap = imageObject.getImageBitmap();
        int sampleSize = imageObject.getSampleSize();


        sampledImageCoords = imageSaver.getSampledCoordinates(imageCoords, sampleSize);

        Log.d("ImageResults", "sampled image coords : " + sampledImageCoords.toShortString());
        Log.d("Image results: ", String.format("Size of imageBitmap: %s, %s ", imageBitmap.getWidth(), imageBitmap.getHeight()));

        System.gc();

        BackgroundObject backgroundObject = new BackgroundObject((int)outputWidth, (int)outputHeight);
        background = backgroundObject.getBackground();



        Log.d("ImageResults", "Output image width and height: " + outputWidth + "," + outputHeight);

        /*
        Canvas  canvas = new Canvas(background);
        canvas.drawColor(0xffffffff);
        canvas.drawBitmap(imageBitmap, sampledImageCoords, canvasImageCoords, null);
        Log.d("ImageResults", String.format("imageCoords: %s, canvasImageCoords: %s", sampledImageCoords.toShortString(), canvasImageCoords.toShortString()));
        */

        // draw onto the background object
        background = imageSaver.drawOnBackground(background, imageBitmap,
                sampledImageCoords, canvasImageCoords);

        //Todo Recycle bitmaps

        imageSaver.storeImage(background, database_id, orderNumber);

    }


    //deprecated
    private Bitmap RegionDecode (File f, Rect R) throws IOException {
        FileInputStream fis = new FileInputStream(f);

        BitmapFactory.Options options = new BitmapFactory.Options();

        options.inSampleSize = 4;
        BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(fis, false);

        Bitmap region = decoder.decodeRegion(R, options);

        return region;

    }
    //deprecated
    public void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("FileSave","Error creating media file, check storage permissions: ");// e.getMessage());
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

    //deprecated
    public  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
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

        database.close();

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    //deprecated
    public static int calculateInSampleSize(
            int maxSize, int imageWidth, int imageHeight) {
        // Raw height and width of image
        int inSampleSize = 1;

        if (imageHeight > maxSize || imageWidth > maxSize) {
            int verticalScale = (int) Math.ceil((float) imageHeight/maxSize);
            int horizontalScale = (int) Math.ceil((float)imageWidth/maxSize);


            inSampleSize = verticalScale > horizontalScale ? verticalScale : horizontalScale;

        }

        return inSampleSize;
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig ) {
        super.onConfigurationChanged( newConfig );
    }


    protected float getValue( Matrix matrix, int whichValue ) {
        matrix.getValues( mMatrixValues );
        return mMatrixValues[whichValue];

    }

    Matrix imageMatrix;

    //loads original images
    private void setImage() {
        Cursor c = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                MediaStore.Images.Media._ID + "=?",
                new String[] {String.valueOf(image_id)}, null);

        if (c.moveToFirst()) {
            final long id = c.getLong(c.getColumnIndex(MediaStore.Images.Media._ID));
            imagePath = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.d("ImageResults", "Image path: " + imagePath);
            imageUri = Uri.parse(MediaStore.Images.Media.EXTERNAL_CONTENT_URI + "/" + id);

            maxSupportMatrixScale = getMaxScale();


            final int size = -1; // use the original image size
            bitmap1 = DecodeUtils.decode(this, imageUri, size, size);
            if( bitmap1 !=  null )
            {
                imageMatrix = new Matrix();

                //mImage.setImageBitmap( bitmap, imageMatrix.isIdentity() ? null : imageMatrix, ImageViewTouchBase.ZOOM_INVALID, ImageViewTouchBase.ZOOM_INVALID );
                mImage.setImageBitmap( bitmap1, imageMatrix.isIdentity() ? null : imageMatrix, 0.8f, maxSupportMatrixScale );
                if (maxSupportMatrixScale <= 1) mImage.setScaleEnabled(false);

            } else {
                Toast.makeText( this, "Image Failed to Load", Toast.LENGTH_LONG ).show();
            }
        }
    }

    private float getMaxScale ()  {
        Resources r = getResources();

        switch (imageType) {
            case EdittingGridFragment.SIZE4R :
                if (imageOrientation == EdittingGridFragment.LANDSCAPE) {
                    viewWidth = (int) convertToPixels(300);
                    viewHeight = (int) convertToPixels(200);
                    outputWidth = 1800f;
                    outputHeight = 1200f;
                } else if (imageOrientation == EdittingGridFragment.PORTRAIT) {
                    viewWidth = (int) convertToPixels(200);
                    viewHeight = (int) convertToPixels(300);
                    outputWidth = 1200f;
                    outputHeight = 1800f;
                }
                break;
            case EdittingGridFragment.SIZEWALLET:
                if (imageOrientation == EdittingGridFragment.LANDSCAPE) {
                    viewWidth = (int) convertToPixels(300);
                    viewHeight = (int) convertToPixels(182);
                    outputWidth = 953f;
                    outputHeight = 578f;
                } else if (imageOrientation ==EdittingGridFragment.PORTRAIT ) {
                    viewWidth = (int) convertToPixels(200);
                    viewHeight = (int) convertToPixels(330);
                    outputWidth = 578f;
                    outputHeight = 953f;
                }

                break;
            case EdittingGridFragment.SIZESQUARE:
                viewWidth = (int) convertToPixels(300);
                viewHeight = (int) convertToPixels(300);
                outputWidth = 840f;
                outputHeight = 840f;
                break;
        }

        //set the size of the image view window
        mImage.getLayoutParams().width = viewWidth;
        mImage.getLayoutParams().height = viewHeight;
        Log.d("ImageResults", "view height: " + viewHeight +  " view width: " + viewWidth);

        //Get the original image's dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        origImageHeight = (float) options.outHeight;
        origImageWidth = (float)options.outWidth;

        float maxScale;

        Log.d ("ImageResults", "Orig image height: " + origImageHeight + " Original Image Width: " + origImageWidth);
        /*
        float minViewPixelDensity;
        float maxScale;

        float verticalPixelDensity =  (float) outputHeight / (float) viewHeight;
        float horizontalPixelDensity = (float) outputWidth/ (float) viewWidth;


        if (verticalPixelDensity>=horizontalPixelDensity) {
            minViewPixelDensity = verticalPixelDensity;
        } else {
            minViewPixelDensity =horizontalPixelDensity;
        }

        Log.d("Scale", "min pixel density:" + minViewPixelDensity);
        float maxVScale = origImageHeight/minViewPixelDensity/viewHeight;
        float maxHScale = origImageWidth/minViewPixelDensity/viewWidth;
        */

        float maxVScale = (float) origImageHeight / (float)outputHeight;
        float maxHScale = (float) origImageWidth / (float)outputWidth;


        if (maxVScale <= maxHScale) {
            maxScale = maxVScale;
        } else {
            maxScale = maxHScale;
        }




        return maxScale;

    }
    public float convertToPixels (int dp) {
        Resources r = getResources();
        return  TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.image_processing_back) {

            this.finish();

        } else if (item.getItemId() == R.id.image_processing_done) {
            //Toast.makeText(this, "Done Clicked", Toast.LENGTH_SHORT).show();
            calculateCoordinates();
            this.finish();
        }

        return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void onStop() {
        database.close();
        super.onStop();    //To change body of overridden methods use File | Settings | File Templates.
    }






}

