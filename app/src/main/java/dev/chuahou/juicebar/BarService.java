package dev.chuahou.juicebar;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BarService extends Service {

    /** Tag for logging. */
    private static final String TAG = "juicebar.BarService";

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

    /** Worker thread. */
    private BarThread thread = null;

    /** Starts the service by starting the worker thread. */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service");
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
