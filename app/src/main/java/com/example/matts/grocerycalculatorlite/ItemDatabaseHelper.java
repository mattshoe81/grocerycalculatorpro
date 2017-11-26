package com.example.matts.grocerycalculatorlite;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;

import static java.sql.Types.REAL;

/**
 * Created by matts on 9/16/2017.
 */

public class ItemDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "Items";
    private static final int DB_VERSION = 1;
    public static final String PRICE = "PRICE";
    public static final String PRICE_TABLE = "PRICES";
    public static final String SHOPPING_LIST_TABLE = "SHOPPINGLIST";
    public static final String DESCRIPTION_COLUMN = "DESCRIPTION";
    public static final String PARENT_LIST_COLUMN = "PARENTLIST";
    public static final String ID_COLUMN = "_id";

    /**
     * Constructor.
     *
     * @param context application context
     */
    ItemDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        updateDatabase(sqLiteDatabase, 0, DB_VERSION);
    }

    public long insertItem(SQLiteDatabase db, double price) {
        ContentValues itemValue = new ContentValues();
        itemValue.put(PRICE, price);
        long id = db.insert(PRICE_TABLE, null, itemValue);
        return id;
    }

    public boolean insertRow(SQLiteDatabase db, String table, ContentValues contentValues) {
        boolean successfulInsertion = true;
        if (db.insert(table, null, contentValues) < 0){
            successfulInsertion = false;
        }

        return successfulInsertion;
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE " +PRICE_TABLE +" ("
                    +ID_COLUMN +" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +PRICE +" REAL "
                    +");");

            db.execSQL("CREATE TABLE "+SHOPPING_LIST_TABLE +" ("
                    +ID_COLUMN +" INTEGER PRIMARY KEY AUTOINCREMENT, "
                    +DESCRIPTION_COLUMN +" TEXT "
                    +PARENT_LIST_COLUMN +" TEXT "
                    +");");
        }
        if (oldVersion < 2) {
            // Do updating and stuff
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        updateDatabase(sqLiteDatabase, oldVersion, newVersion);
    }
}
