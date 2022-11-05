package dev.chuahou.juicebar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
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

    /** Worker thread that does the heavy lifting. */
    class BarThread extends Thread {

        private BarView barView;
        public BarThread(BarView barView) { this.barView = barView; }

        public void run() {
            Log.i(TAG, "Thread started");

            // Main loop. Stops when interrupted.
            try {
                while (!interrupted()) {
                    // Receive battery information.
                    IntentFilter battIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent battStatus = (BarService.this).registerReceiver(null, battIntentFilter);

                    // Battery level out of 1.0.
                    float battLevel = battStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                            / (float) battStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 1);

                    // Scale battery level to be % of maximum intended charge and send it to
                    // BarView.
                    float battLevelScaled = battLevel / 0.8f;
                    barView.setBattLevel(battLevelScaled);

                    sleep(1000);
                }
            } catch (InterruptedException e) {}

            Log.i(TAG, "Thread stopped");
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

        running = true;

        // Make us a foreground service.
        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentText("Juice Bar is running")
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
                100,
                0, -displayMetrics.heightPixels / 2, // Position at top of screen.
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Overlay behind status bar.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE // Forward touches.
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                            // Allow us to go behind status bar.
                PixelFormat.TRANSLUCENT);
        windowManager.addView(barView, params);

        // Start thread.
        thread = new BarThread(barView);
        thread.start();
        return START_NOT_STICKY;
    }

    /** Stops the service by interrupting the worker thread. */
    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service");
        running = false;
        getSystemService(WindowManager.class).removeView(barView);
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; } // No binding required.
}
