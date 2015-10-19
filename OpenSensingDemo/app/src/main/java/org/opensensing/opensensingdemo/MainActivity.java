package org.opensensing.opensensingdemo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends Activity implements Observer {


    public static final String TAG = "OPEN_SENSING_DEMO";

    private Button archiveButton;
    private Button uploadButton;
    private Button getInfoButton;
    private Handler handler;
    private LocalFunfManager localFunfManager;
    private Switch enabledSwitch;
    private View.OnClickListener getInfoButtonListener;
    private CompoundButton.OnCheckedChangeListener enabledSwitchListener;
    private TextView pipelineInUseTextView;
    private TextView infoTextView;

    public static boolean isActive;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isActive = false;

        localFunfManager = LocalFunfManager.getLocalFunfManager(this);
        localFunfManager.addObserver(this);

        infoTextView = (TextView) findViewById(R.id.infoTextView);

        // Used to make interface changes on main thread
        handler = new Handler();

        archiveButton = (Button) findViewById(R.id.archiveButton);
        archiveButton.setEnabled(true);
        archiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localFunfManager.archive();
            }
        });

        uploadButton = (Button) findViewById(R.id.uploadButton);
        uploadButton.setEnabled(true);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localFunfManager.upload();


            }
        });


        getInfoButton = (Button) findViewById(R.id.getInfoButton);
        getInfoButton.setEnabled(false);
        getInfoButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInfo();
            }
        };
        getInfoButton.setOnClickListener(getInfoButtonListener);

        enabledSwitch = (Switch) findViewById(R.id.enabledSwitch);
        enabledSwitch.setEnabled(false);
        enabledSwitchListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (localFunfManager == null) return;
                if (isChecked) localFunfManager.enable();
                else localFunfManager.disable();
            }
        };
        enabledSwitch.setOnCheckedChangeListener(enabledSwitchListener);

        pipelineInUseTextView = (TextView) findViewById(R.id.pipelineInUseTextView);

        localFunfManager.start();

    }

    protected void onResume() {
        super.onResume();
        isActive = true;
        updateUI();

    }

    protected void onPause() {
        super.onPause();
        isActive = false;
    }

    protected void onDestroy() {
        super.onDestroy();
        localFunfManager.destroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_auth) {
            Intent intent = new Intent(this, AuthenticationActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_geofences) {
            Intent intent = new Intent(this, GeofencesActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void getInfo() {
        infoTextView.setText(localFunfManager.getInfo());
    }

    private void updateUI() {
        if (localFunfManager == null) return;
        enabledSwitch.setEnabled(true);
        ((TextView) findViewById(R.id.versionTextView)).setText("collector: " + getVersionName() + "\n" + "funf: " + localFunfManager.getFunfVersion());
        if (localFunfManager.collectionEnabled()) {

            enabledSwitch.setOnCheckedChangeListener(null);
            enabledSwitch.setChecked(true);
            enabledSwitch.setOnCheckedChangeListener(enabledSwitchListener);
            enabledSwitch.setText("Collection enabled");

            getInfoButton.setEnabled(true);
            uploadButton.setEnabled(true);

            pipelineInUseTextView.setText(localFunfManager.getCurrentPipelineName());
        }
        else {
            enabledSwitch.setOnCheckedChangeListener(null);
            enabledSwitch.setChecked(false);
            enabledSwitch.setOnCheckedChangeListener(enabledSwitchListener);
            enabledSwitch.setText("Collection disabled");

            getInfoButton.setEnabled(false);
            uploadButton.setEnabled(false);

            pipelineInUseTextView.setText("");
        }

    }

    @Override
    public void update(Observable observable, Object data) {
        updateUI();
    }

    private String getVersionName() {
        String versionName = "";

        try {
            versionName = getPackageManager().getPackageInfo("org.opensensing.opensensingdemo", 0).versionName;

        } catch (PackageManager.NameNotFoundException e) {

        }

        return versionName;

    }
}
