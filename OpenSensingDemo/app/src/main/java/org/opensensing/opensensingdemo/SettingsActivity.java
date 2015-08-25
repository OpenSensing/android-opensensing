package org.opensensing.opensensingdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * Created by arks on 8/25/15.
 */
public class SettingsActivity extends Activity {

    private CheckBox localConfigCheckBox;
    private LocalFunfManager localFunfManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        localFunfManager = new LocalFunfManager();

        localConfigCheckBox = (CheckBox) findViewById(R.id.localConfigCheckBox);
        if (localFunfManager.localSettingsEnabled()) localConfigCheckBox.setChecked(true);
        else localConfigCheckBox.setChecked(false);
        localConfigCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    localFunfManager.setLocalSettings();
                }
                else {
                    localFunfManager.setRemoteSettings();
                }
            }
        });
    }

}
