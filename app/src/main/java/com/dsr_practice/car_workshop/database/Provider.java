package com.dsr_practice.car_workshop.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * This class implements all actions for database usage
 */
public class Provider extends ContentProvider {
    private DatabaseHelper databaseHelper;
    private static final String SLASH = "/";
    private static final String ADD_TO_PATH = "/*";

    // Content authority for this provider
    private static final String AUTHORITY = Contract.CONTENT_AUTHORITY;

    private static final String PATH_GET_MODELS_FOR_MARK = "models_for_mark";
    public static final Uri URI_MODELS_FOR_MARK =
            Uri.parse("content://" + AUTHORITY + "/" + PATH_GET_MODELS_FOR_MARK);
    private static final String SQL_GET_MODELS_FOR_MARK = "SELECT * FROM " +
            Contract.MarkEntry.TABLE_NAME + " JOIN " + Contract.ModelEntry.TABLE_NAME + " ON " +
            Contract.MarkEntry.COLUMN_NAME_MARK_ID + " = " + Contract.ModelEntry.COLUMN_NAME_FK_MARK_ID +
            " WHERE " + Contract.MarkEntry.COLUMN_NAME_MARK_ID + "= ?";

    // URI ID for route: /marks
    public static final int ROUTE_MARKS = 1;
    // URI ID for route: /marks/{ID}
    public static final int ROUTE_MARKS_ID = 2;
    // URI ID for route: /models_for_mark/{ID}
    public static final int ROUTE_MODELS_FOR_MARK = 3;
    // URI ID for route: /models
    public static final int ROUTE_MODELS = 4;
    // URI ID for route: /models/{ID}
    public static final int ROUTE_MODELS_ID = 5;
    // URI ID for route: /jobs
    public static final int ROUTE_JOBS = 6;
    // URI ID for route: /jobs/{ID}
    public static final int ROUTE_JOBS_ID = 7;

