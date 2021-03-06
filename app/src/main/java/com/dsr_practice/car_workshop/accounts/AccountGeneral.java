package com.dsr_practice.car_workshop.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.dsr_practice.car_workshop.database.Contract;
import com.dsr_practice.car_workshop.sync.SyncAdapter;

public class AccountGeneral {
    private static final String ACCOUNT_TYPE = "com.dsr_practice.car_workshop.accounts";
    private static final String ACCOUNT_NAME = "Car workshop account";
    private static final String AUTHORITY = Contract.CONTENT_AUTHORITY;
    private static final long   SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)
    private static final String PREF_SETUP_COMPLETE = "setup_complete";

    // Gets the standard sync account
    public static Account getAccount() {
        return new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
    }

    // Creates the standard sync account
    public static void createSyncAccount(Context context) {
        // Flag to determine if this is a new account or not
        boolean created = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        // Get an account and the account manager
        Account account = getAccount();
        AccountManager manager = (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

        // Attempt to explicitly create the account with no password or extra data
        if (manager.addAccountExplicitly(account, null, null)) {

            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, AUTHORITY, 1);

            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, AUTHORITY, true);

            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(account, AUTHORITY, new Bundle(), SYNC_FREQUENCY);

            created = true;
        }

        // Schedule an initial sync if we detect problems with either our account
        // or our local data has been deleted
        if (created || !setupComplete) {
            SyncAdapter.performSync();
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }
}
