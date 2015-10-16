package org.opensensing.opensensingdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import com.google.gson.JsonObject;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by arks on 10/12/15.
 */
public class UploadConfigDetails extends Activity implements Observer {

    private LocalFunfManager localFunfManager;

    private Switch wifiOnlySwitch;
    private Button saveButton;
    private EditText urlEditText;
    private EditText intervalEditText;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_confing_details);

        localFunfManager = LocalFunfManager.getLocalFunfManager(this);
        localFunfManager.addObserver(this);
        localFunfManager.start();

        saveButton = (Button) findViewById(R.id.uploadConfigSaveDetailsButton);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        wifiOnlySwitch = (Switch) findViewById(R.id.uploadConfigWifiOnlySwitch);


        urlEditText = (EditText) findViewById(R.id.uploadConfigURLEditText);


        intervalEditText = (EditText) findViewById(R.id.uploadConfigIntervalEditText);


    }


    private void updateUI() {
        if (localFunfManager == null) return;

        wifiOnlySwitch.setOnCheckedChangeListener(null);
        urlEditText.setOnClickListener(null);
        intervalEditText.setOnClickListener(null);

        wifiOnlySwitch.setChecked(localFunfManager.isWifiOnlyUploadEnabled());

        urlEditText.setText(localFunfManager.getUploadUrl());

        intervalEditText.setText(localFunfManager.getUploadInterval().toString());


        wifiOnlySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveButton.setEnabled(true);
            }
        });

        urlEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setEnabled(true);
            }
        });

        intervalEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setEnabled(true);
            }
        });

    }

    protected void onDestroy() {
        super.onDestroy();
        localFunfManager.destroy();
    }

    public void update(Observable observable, Object data) {
        updateUI();
    }

    private void save() {
        JsonObject currentPipelineConfig = localFunfManager.getCurrentPipelineConfig();
        JsonObject requestedUploadConfig = new JsonObject();
        String url =  urlEditText.getText().toString();
        if (!url.startsWith("http")) url = "http://" + url;
        requestedUploadConfig.addProperty("url", url);
        requestedUploadConfig.addProperty("wifiOnly", wifiOnlySwitch.isChecked());
        JsonObject schedule = new JsonObject();

        Double interval = Double.valueOf(intervalEditText.getText().toString());
        schedule.addProperty("interval",interval);
        requestedUploadConfig.add("@schedule", schedule);


        currentPipelineConfig.add("upload", requestedUploadConfig);


        localFunfManager.setCurrentPipelineConfig(currentPipelineConfig);

        this.finish();
    }
}


