package com.wpdf.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.wpdf.dbConfig.Dbcon;
import com.wpdf.dbConfig.Dbhelper;
import com.wpdf.libs.Utils;
import com.wpdf.websaver.NotificationActivity;
import com.wpdf.websaver.R;

import java.util.List;
import java.util.Random;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;


public class FirebaseNotificationService extends FirebaseMessagingService {

    public static final String DisplayMessageAction = "com.wpdf.websaver.NotificationActivity";
    public static final String ExtraMessage = "message";
    private final static String GROUP_KEY = "notification";
    private static final String TAG = "FirebaseNotification";
    private static int msgCount = 0;

    public FirebaseNotificationService() {
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.getData());

            sendNotification(remoteMessage.getData().toString());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        /*Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 *//* Request code *//*, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("FCM Message")
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 *//* ID of notification *//*, notificationBuilder.build());*/

        //

        Dbcon db = new Dbcon(this);

        String fieldValues[] = new String[]{messageBody, "0"};
        String fieldNames[] = new String[]{"message", "read"};

        try {
            db.insert(fieldValues, fieldNames, Dbhelper.NOTIFICATIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long when = System.currentTimeMillis();

        NotificationManager mNotificationManager = (NotificationManager) this.
                getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent;

        notificationIntent = new Intent(this, NotificationActivity.class);

        notificationIntent.putExtra("message_delivered", true);
        notificationIntent.putExtra(ExtraMessage, messageBody);
        notificationIntent.putExtra("LOAD", false);

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //notificationIntent.setFlags(603979776);

        //
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(NotificationActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(notificationIntent);
        //

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.icon);

        mBuilder.setSmallIcon(R.drawable.icon)
                .setLargeIcon(largeIcon)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(messageBody).setWhen(when).setNumber(++msgCount)
                .setDefaults(1).setDefaults(2)
                .setLights(Notification.DEFAULT_LIGHTS, 5000, 5000)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setAutoCancel(true)
                .setGroup(GROUP_KEY) //GROUP_KEY strClientId
                .setVisibility(VISIBILITY_PUBLIC)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI);

        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setColor(getResources().getColor(R.color.colorPrimaryDark));
        }

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        //String[] events = msg.split(""); //new String[6]
        // Sets a title for the Inbox in expanded layout
        inboxStyle.setBigContentTitle(getString(R.string.app_name));

        //split message
        List<String> events = Utils.splitLongText(messageBody, 40);
        //

        // Moves events into the expanded layout
        for (String event : events) {
            inboxStyle.addLine(event);
        }

        mBuilder.setStyle(inboxStyle);

        mBuilder.setContentIntent(contentIntent);

        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        mNotificationManager.notify(m, mBuilder.build());

    }

    /**
     * @param message
     */
    private void broadCastMessage(String message) {
        Intent intent = new Intent(DisplayMessageAction);
        intent.putExtra(ExtraMessage, message);
        this.sendBroadcast(intent);
    }
}
