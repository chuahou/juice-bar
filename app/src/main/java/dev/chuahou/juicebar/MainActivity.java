package dev.chuahou.juicebar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    /** Tag for logging. */
    private static final String TAG = "juicebar.MainActivity";

    /** Main service on/off switch. */
    private Switch serviceSwitch;

    /** Result code for asking for permissions. */
    private static final int permsRequestCode = 19875;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serviceSwitch = findViewById(R.id.serviceSwitch);

        // Handle switch being switched on/off.
        serviceSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            assert(compoundButton == serviceSwitch);
            if (serviceSwitch.isChecked()) {
                Log.i(TAG, "Starting service");

                // Get permissions to draw if necessary.
                Log.i(TAG, "Getting permissions to draw");
                if (!Settings.canDrawOverlays(this)) {
                    Intent permsIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(permsIntent, permsRequestCode);
                } else {
                    startService(); // Start service directly otherwise.
                }
            } else {
                Log.i(TAG, "Stopping service");
                stopService(new Intent(this, BarService.class));
            }
        });
    }

    /** Start bar service. */
    private void startService() {
        startForegroundService(new Intent(this, BarService.class));
    }

    /** Get result from requesting permission, start bar service if we have the requisite
      * permissions. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == permsRequestCode) {
            if (Settings.canDrawOverlays(this))
                startService();
            else // Didn't get perms, turn off switch.
                serviceSwitch.setChecked(false);
        }
    }
}
