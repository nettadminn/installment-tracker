package com.example.installmentapp.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.installmentapp.R;
import com.example.installmentapp.database.InstallmentDbHelper;
import com.example.installmentapp.model.Installment;

import java.util.List;

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    private static final String CHANNEL_ID = "installment_reminder_channel";
    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.d(TAG, "onReceive triggered");

            // Create notification channel for Android 8.0+
            createNotificationChannel(context);

            // Check notification permission (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "POST_NOTIFICATIONS permission not granted, skipping notification");
                    // Still reschedule alarm even if notification permission denied
                    rescheduleAlarm(context);
                    return;
                }
            }

            // Check for installments that are due for notification
            InstallmentDbHelper dbHelper = new InstallmentDbHelper(context);
            List<Installment> installments = dbHelper.getAllInstallments();

            long currentTime = System.currentTimeMillis();
            StringBuilder notificationMessage = new StringBuilder();
            boolean hasNotifications = false;

            for (Installment installment : installments) {
                long notifyTime = installment.getDueDateMillis() - (installment.getNotifyDaysBefore() * 24 * 60 * 60 * 1000L);
                if (currentTime >= notifyTime && currentTime < installment.getDueDateMillis()) {
                    // Time to show notification (between notify time and due date)
                    hasNotifications = true;
                    notificationMessage.append("• ")
                            .append(installment.getTitle())
                            .append(" - ")
                            .append(com.example.installmentapp.util.DateConverter.gregorianToPersian(installment.getDueDateMillis()))
                            .append("\n");
                }
            }

            if (hasNotifications) {
                // Show notification
                Intent notificationIntent = new Intent(context, com.example.installmentapp.MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle("یادآوری قسط")
                        .setContentText("قسط‌های مورد نیاز پرداخت:")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(notificationMessage.toString()))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .build();

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFICATION_ID, notification);
                Log.d(TAG, "Notification sent for " + installments.size() + " installments");
            }

            // Reschedule for next notification
            rescheduleAlarm(context);

        } catch (Exception e) {
            Log.e(TAG, "Error in onReceive", e);
            // Try to reschedule anyway
            try {
                rescheduleAlarm(context);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to reschedule alarm", ex);
            }
        }
    }

    private void createNotificationChannel(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "Installment Reminder";
                String description = "Channel for installment payment reminders";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);
                NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification channel", e);
        }
    }

    private void rescheduleAlarm(Context context) {
        try {
            // Check exact alarm permission (Android 14+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms - permission not granted");
                    return;
                }
            }

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            // Cancel existing alarm
            alarmManager.cancel(pendingIntent);

            // Set new alarm based on earliest installment
            InstallmentDbHelper dbHelper = new InstallmentDbHelper(context);
            List<Installment> installments = dbHelper.getAllInstallments();
            long earliestTrigger = Long.MAX_VALUE;

            for (Installment installment : installments) {
                long notifyTime = installment.getDueDateMillis() - (installment.getNotifyDaysBefore() * 24 * 60 * 60 * 1000L);
                if (notifyTime < earliestTrigger) {
                    earliestTrigger = notifyTime;
                }
            }

            if (earliestTrigger != Long.MAX_VALUE) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, earliestTrigger, pendingIntent);
                Log.d(TAG, "Alarm rescheduled for: " + earliestTrigger);
            } else {
                Log.d(TAG, "No installments to schedule");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException scheduling alarm - exact alarm permission may be missing", e);
        } catch (Exception e) {
            Log.e(TAG, "Error rescheduling alarm", e);
        }
    }
}