package dev.chuahou.juicebar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BarService extends Service {

    /** Tag for logging. */
    private static final String TAG = "juicebar.BarService";

    /** Channel and notification IDs for foreground service notification. */
    private static final String CHANNEL_ID = "Juice Bar notification channel";
    private static final int NOTIF_ID = 2357;

    /** Worker thread that does the heavy lifting. */
    class BarThread extends Thread {
        public void run() {
            try {
                while (!interrupted()) {
                    sleep(1000);
                    Log.i(TAG, "Running");
                }
            } catch (InterruptedException e) {}
        }
    }
    private BarThread thread = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Creating notification channel");
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID,
                NotificationManager.IMPORTANCE_MIN);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    /** Starts the service by starting the worker thread. */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service");

        // Make us a foreground service.
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentText("Juice Bar is running")
                .setOngoing(true)
                .build();
        startForeground(NOTIF_ID, notification);

        // Start thread.
        thread = new BarThread();
        thread.start();
        return START_NOT_STICKY;
    }

    /** Stops the service by interrupting the worker thread. */
    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service");
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; } // No binding required.
}
