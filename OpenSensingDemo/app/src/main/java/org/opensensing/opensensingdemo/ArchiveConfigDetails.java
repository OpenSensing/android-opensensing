package org.opensensing.opensensingdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by arks on 10/13/15.
 */
public class ArchiveConfigDetails extends Activity implements Observer {

    private LocalFunfManager localFunfManager;

    private Button saveButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_config_details);

        localFunfManager = LocalFunfManager.getLocalFunfManager(this);
        localFunfManager.addObserver(this);
        localFunfManager.start();

        saveButton = (Button) findViewById(R.id.archiveConfigSaveDetailsButton);
        saveButton.setEnabled(false);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });


    }


    @Override
    public void update(Observable observable, Object data) {
        updateUI();
    }

    protected void onDestroy() {
        super.onDestroy();
        localFunfManager.destroy();

    }

    private void save() {

        this.finish();
    }

    private void updateUI() {

    }
}
