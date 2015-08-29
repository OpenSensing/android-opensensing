package org.opensensing.opensensingdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;


/**
 * Created by arks on 8/29/15.
 */
public class ProbeConfigDetails extends Activity {

    public static SettingsActivity settingsActivity;

    private Group group;

    private TextView probeNameTextView;
    private TextView probeFullNameTextView;
    private TextView intervalLabelTextView;
    private EditText intervalEditText;
    private TextView durationLabelTextView;
    private EditText durationEditText;
    private TextView strictLabelTextView;
    private Switch strictSwitch;
    private TextView opportunisticLabelTextView;
    private Switch opportunisticSwitch;

    private Button saveButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_probe_config_details);
        Intent intent = getIntent();

        probeNameTextView = (TextView) findViewById(R.id.probeNameTextView);
        probeFullNameTextView = (TextView) findViewById(R.id.probeFullNameTextView);
        intervalLabelTextView = (TextView) findViewById(R.id.intervalLabelTextView);
        intervalEditText = (EditText) findViewById(R.id.intervalEditText);
        durationLabelTextView = (TextView) findViewById(R.id.durationLabelTextView);
        durationEditText = (EditText) findViewById(R.id.durationEditText);
        strictLabelTextView = (TextView) findViewById(R.id.strictLabelTextView);
        strictSwitch = (Switch) findViewById(R.id.strictSwitch);
        opportunisticLabelTextView = (TextView) findViewById(R.id.opportunisticLabelTextView);
        opportunisticSwitch = (Switch) findViewById(R.id.opportunisticSwitch);

        saveButton = (Button) findViewById(R.id.saveProbeConfigDetailsButton);
        saveButton.setEnabled(false);


        buildUI(intent);

    }

    private void buildUI(Intent intent) {
        if (settingsActivity==null) return;
        group = settingsActivity.getGroupByFullName(intent.getStringExtra("group.fullName"));


        probeNameTextView.setText(group.string);
        probeFullNameTextView.setText(group.fullName);

        intervalLabelTextView.setText("interval");
        intervalEditText.setText(group.children.get("interval").getValue());
        intervalEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setEnabled(true);
            }
        });

        durationLabelTextView.setText("duration");
        durationEditText.setText(group.children.get("duration").getValue());
        durationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveButton.setEnabled(true);
            }
        });

        strictLabelTextView.setText("strict");
        strictSwitch.setChecked(group.children.get("strict").getAsBoolean());
        strictSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveButton.setEnabled(true);
            }
        });

        opportunisticLabelTextView.setText("opportunistic");
        opportunisticSwitch.setChecked(group.children.get("opportunistic").getAsBoolean());
        opportunisticSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                saveButton.setEnabled(true);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

    }

    private void save() {
        if (settingsActivity == null) return;
        updateValue("interval", this.intervalEditText.getText().toString());
        updateValue("duration", this.durationEditText.getText().toString());
        updateValue("strict", new Boolean(this.strictSwitch.isChecked()).toString());
        updateValue("opportunistic", new Boolean(this.opportunisticSwitch.isChecked()).toString());
        settingsActivity.pingUIUpdate();
        this.finish();

    }

    private void updateValue(String key, String value) {
        group.children.get(key).setValue(value);
    }

}
