package org.opensensing.opensensingdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.storage.NameValueDatabaseHelper;

/**
 * Created by arks on 8/25/15.
 */
public class LocalFunfManager extends Observable implements Probe.DataListener {

    private FunfManager funfManager;
    private Context context;
    private ServiceConnection funfManagerConn;
    public static final String LOCAL_PIPELINE_NAME = "local_pipeline";
    public static final String REMOTE_PIPELINE_NAME = "remote_pipeline";
    public static final String ERROR_PIPELINE_NAME = "error_pipeline";

    private static LocalFunfManager localFunfManager = null;


    public static LocalFunfManager getLocalFunfManager(Context context) {
        localFunfManager = new LocalFunfManager(context);
        return localFunfManager;
    }

    public LocalFunfManager(Context context) {
        this.context = context;
    }

    public void start() {
        funfManagerConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                funfManager = ((FunfManager.LocalBinder) service).getManager();

                if (funfManager.isEnabled(LOCAL_PIPELINE_NAME) && funfManager.isEnabled(REMOTE_PIPELINE_NAME)) {
                    reloadPipeline();
                }

                updateUI();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                funfManager = null;
            }

        };

        this.context.bindService(new Intent(this.context, FunfManager.class), funfManagerConn, Context.BIND_AUTO_CREATE);
    }

    public void destroy() {
        this.context.unbindService(funfManagerConn);
    }

    public void requestLocalPipeline() {
        setRequestedPipelineName(LOCAL_PIPELINE_NAME);
        reloadPipeline();
    }

    public void requestRemotePipeline() {
        setRequestedPipelineName(REMOTE_PIPELINE_NAME);
        reloadPipeline();
    }

    private void setRequestedPipelineName(String name) {

        SharedPreferences sharedPreferences = this.context.getSharedPreferences(this.context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("requestedPipelineName", name);
        editor.commit();

    }

    public String getRequestedPipelineName() {
        SharedPreferences sharedPreferences = this.context.getSharedPreferences(this.context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("requestedPipelineName", "None");
        if (name.equals("None")) {
            setRequestedPipelineName(LOCAL_PIPELINE_NAME);
            return  LOCAL_PIPELINE_NAME;
        }
        return name;
    }

    private void reloadPipeline() {
        if (getRequestedPipelineName().equals(REMOTE_PIPELINE_NAME)) setRemotePipeline();
        else if (getRequestedPipelineName().equals(LOCAL_PIPELINE_NAME)) setLocalPipeline();
        updateUI();
    }

    public String getCurrentPipelineName() {
        BasicPipeline localPipeline = (BasicPipeline) funfManager.getRegisteredPipeline(LOCAL_PIPELINE_NAME);
        BasicPipeline remotePipeline = (BasicPipeline) funfManager.getRegisteredPipeline(REMOTE_PIPELINE_NAME);

        if (localPipeline.isEnabled()) return LOCAL_PIPELINE_NAME;
        if (remotePipeline.isEnabled()) return REMOTE_PIPELINE_NAME;

        return ERROR_PIPELINE_NAME;

    }

    public JsonObject getCurrentPipelineConfig() {
        return funfManager.getPipelineConfig(getCurrentPipelineName());
    }

    public List<JsonElement> getCurrentPipelineDataRequests() {
        return getCurrentPipeline().getDataRequests();
    }

    private BasicPipeline getCurrentPipeline() {
        return (BasicPipeline) funfManager.getRegisteredPipeline(getCurrentPipelineName());
    }

    public void setCurrentPipelineConfig(JsonObject config) {
        if (!(funfManager==null)) funfManager.saveAndReload(getCurrentPipelineName(), config);
    }


    public boolean collectionEnabled() {

        if (funfManager == null) return false;
        return funfManager.isEnabled(LOCAL_PIPELINE_NAME) || funfManager.isEnabled(REMOTE_PIPELINE_NAME);

    }


    public void setLocalPipeline() {
        funfManager.enablePipeline(LOCAL_PIPELINE_NAME);
        funfManager.disablePipeline(REMOTE_PIPELINE_NAME);
    }

    public void setRemotePipeline() {
        funfManager.disablePipeline(LOCAL_PIPELINE_NAME);
        funfManager.enablePipeline(REMOTE_PIPELINE_NAME);
    }

    public void enable() {
        reloadPipeline();
        updateUI();
    }

    public void disable() {
        funfManager.disablePipeline(REMOTE_PIPELINE_NAME);
        funfManager.disablePipeline(LOCAL_PIPELINE_NAME);
        updateUI();
    }

    public void upload() {
        getCurrentPipeline().onRun(BasicPipeline.ACTION_UPLOAD, null);

    }

    private void updateUI() {
        setChanged();
        notifyObservers();
    }

    public boolean isWifiOnlyUploadEnabled() {
        boolean wifiOnly = false;
        try {
            wifiOnly = getCurrentPipelineConfig().get("upload").getAsJsonObject().get("wifiOnly").getAsBoolean();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return wifiOnly;
    }

    public String getUploadUrl() {
        String url = "";
        url = getCurrentPipelineConfig().get("upload").getAsJsonObject().get("url").getAsString();
        return url;
    }

    public Double getUploadInterval() {
        Double interval = -1.0;
        try {
            interval = getCurrentPipelineConfig().get("upload").getAsJsonObject().get("@schedule").getAsJsonObject().get("interval").getAsDouble();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (interval == -1.0) {
            try {
                //TODO proper class discovery
                interval = Class.forName("edu.mit.media.funf.storage.HttpArchive").getAnnotation(edu.mit.media.funf.Schedule.DefaultSchedule.class).interval();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
        return interval;
    }

    @Override
    public void onDataReceived(IJsonObject iJsonObject, IJsonObject iJsonObject1) {

    }

    @Override
    public void onDataCompleted(IJsonObject iJsonObject, JsonElement jsonElement) {

    }

    public void archive() {
        if (getCurrentPipeline().isEnabled()) {
            getCurrentPipeline().onRun(BasicPipeline.ACTION_ARCHIVE, null);
        }
    }

    public String getInfo() {
        return getDataInfo();
    }

    public String getFunfVersion() {
        if (funfManager == null) return "unknown";
        return funfManager.getVersion();
    }

    private String getDataInfo() {
        //getDb() for both json and sqlite not implemented in funf-core yet.
        return "";
    }

    public void setAuthToken(String token) {
        String url = "";
        try {
             url = getCurrentPipelineConfig().get("upload").getAsJsonObject().get("url").getAsString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if (funfManager == null) return;
        funfManager.setAuthToken(url, token);
    }

    public List<JsonElement> getFences() {
        return getCurrentPipeline().getFences();
    }
}
