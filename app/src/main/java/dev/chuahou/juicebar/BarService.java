package dev.chuahou.juicebar;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

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

    /** Worker thread that does the heavy lifting. */
    class BarThread extends Thread {
        private TextView text;
        public BarThread(TextView text) { this.text = text; }

        public void run() {
            Log.i(TAG, "Thread started");

            // Handler to run things in main UI thread.
            Handler handler = new Handler(Looper.getMainLooper());

            // Main loop. Stops when interrupted.
            try {
                while (!interrupted()) {
                    // Receive battery information.
                    IntentFilter battIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent battStatus = (BarService.this).registerReceiver(null, battIntentFilter);

                    // Battery level out of 100.0.
                    float battLevel = battStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) * 100
                            / (float) battStatus.getIntExtra(BatteryManager.EXTRA_SCALE, 1);

                    // Scale battery level to be % of maximum intended charge.
                    float battLevelScaled = battLevel / 0.8f;

                    Log.i(TAG, "Battery level: " + battLevel + "%, scaled: " + battLevelScaled + "%");
                    handler.post(() -> { text.setText(battLevelScaled + "%"); });

                    sleep(1000);
                }
            } catch (InterruptedException e) {}

            // Remove view now that we are shut down.
            getSystemService(WindowManager.class).removeView(text);

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

        TextView text = new TextView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        getSystemService(WindowManager.class).addView(text, params);

        // Start thread.
        thread = new BarThread(text);
        thread.start();
        return START_NOT_STICKY;
    }

    /** Stops the service by interrupting the worker thread. */
    @Override
    public void onDestroy() {
        Log.i(TAG, "Stopping service");
        running = false;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public IBinder onBind(Intent intent) { return null; } // No binding required.
}