    // UriMatcher, used to decode incoming URIs
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, Contract.PATH_MARKS, ROUTE_MARKS);
        sUriMatcher.addURI(AUTHORITY, Contract.PATH_MARKS + ADD_TO_PATH, ROUTE_MARKS_ID);
        sUriMatcher.addURI(AUTHORITY, PATH_GET_MODELS_FOR_MARK, ROUTE_MODELS_FOR_MARK);
        sUriMatcher.addURI(AUTHORITY, Contract.PATH_MODELS, ROUTE_MODELS);
        sUriMatcher.addURI(AUTHORITY, Contract.PATH_MODELS + ADD_TO_PATH, ROUTE_MODELS_ID);
        sUriMatcher.addURI(AUTHORITY, Contract.PATH_JOBS, ROUTE_JOBS);
        sUriMatcher.addURI(AUTHORITY, Contract.PATH_JOBS + ADD_TO_PATH, ROUTE_JOBS_ID);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Determine the mime type for entries returned by a given URI
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_MARKS:
                return Contract.MarkEntry.CONTENT_TYPE;
            case ROUTE_MARKS_ID:
                return Contract.MarkEntry.CONTENT_ITEM_TYPE;
            case ROUTE_MODELS_FOR_MARK:
                return Contract.ModelEntry.CONTENT_TYPE; // return models for mark
            case ROUTE_MODELS:
                return Contract.ModelEntry.CONTENT_TYPE;
            case ROUTE_MODELS_ID:
                return Contract.ModelEntry.CONTENT_ITEM_TYPE;
            case ROUTE_JOBS:
                return Contract.JobEntry.CONTENT_TYPE;
            case ROUTE_JOBS_ID:
                return Contract.JobEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        int uriMatch = sUriMatcher.match(uri);
        String id = uri.getLastPathSegment();
        Cursor cursor;
        switch (uriMatch) {
            case ROUTE_MARKS:
                cursor = db.query(Contract.MarkEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ROUTE_MARKS_ID:
                cursor = db.query(
                        Contract.MarkEntry.TABLE_NAME,
                        projection,
                        configureWhere(Contract.MarkEntry.COLUMN_NAME_MARK_ID, id, selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ROUTE_MODELS_FOR_MARK:
                cursor = db.rawQuery(SQL_GET_MODELS_FOR_MARK, selectionArgs);
                break;
            case ROUTE_MODELS:
                cursor = db.query(Contract.ModelEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ROUTE_MODELS_ID:
                cursor = db.query(
                        Contract.ModelEntry.TABLE_NAME,
                        projection,
                        configureWhere(Contract.ModelEntry.COLUMN_NAME_MODEL_ID, id, selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case ROUTE_JOBS:
                cursor = db.query(Contract.JobEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case ROUTE_JOBS_ID:
                cursor = db.query(
                        Contract.JobEntry.TABLE_NAME,
                        projection,
                        configureWhere(Contract.JobEntry.COLUMN_NAME_JOB_ID, id, selection),
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        cursor.setNotificationUri(ctx.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        assert db != null;
        final int match = sUriMatcher.match(uri);
        Uri result;
        long id;
        switch (match) {
            case ROUTE_MARKS:
                id = db.insertOrThrow(Contract.MarkEntry.TABLE_NAME, null, values);
                result = Uri.parse(Contract.MarkEntry.CONTENT_URI + SLASH + id);
                break;
            case ROUTE_MODELS:
                id = db.insertOrThrow(Contract.ModelEntry.TABLE_NAME, null, values);
                result = Uri.parse(Contract.ModelEntry.CONTENT_URI + SLASH + id);
                break;
            case ROUTE_JOBS:
                id = db.insertOrThrow(Contract.JobEntry.TABLE_NAME, null, values);
                result = Uri.parse(Contract.JobEntry.CONTENT_URI + SLASH + id);
                break;
            default:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String id = uri.getLastPathSegment();
        int count;
        switch (match) {
            case ROUTE_MARKS:
                count = db.delete(Contract.MarkEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTE_MARKS_ID:
                count = db.delete(
                        Contract.MarkEntry.TABLE_NAME,
                        configureWhere(Contract.MarkEntry.COLUMN_NAME_MARK_ID, id, selection),
                        selectionArgs);
                break;
            case ROUTE_MODELS:
                count = db.delete(Contract.ModelEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTE_MODELS_ID:
                count = db.delete(
                        Contract.ModelEntry.TABLE_NAME,
                        configureWhere(Contract.ModelEntry.COLUMN_NAME_MODEL_ID, id, selection),
                        selectionArgs);
                break;
            case ROUTE_JOBS:
                count = db.delete(Contract.JobEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case ROUTE_JOBS_ID:
                count = db.delete(
                        Contract.JobEntry.TABLE_NAME,
                        configureWhere(Contract.JobEntry.COLUMN_NAME_JOB_ID, id, selection),
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Delete not supported on URI: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        if (count > 0) {
            Context ctx = getContext();
            assert ctx != null;
            ctx.getContentResolver().notifyChange(uri, null, false);
        }
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = databaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String id = uri.getLastPathSegment();
        int count;
        switch (match) {
            case ROUTE_MARKS:
                count = db.update(Contract.MarkEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTE_MARKS_ID:
                count = db.update(
                        Contract.MarkEntry.TABLE_NAME,
                        values,
                        configureWhere(Contract.MarkEntry.COLUMN_NAME_MARK_ID, id, selection),
                        selectionArgs);
                break;
            case ROUTE_MODELS:
                count = db.update(Contract.ModelEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTE_MODELS_ID:
                count = db.update(
                        Contract.ModelEntry.TABLE_NAME,
                        values,
                        configureWhere(Contract.ModelEntry.COLUMN_NAME_MODEL_ID, id, selection),
                        selectionArgs);
                break;
            case ROUTE_JOBS:
                count = db.update(Contract.JobEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case ROUTE_JOBS_ID:
                count = db.update(
                        Contract.JobEntry.TABLE_NAME,
                        values,
                        configureWhere(Contract.JobEntry.COLUMN_NAME_JOB_ID, id, selection),
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Update not supported on URI: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        if (count > 0) {
            Context ctx = getContext();
            assert ctx != null;
            ctx.getContentResolver().notifyChange(uri, null, false);
        }
        return count;
    }

    private String configureWhere(String idColumnName, String idValue, String selection) {
        String result = idColumnName + " = " + idValue;
        if (!TextUtils.isEmpty(selection)) {
            result += " AND " + selection;
        }
        return result;
    }
}
