package com.dsr_practice.car_workshop.sync;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.SparseArray;

import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.sync.Job;
import com.dsr_practice.car_workshop.models.common.sync.Mark;
import com.dsr_practice.car_workshop.models.common.sync.Model;
import com.dsr_practice.car_workshop.models.common.sync.SyncModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Class which implements sync logic
 */
class SyncImplementation<T extends SyncModel> implements SyncInterface {

    // Entries which were loaded from server
    private List<T> loadedItems;

    // ID for determination of loaded entries' type
    private final int itemId;

    // Entry's content URI for database access
    private final Uri contentUri;

    // Current projection which will be used for queries
    private final String[] projection;

    // Column name for entry's id
    private final String columnNameId;
    // Column name for entry's name field
    private final String columnNameForName;
    // Column name for optional field if an entry have it
    private String columnNameOptional;

    //region Local projections for all entries for more convenient loading data from cursor
    private static final String[] MARK_PROJECTION = new String[] {
            Contract.MarkEntry._ID,
            Contract.MarkEntry.COLUMN_NAME_MARK_ID,
            Contract.MarkEntry.COLUMN_NAME_MARK_NAME,
    };
    private static final String[] MODEL_PROJECTION = new String[] {
            Contract.ModelEntry._ID,
            Contract.ModelEntry.COLUMN_NAME_MODEL_ID,
            Contract.ModelEntry.COLUMN_NAME_MODEL_NAME,
            Contract.ModelEntry.COLUMN_NAME_FK_MARK_ID,
    };
    private static final String[] JOB_PROJECTION = new String[] {
            Contract.JobEntry._ID,
            Contract.JobEntry.COLUMN_NAME_JOB_ID,
            Contract.JobEntry.COLUMN_NAME_JOB_NAME,
            Contract.JobEntry.COLUMN_NAME_PRICE,
    };
    //endregion

    //region Constants representing column positions from every PROJECTION
    private static final int COLUMN_ID = 0;
    private static final int COLUMN_ENTRY_ID = 1;
    private static final int COLUMN_ENTRY_NAME = 2;
    private static final int COLUMN_OPTIONAL_FIELD = 3;
    //endregion

    /**
     * Constructor for initialization of all fields
     * @param loadedEntries Items which were loaded from server
     * @param entryId ID of items' type
     */
    SyncImplementation(List<T> loadedEntries, int entryId) {
        this.loadedItems = loadedEntries;
        this.itemId = entryId;
        //region Configure all fields which depend on entry's type
        switch (this.itemId) {
            case Job.JOB_ID: {
                this.contentUri = Contract.JobEntry.CONTENT_URI;
                this.projection = JOB_PROJECTION;

                this.columnNameId = Contract.JobEntry.COLUMN_NAME_JOB_ID;
                this.columnNameForName = Contract.JobEntry.COLUMN_NAME_JOB_NAME;
                this.columnNameOptional = Contract.JobEntry.COLUMN_NAME_PRICE;
                break;
            }
            case Mark.MARK_ID: {
                this.contentUri = Contract.MarkEntry.CONTENT_URI;
                this.projection = MARK_PROJECTION;

                this.columnNameId = Contract.MarkEntry.COLUMN_NAME_MARK_ID;
                this.columnNameForName = Contract.MarkEntry.COLUMN_NAME_MARK_NAME;
                break;
            }
            default: {
                this.contentUri = Contract.ModelEntry.CONTENT_URI;
                this.projection = MODEL_PROJECTION;

                this.columnNameId = Contract.ModelEntry.COLUMN_NAME_MODEL_ID;
                this.columnNameForName = Contract.ModelEntry.COLUMN_NAME_MODEL_NAME;
                this.columnNameOptional = Contract.ModelEntry.COLUMN_NAME_FK_MARK_ID;
            }
        }
        //endregion
    }

    @Override
    public void sync(ContentResolver contentResolver, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of loaded entries
        SparseArray<SyncModel> entryArray = new SparseArray<>();
        for (SyncModel entry : loadedItems) {
            entryArray.put(entry.getId(), entry);

        }

        // Get all entries in local database to compare with data from server
        Cursor cursor = contentResolver
                .query(contentUri, projection, null, null, null);
        assert cursor != null;

        // Cursor fields
        int id, entryId, optional = -1;
        String name;
        while (cursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = cursor.getInt(COLUMN_ID);
            entryId = cursor.getInt(COLUMN_ENTRY_ID);
            name = cursor.getString(COLUMN_ENTRY_NAME);
            if (itemId != Mark.MARK_ID)
                optional = cursor.getInt(COLUMN_OPTIONAL_FIELD);

            SyncModel match = entryArray.get(entryId);
            if (match != null) {
                // Entry exists. Remove from entry map to prevent insert later
                entryArray.remove(entryId);
                // Check to see if the entry needs to be updated
                Uri uri = contentUri.buildUpon().appendPath(Integer.toString(id)).build();
                if (match.getName() != null && !match.getName().equals(name)) {
                    /*
                    Update existing record.
                    For jobs we should check if the job's price was changed
                    For other models we check only names.
                    NOTE: for models we don't check if the model's mark was changed on server
                     */
                    if (entryId != Job.JOB_ID) {
                        batch.add(ContentProviderOperation.newUpdate(uri)
                                .withValue(columnNameForName, match.getName())
                                .build());
                    }
                    else {
                        Job job = (Job) match;
                        if (job.getPrice() != optional) {
                            batch.add(ContentProviderOperation.newUpdate(uri)
                                    .withValue(columnNameForName, match.getName())
                                    .withValue(columnNameOptional, job.getPrice())
                                    .build());
                        }
                    }
                    syncResult.stats.numUpdates++;
                }
                else {
                    // Entry doesn't exist anymore. Remove it from the database
                    batch.add(ContentProviderOperation.newDelete(uri).build());
                    syncResult.stats.numDeletes++;
                }
            }
        } // while
        cursor.close();

        // Add new models
        for (int i = 0; i < entryArray.size(); i++) {
            int key = entryArray.keyAt(i);
            SyncModel syncModel = entryArray.get(key);
            if (this.itemId == Mark.MARK_ID) {
                batch.add(ContentProviderOperation.newInsert(contentUri)
                        .withValue(columnNameId, syncModel.getId())
                        .withValue(columnNameForName, syncModel.getName())
                        .build());
            }
            else {
                batch.add(ContentProviderOperation.newInsert(contentUri)
                        .withValue(columnNameId, syncModel.getId())
                        .withValue(columnNameForName, syncModel.getName())
                        .withValue(columnNameOptional,
                                this.itemId == Job.JOB_ID
                                        ? ((Job) syncModel).getPrice()
                                        : ((Model) syncModel).getMark())
                        .build());
            }
            syncResult.stats.numInserts++;
        }
        contentResolver.applyBatch(Contract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(
                contentUri,  // URI where data was modified
                null,        // No local observer
                false);
    }
}
