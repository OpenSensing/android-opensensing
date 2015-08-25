package org.opensensing.opensensingdemo;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.CompoundButton;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.builtin.SimpleLocationProbe;
import edu.mit.media.funf.probe.builtin.WifiProbe;

/**
 * Created by arks on 8/25/15.
 */
public class LocalFunfManager {

    private BasicPipeline pipeline;

    public LocalFunfManager() {
        private ServiceConnection funfManagerConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                funfManager = ((FunfManager.LocalBinder) service).getManager();



                Gson gson = funfManager.getGson();
                wifiProbe = gson.fromJson(new JsonObject(), WifiProbe.class);
                locationProbe = gson.fromJson(new JsonObject(), SimpleLocationProbe.class);
                pipeline = (BasicPipeline) funfManager.getRegisteredPipeline(PIPELINE_NAME);
                wifiProbe.registerPassiveListener(MainActivity.this);
                locationProbe.registerPassiveListener(MainActivity.this);

                // This checkbox enables or disables the pipeline
                enabledCheckbox.setChecked(pipeline.isEnabled());
                enabledCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (funfManager != null) {
                            if (isChecked) {
                                funfManager.enablePipeline(PIPELINE_NAME);

                                pipeline = (BasicPipeline) funfManager.getRegisteredPipeline(PIPELINE_NAME);


                                JsonParser parser = new JsonParser();
                                JsonObject testConfig = parser.parse(getString(R.string.test_config)).getAsJsonObject();



                                boolean success = funfManager.saveAndReload(PIPELINE_NAME, testConfig);
                                funfManager.enablePipeline(PIPELINE_NAME);


                                Log.i(TAG, testConfig.toString());
                                Log.i(TAG, ""+success);



                            } else {
                                funfManager.disablePipeline(PIPELINE_NAME);
                            }
                        }
                    }
                });

                // Set UI ready to use, by enabling buttons
                enabledCheckbox.setEnabled(true);
                archiveButton.setEnabled(true);
                scanNowButton.setEnabled(true);
                getInfoButton.setEnabled(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                funfManager = null;
            }


        };
    }

    public boolean localSettingsEnabled() {
        return true;
    }

    public void setLocalSettings() {

    }

    public void setRemoteSettings() {

    }

}
