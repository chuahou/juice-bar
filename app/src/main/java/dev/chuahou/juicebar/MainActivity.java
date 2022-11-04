package dev.chuahou.juicebar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    /** Tag for logging. */
    private static final String TAG = "juicebar.MainActivity";

    /** Main service on/off switch. */
    private Switch serviceSwitch;

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
                startService(new Intent(this, BarService.class));
            } else {
                Log.i(TAG, "Stopping service");
                stopService(new Intent(this, BarService.class));
            }
        });
    }
}
