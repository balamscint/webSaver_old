package com.wpdf.websaver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.wpdf.adapter.NotificationAdapter;
import com.wpdf.model.NotificationModel;
import com.wpdf.services.FirebaseNotificationService;

import java.util.ArrayList;

public class NotificationActivity extends AppCompatActivity {

    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent
                    .getStringExtra(FirebaseNotificationService.ExtraMessage);

            if (message != null && !message.equalsIgnoreCase("")) {
                ///
            }
        }
    };
    public NotificationAdapter notificationAdapter;
    public ListView listViewNotifications;

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(NotificationActivity.this, PDFActivity.class));
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create channel to show notifications.
            String channelId = getString(R.string.default_notification_channel_id);
            String channelName = getString(R.string.default_notification_channel_name);
            NotificationManager notificationManager =
                    getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                    channelName, NotificationManager.IMPORTANCE_LOW));
        }

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                Log.d("NotificationActivity", "Key: " + key + " Value: " + value);
            }
        }

        listViewNotifications = findViewById(R.id.listViewNotification);
        TextView emptyTextView = findViewById(android.R.id.empty);
        listViewNotifications.setEmptyView(emptyTextView);

        notificationAdapter = new
                NotificationAdapter(NotificationActivity.this, getNotifications());
        listViewNotifications.setAdapter(notificationAdapter);
    }


    private ArrayList<NotificationModel> getNotifications() {

        ArrayList<NotificationModel> notificationModels = new ArrayList<>();
        Cursor cursor = null;
        try {

           /* String strDocument;
            int iNew;
            String strDocId;

            // +  " AND updated_date = '"+currentDate+"'"
            // select * from collections  where  collection_name = 'notification' and updated_date >= date('now','-15 day') and updated_date <= date('now') and updated_date like '2017-01-11%'
            cursor = fetch(
                    DbHelper.strTableNameCollection, new String[]{DbHelper.COLUMN_OBJECT_ID, DbHelper.COLUMN_DOCUMENT
                            , DbHelper.COLUMN_NEW_UPDATED},
                    DbHelper.COLUMN_COLLECTION_NAME + "=? AND updated_date >= date('now','-15 day') and updated_date <= date('now')" +
                            " OR updated_date like '" + currentDate + "%'",
                    new String[]{Config.collectionNotification}, DbHelper.COLUMN_UPDATE_DATE
                            + " desc", null, true, null, null);

            if (cursor != null && cursor.getCount() > 0) {
                Config.notificationModels.clear();
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    strDocId = cursor.getString(0);
                    strDocument = cursor.getString(1);
                    iNew = cursor.getInt(2);


                    JSONObject jsonObjectProvider = new JSONObject(strDocument);

                    if (jsonObjectProvider.has(App42GCMService.ExtraMessage)) {*/
            notificationModels.add(new NotificationModel("TEST", 1, 1));

                   /* }
                    cursor.moveToNext();
                }
            }
            CareGiver.getDbCon().closeCursor(cursor);

            //
            CareGiver.getDbCon().update(
                    DbHelper.strTableNameCollection,
                    DbHelper.COLUMN_COLLECTION_NAME + "=?",
                    new String[]{"0"},
                    new String[]{DbHelper.COLUMN_NEW_UPDATED},
                    new String[]{Config.collectionNotification});*/
            //

        } catch (Exception e) {
            e.printStackTrace();
        }

        return notificationModels;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter(
                FirebaseNotificationService.DisplayMessageAction);
        filter.setPriority(2);
        registerReceiver(mBroadcastReceiver, filter);
    }
}
