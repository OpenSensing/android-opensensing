package org.opensensing.opensensingdemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by arks on 8/25/15.
 */
public class SettingsActivity extends Activity implements Observer {

    private RadioButton localConfigRadioButton;
    private RadioButton remoteConfigRadioButton;
    private LocalFunfManager localFunfManager;
    private ExpandableListView configExpandableListView;
    private MyExpandableListAdapter adapter;
    private Button saveConfigButton;
    private Boolean probeHasBeenToggled;
    private Button uploadConfigButton;

    private int expandedGroup;

    SparseArray<Group> groups = new SparseArray<Group>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ProbeConfigDetails.settingsActivity = this;

        probeHasBeenToggled = false;
        expandedGroup = -1;

        localFunfManager = LocalFunfManager.getLocalFunfManager(this);
        localFunfManager.addObserver(this);
        localFunfManager.start();

        localConfigRadioButton = (RadioButton) findViewById(R.id.localConfigRadioButton);
        remoteConfigRadioButton = (RadioButton) findViewById(R.id.remoteConfigRadioButton);

        saveConfigButton = (Button) findViewById(R.id.saveConfigButton);
        saveConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
            }
        });

        configExpandableListView = (ExpandableListView) findViewById(R.id.configExpandableListView);
        configExpandableListView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        uploadConfigButton = (Button) findViewById(R.id.uploadConfigButton);
        uploadConfigButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SettingsActivity.this, UploadConfigDetails.class);
                SettingsActivity.this.startActivity(intent);
            }
        });

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


    public void pingUIUpdate() {
        configExpandableListView.setAdapter(adapter);
        configExpandableListView.expandGroup(expandedGroup);
        probeToggled();
    }

    private void initUI() {

    }

    private void updateUI() {
        if (localFunfManager == null) return;
        if (localFunfManager.getCurrentPipelineName().equals(localFunfManager.LOCAL_PIPELINE_NAME)) {
            remoteConfigRadioButton.setChecked(false);
            localConfigRadioButton.setChecked(true);
        }
        if (localFunfManager.getCurrentPipelineName().equals(localFunfManager.REMOTE_PIPELINE_NAME)) {
            remoteConfigRadioButton.setChecked(true);
            localConfigRadioButton.setChecked(false);
        }
        renderCurrentPipelineConfig();
    }

    protected void onDestroy() {
        super.onDestroy();
        localFunfManager.destroy();
    }

    public void launchProbeConfigDetails(Group group, Integer expandedGroup) {
        this.expandedGroup = expandedGroup;
        Intent intent = new Intent(this, ProbeConfigDetails.class);
        intent.putExtra("group.fullName", group.fullName);
        startActivity(intent);

    }

    @Override
    public void update(Observable observable, Object data) {
        updateUI();
    }

    private void renderCurrentPipelineConfig() {

        if (probeHasBeenToggled) this.saveConfigButton.setEnabled(true);
        else this.saveConfigButton.setEnabled(false);


        groups = new SparseArray<Group>();

        JsonParser jsonParser = new JsonParser();
        JsonObject fullConfig = jsonParser.parse(getString(R.string.full_config)).getAsJsonObject();

        LinkedList<JsonElement> fullConfigProbes = new LinkedList<JsonElement>();

        for (JsonElement v: fullConfig.get("data").getAsJsonArray()) {
            fullConfigProbes.add(v);
        }


        Integer jj = 0;

        HashMap<String,ConfigEntry> activeProbes = new HashMap<String, ConfigEntry>();
        for (JsonElement element:localFunfManager.getCurrentPipelineDataRequests()) {
            ConfigEntry configEntry = buildConfigEntry(element);
            activeProbes.put(configEntry.name, configEntry);
        }

        for (JsonElement element:fullConfigProbes) {

            if (element.toString().equals("null")) continue;

            ConfigEntry fullConfigEntry = buildConfigEntry(element);

            ConfigEntry configEntry = mergeConfigEntry(activeProbes.get(fullConfigEntry.name), fullConfigEntry);


            Group group = new Group(configEntry.name, configEntry.active);
            group.addChild("interval", configEntry.interval);

            group.addChild("duration", configEntry.duration);
            group.addChild("opportunistic", configEntry.opportunistic);
            group.addChild("strict", configEntry.strict);
            //group.addChild("active", configEntry.active);


            groups.append(jj, group);
            jj += 1;

        }
        adapter = new MyExpandableListAdapter(this, groups);
        adapter.setSettingsActivity(this);
        configExpandableListView.setAdapter(adapter);
    }

    private ConfigEntry mergeConfigEntry(ConfigEntry activeConfigEntry, ConfigEntry fullConfigEntry) {
        if (activeConfigEntry == null) return fullConfigEntry;
        activeConfigEntry.active = true;


        return activeConfigEntry;

    }

    private ConfigEntry buildConfigEntry(JsonElement element) {

        if (element.isJsonPrimitive()) {
            ConfigEntry configEntry = new ConfigEntry(element.getAsString());

            return configEntry;
        }
        else {
            ConfigEntry configEntry = new ConfigEntry(element.getAsJsonObject().get("@type").getAsString());

            try { configEntry.interval = element.getAsJsonObject().get("@schedule").getAsJsonObject().get("interval").getAsDouble();}
            catch (java.lang.NullPointerException e) {}

            try{ configEntry.duration = element.getAsJsonObject().get("@schedule").getAsJsonObject().get("duration").getAsDouble();}
            catch (java.lang.NullPointerException e) {}

            try{ configEntry.opportunistic = element.getAsJsonObject().get("@schedule").getAsJsonObject().get("opportunistic").getAsBoolean();}
            catch (java.lang.NullPointerException e) {}

            try{ configEntry.strict = element.getAsJsonObject().get("@schedule").getAsJsonObject().get("strict").getAsBoolean();}
            catch (java.lang.NullPointerException e) {}


            return configEntry;

        }

    }

    public void probeToggled() {

        this.saveConfigButton.setEnabled(true);

    }

    public Group getGroupByFullName(String fullName) {

        for (int i=0; i<groups.size(); ++i) {
            Group group = groups.get(i);
            if (group.fullName.equals(fullName)) return group;
        }
        return null;
    }

    private void saveConfig() {

        buildConfig();
        probeHasBeenToggled = false;
        this.saveConfigButton.setEnabled(false);
    }

    //TODO move this to LocalFunfManager
    //TODO disable probes for which we don't have permissions
    private void buildConfig() {

        //List<JsonElement> requestedConfig = new ArrayList<JsonElement>();
        JsonArray requestedConfig = new JsonArray();

        SparseArray<Group> groups = adapter.getGroups();
        for (int i=0; i<groups.size(); ++i) {
            Group group = groups.get(i);


            ConfigEntry configEntry = new ConfigEntry(group.fullName);
            configEntry.interval = group.children.get("interval").getAsDouble();
            configEntry.duration = group.children.get("duration").getAsDouble();
            configEntry.strict = group.children.get("strict").getAsBoolean();
            configEntry.opportunistic = group.children.get("opportunistic").getAsBoolean();

            if (!group.active) continue;

            requestedConfig.add(configEntry.toJsonObject());



        }

        JsonObject currentPipelineConfig = localFunfManager.getCurrentPipelineConfig();
        currentPipelineConfig.add("data", requestedConfig);

        Log.i(MainActivity.TAG, localFunfManager.getCurrentPipelineConfig().toString());
        Log.i(MainActivity.TAG, ".... " + currentPipelineConfig.toString());
        localFunfManager.setCurrentPipelineConfig(currentPipelineConfig);
    }


    class ConfigEntry {
        //TODO getters and setters
        public String name;
        public Double interval;
        public Double duration;
        public Boolean strict;
        public Boolean opportunistic;
        public Boolean active;

        public ConfigEntry(String name) {
            //TODO introspection on the fields declared as @Configurable
            this.name = name;
            this.interval = getDefaultInterval();
            this.duration = getDefaultDuration();
            this.strict = getDefaultStrict();
            this.opportunistic = getDefaultOpportunistic();
            this.active = false;
        }

        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("@type", this.name);

            JsonObject schedule = new JsonObject();
            schedule.addProperty("interval", this.interval);
            schedule.addProperty("duration", this.duration);
            schedule.addProperty("strict", this.strict);
            schedule.addProperty("opportunistic", this.opportunistic);

            jsonObject.add("@schedule", schedule);

            return jsonObject;
        }

        private Double getDefaultInterval() {
            Double interval = -1.0;
            try {
                interval = Class.forName(this.name).getAnnotation(edu.mit.media.funf.Schedule.DefaultSchedule.class).interval();
            } catch (ClassNotFoundException e) {}

            return interval;
        }

        private Double getDefaultDuration() {
            Double duration = -1.0;
            try {
                duration = Class.forName(this.name).getAnnotation(edu.mit.media.funf.Schedule.DefaultSchedule.class).duration();
            } catch (ClassNotFoundException e) {}

            return duration;
        }

        private Boolean getDefaultStrict() {
            Boolean strict = false;
            try {
                strict = Class.forName(this.name).getAnnotation(edu.mit.media.funf.Schedule.DefaultSchedule.class).strict();
            } catch (ClassNotFoundException e) {}

            return strict;
        }

        private Boolean getDefaultOpportunistic() {
            Boolean opportunistic = false;
            try {
                opportunistic = Class.forName(this.name).getAnnotation(edu.mit.media.funf.Schedule.DefaultSchedule.class).opportunistic();
            } catch (ClassNotFoundException e) {}

            return opportunistic;
        }

    }

}
