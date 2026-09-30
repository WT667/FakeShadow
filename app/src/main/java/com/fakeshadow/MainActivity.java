package com.fakeshadow;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Control panel for FakeShadow.
 * Includes top-10 Chinese university location presets.
 */
public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "fakeshadow_prefs";

    /** 中国TOP10高校预设：名称, 纬度, 经度 */
    private static final String[][] UNIVERSITIES = {
            {"北京大学",   "39.9892",  "116.3975"},
            {"清华大学",   "40.0027",  "116.3264"},
            {"复旦大学",   "31.2988",  "121.5049"},
            {"上海交大",   "31.0285",  "121.4358"},
            {"浙江大学",   "30.2681",  "120.1193"},
            {"南京大学",   "32.1195",  "118.9436"},
            {"中科大",     "31.8334",  "117.2733"},
            {"武汉大学",   "30.5419",  "114.3568"},
            {"华中科大",   "30.5100",  "114.4127"},
            {"中山大学",   "23.0975",  "113.2960"},
    };

    private SwitchMaterial swEnabled;
    private TextInputEditText etLat;
    private TextInputEditText etLng;
    private TextInputEditText etTarget;
    private MaterialButton btnSave;
    private ChipGroup chipGroup;

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
        chipGroup = findViewById(R.id.chip_universities);

        sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        loadPrefs();
        setupUniversityChips();

        btnSave.setOnClickListener(v -> savePrefs());
    }

    private void setupUniversityChips() {
        for (String[] uni : UNIVERSITIES) {
            String name = uni[0];
            String lat = uni[1];
            String lng = uni[2];

            Chip chip = new Chip(this);
            chip.setText(name);
            chip.setCheckable(true);
            chip.setOnClickListener(v -> {
                etLat.setText(lat);
                etLng.setText(lng);
                Toast.makeText(this, name + " " + lat + ", " + lng, Toast.LENGTH_SHORT).show();
            });
            chipGroup.addView(chip);
        }
    }

    private void loadPrefs() {
        swEnabled.setChecked(sp.getBoolean("enabled", false));
        etLat.setText(sp.getString("latitude", "39.9892"));
        etLng.setText(sp.getString("longitude", "116.3975"));
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
