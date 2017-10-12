package com.dsr_practice.car_workshop.models.lists;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.SparseArray;

import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.models.common.sync.Model;
import com.dsr_practice.car_workshop.sync.SyncAdapter;
import com.dsr_practice.car_workshop.sync.SyncInterface;

import java.util.ArrayList;
import java.util.List;

public class ModelList implements SyncInterface {
    private List<Model> models;

    private static final String[] MODEL_PROJECTION = new String[] {
            Contract.ModelEntry._ID,
            Contract.ModelEntry.COLUMN_NAME_MODEL_ID,
            Contract.ModelEntry.COLUMN_NAME_MODEL_NAME,
            Contract.ModelEntry.COLUMN_NAME_FK_MARK_ID
    };

    public ModelList(List<Model> models) {
        this.models = models;
    }

    @Override
    public void sync(ContentResolver contentResolver, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();

        // Build hash table of incoming models
        SparseArray<Model> entryArray = new SparseArray<>();
        for (Model model : models) {
            entryArray.put(model.getId(), model);
        }

        // Get all models in local database to compare with data from server
        Cursor cursor = contentResolver.query(
                Contract.ModelEntry.CONTENT_URI, MODEL_PROJECTION, null, null, null);
        assert cursor != null;

        // Cursor fields
        int id, modelId;
        String name;
        while (cursor.moveToNext()) {
            syncResult.stats.numEntries++;
            id = cursor.getInt(SyncAdapter.COLUMN_ID);
            modelId = cursor.getInt(SyncAdapter.COLUMN_ENTRY_ID);
            name = cursor.getString(SyncAdapter.COLUMN_ENTRY_NAME);

            Model match = entryArray.get(modelId);
            if (match != null) {
                // Model exists. Remove from entry map to prevent insert later
                entryArray.remove(modelId);
                // Check to see if the model needs to be updated
                Uri uri = Contract.ModelEntry.CONTENT_URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                if (match.getName() != null && !match.getName().equals(name)) {
                    // Update existing record
                    batch.add(ContentProviderOperation.newUpdate(uri)
                            .withValue(Contract.ModelEntry.COLUMN_NAME_MODEL_NAME, match.getName())
                            .build());
                    syncResult.stats.numUpdates++;
                }
                else {
                    // Model doesn't exist anymore. Remove it from the database
                    batch.add(ContentProviderOperation.newDelete(uri).build());
                    syncResult.stats.numDeletes++;
                }
            }
        } // while
        cursor.close();

        // Add new models
        for (int i = 0; i < entryArray.size(); i++) {
            int key = entryArray.keyAt(i);
            Model model = entryArray.get(key);
            batch.add(ContentProviderOperation.newInsert(Contract.ModelEntry.CONTENT_URI)
                    .withValue(Contract.ModelEntry.COLUMN_NAME_MODEL_ID, model.getId())
                    .withValue(Contract.ModelEntry.COLUMN_NAME_MODEL_NAME, model.getName())
                    .withValue(Contract.ModelEntry.COLUMN_NAME_FK_MARK_ID, model.getMark())
                    .build());
            syncResult.stats.numInserts++;
        }
        contentResolver.applyBatch(Contract.CONTENT_AUTHORITY, batch);
        contentResolver.notifyChange(
                Contract.ModelEntry.CONTENT_URI, // URI where data was modified
                null,                           // No local observer
                false);
    }
}
