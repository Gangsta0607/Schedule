package com.schedule;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;
import java.util.GregorianCalendar;

@SuppressLint("DefaultLocale")
public class MyReceiver extends BroadcastReceiver {

    public MyReceiver() {
    }

    private static final int NOTIFY_ID = 1;
    private static final String CHANNEL_ID = "CHANNEL_ID";
    @SuppressLint({"Recycle", "UnsafeProtectedBroadcastReceiver"})
    @Override
    public void onReceive(Context context, Intent intent) {
        Calendar calendar = new GregorianCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        if (calendar.get(Calendar.DAY_OF_MONTH) == 1)
            calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MONTH, 1);
        String day_ = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (day_.length() == 1) day_ = "0" + day_;
        String month = String.valueOf(calendar.get(Calendar.MONTH));
        if (month.length() == 1) month = "0" + month;

        String nextDay = String.format("%s.%s.%s", day_, month, calendar.get(Calendar.YEAR));
        Log.i("nextDay", nextDay);
        SQLiteDatabase db = context.openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        Cursor query;
        try {
            query = db.rawQuery(String.format("SELECT * FROM duties WHERE pass_date='%s'", nextDay), null);
        } catch (android.database.sqlite.SQLiteException e) {
            return;
        }
        Log.i("count", String.valueOf(query.getCount()));
        query.moveToFirst();
        int count = query.getCount();
        if (count > 0) {
            String sign = count == 1 ? "долг" : count == 2 ? "долга" : "долгов";
            String notificationText = String.format("У вас на завтра %d %s", count, sign);

            // Create PendingIntent
            Intent resultIntent = new Intent(context, MainActivity.class);
            @SuppressLint("UnspecifiedImmutableFlag") PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            // Create Notification
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_duties)
                            .setContentTitle("Долги на завтра")
                            .setContentText(notificationText)
                            .setContentIntent(resultPendingIntent);

            // Show Notification
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

            NotificationChannel notificationChannel;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(notificationChannel);
            }
            notificationManager.notify(NOTIFY_ID, builder.build());
        }
        query.close();
        db.close();
    }
}
