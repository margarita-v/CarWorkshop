package com.dsr_practice.car_workshop.database;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * This class describes all local entities for Provider class
 */
public class Contract {
    /**
     * To prevent someone from accidentally instantiating the contract class,
     * give it an empty constructor.
     */
    public Contract() {}

    public static final String CONTENT_AUTHORITY = "com.dsr_practice.car_workshop.database";
    private static final Uri BASE_CONTENT_URI  = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_MARKS  = "marks";
    static final String PATH_MODELS = "models";
    static final String PATH_JOBS   = "jobs";
    private static final String SEPARATOR   = "/";

    /**
     *  Projections for all items which will be stored in local database.
     *  COLUMN_ITEM_NAME is first for usage in cursor adapters
     */
    public static final String[] MARK_PROJECTION = new String[] {
            MarkEntry.COLUMN_NAME_MARK_NAME,
            MarkEntry.COLUMN_NAME_MARK_ID,
            MarkEntry._ID
    };
    public static final String[] MODEL_PROJECTION = new String[] {
            ModelEntry.COLUMN_NAME_MODEL_NAME,
            ModelEntry.COLUMN_NAME_FK_MARK_ID,
            ModelEntry.COLUMN_NAME_MODEL_ID,
            ModelEntry._ID
    };
    public static final String[] JOB_PROJECTION = new String[] {
            JobEntry.COLUMN_NAME_JOB_NAME,
            JobEntry.COLUMN_NAME_PRICE,
            JobEntry.COLUMN_NAME_JOB_ID,
            JobEntry._ID
    };

    /**
     * Columns supported by "marks" records
     */
    public static class MarkEntry implements BaseColumns {
        static final String TABLE_NAME = "mark";
        // Not to be confused with the database primary key, which is _ID
        public static final String COLUMN_NAME_MARK_ID = "mark_id";
        public static final String COLUMN_NAME_MARK_NAME = "mark_name";

        // Fully qualified URI for "mark" resources
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MARKS).build();
        // MIME type for lists of marks
        static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_URI + SEPARATOR + PATH_MARKS;
        // MIME type for individual marks
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_URI + SEPARATOR + PATH_MARKS;
    }

    /**
     * Columns supported by "models" records
     */
    public static class ModelEntry implements BaseColumns {
        static final String TABLE_NAME = "model";
        // Not to be confused with the database primary key, which is _ID
        public static final String COLUMN_NAME_MODEL_ID = "model_id";
        public static final String COLUMN_NAME_MODEL_NAME = "model_name";
        public static final String COLUMN_NAME_FK_MARK_ID = "fk_mark_id";

        // Fully qualified URI for "model" resources
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MODELS).build();
        // MIME type for lists of models
        static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_URI + SEPARATOR + PATH_MODELS;
        // MIME type for individual models
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_URI + SEPARATOR + PATH_MODELS;
    }

    /**
     * Columns supported by "jobs" records
     */
    public static class JobEntry implements BaseColumns {
        static final String TABLE_NAME = "job";
        // Not to be confused with the database primary key, which is _ID
        public static final String COLUMN_NAME_JOB_ID = "job_id";
        public static final String COLUMN_NAME_JOB_NAME = "job_name";
        public static final String COLUMN_NAME_PRICE = "price";

        // Fully qualified URI for "job" resources
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOBS).build();
        // MIME type for lists of jobs
        static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + CONTENT_URI + SEPARATOR + PATH_JOBS;
        // MIME type for individual jobs
        static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + CONTENT_URI + SEPARATOR + PATH_JOBS;
    }
}
