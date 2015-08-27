package org.opensensing.opensensingdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by arks on 8/25/15.
 */
public class SettingsActivity extends Activity implements Observer {

    private RadioButton localConfigRadioButton;
    private RadioButton remoteConfigRadioButton;
    private LocalFunfManager localFunfManager;
    private TextView configTextView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        localFunfManager = new LocalFunfManager(this);
        localFunfManager.addObserver(this);
        localFunfManager.start();

        localConfigRadioButton = (RadioButton) findViewById(R.id.localConfigRadioButton);
        remoteConfigRadioButton = (RadioButton) findViewById(R.id.remoteConfigRadioButton);
        configTextView = (TextView) findViewById(R.id.configTextView);

        initUI();

        localConfigRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) localFunfManager.requestLocalPipeline();
            }
        });

        remoteConfigRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) localFunfManager.requestRemotePipeline();
            }
        });




    }

    private void initUI() {
        if (localFunfManager.getRequestedPipelineName().equals(localFunfManager.LOCAL_PIPELINE_NAME)) {
            remoteConfigRadioButton.setChecked(false);
            localConfigRadioButton.setChecked(true);
        }
        if (localFunfManager.getRequestedPipelineName().equals(localFunfManager.REMOTE_PIPELINE_NAME)) {
            remoteConfigRadioButton.setChecked(true);
            localConfigRadioButton.setChecked(false);
        }
    }

    private void updateUI() {
        configTextView.setText(localFunfManager.getCurrentPipelineConfig().toString());
    }

    protected void onDestroy() {
        super.onDestroy();
        localFunfManager.destroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        updateUI();
    }
}
