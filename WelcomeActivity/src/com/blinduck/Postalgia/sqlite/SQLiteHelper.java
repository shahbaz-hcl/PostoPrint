package com.blinduck.Postalgia.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 6/22/13
 * Time: 8:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String IMAGE_TABLE = "images_table";

    public static final String _ID = "_id";
    public static final String ORIGINAL_IMAGE_LOC = "orig_image_location";
    public static final String NEW_IMAGE_LOC = "new_image_loc";
    public static final String IMAGE_ID = "image_id";
    public static final String SESSION = "session";
    public static final String QUANTITY = "quantity";
    public static final String UPLOAD_STATUS = "upload_status";
    public static final String TYPE = "image_type";
    public static final String EDITTED_STATUS = "edit_status";
    public static final String ORIENTATION = "orientation";

    private static final String DATABASE_NAME = "postalgia_database";
    private static final int DATABASE_VERSION = 1;


    private static final String DATABASE_CREATE =   String.format(
            "create table %s " +
                    "(%s integer primary key autoincrement, " +
                    "%s integer not null, " +
                    "%s text not null," +
                    "%s text not null, " +
                    "%s integer not null, " +
                    "%s integer not null, " +
                    "%s integer not null, " +
                    "%s integer not null, " +
                    "%s integer not null, " +
                    "%s integer not null);",
            IMAGE_TABLE,

            _ID, SESSION,
            ORIGINAL_IMAGE_LOC, NEW_IMAGE_LOC ,
            IMAGE_ID, QUANTITY,TYPE, ORIENTATION,
            EDITTED_STATUS, UPLOAD_STATUS);

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        //To change body of  implemented methods use File | Settings | File Templates.
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + IMAGE_TABLE);
        onCreate(db);
    }
}

