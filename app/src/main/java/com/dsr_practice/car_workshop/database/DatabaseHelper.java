package com.dsr_practice.car_workshop.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final int    DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "car_workshop.db";

    private static final String TYPE_TEXT = " TEXT";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String PRIMARY_KEY = " INTEGER PRIMARY KEY";
    private static final String COMMA_SEP = ",";

    //region Create tables
    private static final String SQL_CREATE_MARKS =
            "CREATE TABLE " + Contract.MarkEntry.TABLE_NAME + " (" +
                    Contract.MarkEntry._ID + PRIMARY_KEY + COMMA_SEP +
                    Contract.MarkEntry.COLUMN_NAME_MARK_ID + TYPE_INTEGER + COMMA_SEP +
                    Contract.MarkEntry.COLUMN_NAME_MARK_NAME + TYPE_TEXT + ")";

    private static final String SQL_CREATE_MODELS =
            "CREATE TABLE " + Contract.ModelEntry.TABLE_NAME + " (" +
                    Contract.ModelEntry._ID + PRIMARY_KEY + COMMA_SEP +
                    Contract.ModelEntry.COLUMN_NAME_MODEL_ID + TYPE_INTEGER + COMMA_SEP +
                    Contract.ModelEntry.COLUMN_NAME_MODEL_NAME + TYPE_TEXT + COMMA_SEP +
                    Contract.ModelEntry.COLUMN_NAME_FK_MARK_ID + TYPE_INTEGER + ")";

    private static final String SQL_CREATE_JOBS =
            "CREATE TABLE " + Contract.JobEntry.TABLE_NAME + " (" +
                    Contract.JobEntry._ID + PRIMARY_KEY + COMMA_SEP +
                    Contract.JobEntry.COLUMN_NAME_JOB_ID + TYPE_INTEGER + COMMA_SEP +
                    Contract.JobEntry.COLUMN_NAME_JOB_NAME + TYPE_TEXT + COMMA_SEP +
                    Contract.JobEntry.COLUMN_NAME_PRICE + TYPE_INTEGER + ")";
    //endregion

    //region Drop tables
    private static final String SQL_DROP_MARKS =
            "DROP TABLE IF EXISTS " + Contract.MarkEntry.TABLE_NAME;
    private static final String SQL_DROP_MODELS =
            "DROP TABLE IF EXISTS " + Contract.ModelEntry.TABLE_NAME;
    private static final String SQL_DROP_JOBS =
            "DROP TABLE IF EXISTS " + Contract.JobEntry.TABLE_NAME;
    //endregion

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MARKS);
        db.execSQL(SQL_CREATE_MODELS);
        db.execSQL(SQL_CREATE_JOBS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DROP_MARKS);
        db.execSQL(SQL_DROP_MODELS);
        db.execSQL(SQL_DROP_JOBS);
        onCreate(db);
    }
}
