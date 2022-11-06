package dev.chuahou.juicebar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class BarService extends Service {

    /** Check if service is currently running. */
    public static boolean isRunning() {
        return running;
    }
    private static boolean running = false;

    /** Tag for logging. */
    private static final String TAG = "juicebar.BarService";

    /** Channel and notification IDs for foreground service notification. */
    private static final String CHANNEL_ID = "Juice Bar notification channel";
    private static final int NOTIF_ID = 2357;

    /** BarView to draw. */
    private BarView barView;

    /** Broadcast receiver for battery changes. */
    private class BattReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Battery changed, setting battery level");
            setBattLevelFromIntent(intent);
        }
    }
    private BattReceiver battReceiver = null;

    /** Get battery level from given intent and update barView. */
    private void setBattLevelFromIntent(Intent intent) {
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        float battLevel = level / (float) scale;
        barView.setBattLevel(battLevel / 0.8f);
    }

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

        running = true;

        // Make us a foreground service.
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Juice Bar")
                .setContentText("Juice Bar is running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setOngoing(true)
                .build();
        startForeground(NOTIF_ID, notification);

        // Create our bar view and place it over the status bar.
        barView = new BarView(this);
        WindowManager windowManager = getSystemService(WindowManager.class);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        Log.d(TAG, "Screen size: " + displayMetrics.widthPixels + "x" + displayMetrics.heightPixels);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                8, // Fixed hard-coded height for personal preference for now.
                0, -displayMetrics.heightPixels / 2, // Position at top of screen.
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Overlay behind status bar.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // Forward touches.
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                            // Allow us to go behind status bar.
                PixelFormat.TRANSLUCENT);
        windowManager.addView(barView, params);

        // Register receiver for battery information change.
        battReceiver = new BattReceiver();
        Intent battStatus = this.registerReceiver(battReceiver,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        setBattLevelFromIntent(battStatus);

        return START_NOT_STICKY;
    }

    /** Stops the service by removing view and unregistering receiver. */
    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service");
        running = false;
        getSystemService(WindowManager.class).removeView(barView);
        if (battReceiver != null) {
            this.unregisterReceiver(battReceiver);
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; } // No binding required.
}
