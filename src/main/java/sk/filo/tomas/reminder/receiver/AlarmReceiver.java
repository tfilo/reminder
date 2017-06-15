package sk.filo.tomas.reminder.receiver;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Date;

import sk.filo.tomas.reminder.MainActivity;
import sk.filo.tomas.reminder.R;
import sk.filo.tomas.reminder.dao.DatabaseHelper;
import sk.filo.tomas.reminder.item.AlarmExtendedItem;

import static android.content.Context.POWER_SERVICE;

/**
 * Created by tomas on 27.10.2016.
 */

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AlarmReceiver");
        wakeLock.acquire();
        try {
            DatabaseHelper dbH = new DatabaseHelper(context);

            Long id = intent.getLongExtra("alarmId", -1L);
            if (!id.equals(-1L)) {
                Log.d(TAG, "Receiving id " + id);

                AlarmExtendedItem aei = dbH.getExtendedAlarmInfo(id);
                Log.d(TAG, "AlarmExtendedItem " + aei.toString());

                if (aei != null) {
                    NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    Uri alarmSound = Uri.parse(sharedPreferences.getString(MainActivity.RING_TONE, ""));
                    Boolean sound = sharedPreferences.getBoolean(MainActivity.USE_SOUND, true);
                    switch (aei.type) {
                        case CONTACT:
                            builder.setSmallIcon(R.drawable.ic_redeem_white_24dp)
                                    .setTicker(context.getString(R.string.birthday))
                                    .setWhen(System.currentTimeMillis())
                                    .setContentTitle(context.getString(R.string.birthday))
                                    .setContentText(aei.name + " " + context.getString(R.string.has_birthday))
                                    .setAutoCancel(true)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setCategory(Notification.CATEGORY_EVENT);

                            String number = getContactNumberById(context, aei.parentId);

                            if (number!=null && !number.isEmpty()) {
                                Intent dial = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null));
                                PendingIntent pendingDial = PendingIntent.getActivity(context, aei.id.intValue(),
                                        dial, PendingIntent.FLAG_UPDATE_CURRENT);

                                Intent send = new Intent(Intent.ACTION_SENDTO);
                                send.setData(Uri.parse("smsto:" + Uri.encode(number)));
                                send.putExtra("sms_body", context.getResources().getString(R.string.happy_birthday));

                                PendingIntent sendSms = PendingIntent.getActivity(context, Integer.MAX_VALUE - aei.id.intValue(),
                                        send, PendingIntent.FLAG_UPDATE_CURRENT);

                                builder.addAction(R.drawable.ic_phone_white_18dp, context.getResources().getString(R.string.call), pendingDial)
                                       .addAction(R.drawable.ic_message_white_18dp, context.getResources().getString(R.string.message), sendSms);
                            }

                            if (sound) {
                                builder.setSound(alarmSound);
                            }
                            break;
                        case REMINDER:
                            Intent notificationIntent = new Intent(context, MainActivity.class);
                            notificationIntent.putExtra("openFragment", "NewReminderFragment");
                            notificationIntent.putExtra("reminderId", aei.parentId);

                            PendingIntent pendingIntent = PendingIntent.getActivity(context, aei.id.intValue(),
                                    notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            builder.setSmallIcon(R.drawable.ic_alarm_white_24dp)
                                    .setTicker(context.getString(R.string.reminder))
                                    .setWhen(System.currentTimeMillis())
                                    .setContentTitle(aei.name)
                                    .setContentText(aei.description)
                                    .setAutoCancel(true)
                                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                                    .setContentIntent(pendingIntent)
                                    .setCategory(Notification.CATEGORY_REMINDER);

                            if (sound) {
                                builder.setSound(alarmSound);
                            }
                            dbH.enableDisableAlarm(id, false);
                            break;
                    }
                    Notification notif = builder.build();
                    notif.flags |= Notification.FLAG_INSISTENT | Notification.FLAG_AUTO_CANCEL;
                    mNotificationManager.notify(aei.id.intValue(), notif);
                    dbH.updateLastExecuted(id, aei.alarmTime);
                }
            }
        } finally {
            wakeLock.release();
        }
    }

    private String getContactNumberById(Context context, Long id) {
        ContentResolver cr = context.getContentResolver();
        String result = null;
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.NAME_RAW_CONTACT_ID + " = " + id.toString(), null, null);
        if (phones.moveToFirst()) {
            result = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        Log.d(TAG, "Found number: " + result);
        return result;
    }
}
