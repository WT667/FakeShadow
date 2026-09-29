package com.fakeshadow;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Control panel for FakeShadow.  Writes settings to
 * {@code fakeshadow_prefs} with MODE_WORLD_READABLE so the Xposed hook
 * (running inside other apps' processes) can read them via XSharedPreferences.
 */
public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "fakeshadow_prefs";

    private SwitchMaterial swEnabled;
    private TextInputEditText etLat;
    private TextInputEditText etLng;
    private TextInputEditText etTarget;
    private MaterialButton btnSave;

    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swEnabled = findViewById(R.id.sw_enabled);
        etLat = findViewById(R.id.et_lat);
        etLng = findViewById(R.id.et_lng);
        etTarget = findViewById(R.id.et_target);
        btnSave = findViewById(R.id.btn_save);

        // MODE_WORLD_READABLE lets the hook process read our prefs.
        // LSPosed grants this automatically when the module is activated.
        sp = getSharedPreferences(PREFS, MODE_WORLD_READABLE);

        loadPrefs();

        btnSave.setOnClickListener(v -> savePrefs());
    }

    private void loadPrefs() {
        swEnabled.setChecked(sp.getBoolean("enabled", false));
        etLat.setText(sp.getString("latitude", "34.052235"));
        etLng.setText(sp.getString("longitude", "-118.243683"));
        etTarget.setText(sp.getString("target_package", ""));
    }

    private void savePrefs() {
        String lat = etLat.getText().toString().trim();
        String lng = etLng.getText().toString().trim();

        if (!isValidLat(lat) || !isValidLng(lng)) {
            Toast.makeText(this, R.string.toast_invalid_coords, Toast.LENGTH_SHORT).show();
            return;
        }

        sp.edit()
                .putBoolean("enabled", swEnabled.isChecked())
                .putString("latitude", lat)
                .putString("longitude", lng)
                .putString("target_package", etTarget.getText().toString().trim())
                .apply();

        Toast.makeText(this, swEnabled.isChecked()
                ? R.string.toast_saved_on : R.string.toast_saved_off,
                Toast.LENGTH_SHORT).show();
    }

    private boolean isValidLat(String s) {
        try {
            double v = Double.parseDouble(s);
            return v >= -90 && v <= 90;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidLng(String s) {
        try {
            double v = Double.parseDouble(s);
            return v >= -180 && v <= 180;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
