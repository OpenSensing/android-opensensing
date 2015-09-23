package org.opensensing.opensensingdemo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import edu.mit.media.funf.FunfManager;
import edu.mit.media.funf.json.IJsonObject;
import edu.mit.media.funf.pipeline.BasicPipeline;
import edu.mit.media.funf.probe.Probe;
import edu.mit.media.funf.probe.builtin.SimpleLocationProbe;
import edu.mit.media.funf.probe.builtin.WifiProbe;
import edu.mit.media.funf.storage.HttpArchive;
import edu.mit.media.funf.storage.NameValueDatabaseHelper;

/**
 * Created by arks on 8/25/15.
 */
public class LocalFunfManager extends Observable implements Probe.DataListener {

    private FunfManager funfManager;
    private WifiProbe wifiProbe;
    private SimpleLocationProbe locationProbe;
    private Context context;
    private ServiceConnection funfManagerConn;
    public static final String LOCAL_PIPELINE_NAME = "local_pipeline";
    public static final String REMOTE_PIPELINE_NAME = "remote_pipeline";
    public static final String ERROR_PIPELINE_NAME = "error_pipeline";

    private BasicPipeline localPipeline;
    private BasicPipeline remotePipeline;




    public LocalFunfManager(Context context) {
        this.context = context;
    }

    public void start() {
        funfManagerConn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                funfManager = ((FunfManager.LocalBinder) service).getManager();



                Gson gson = funfManager.getGson();
                wifiProbe = gson.fromJson(new JsonObject(), WifiProbe.class);
                locationProbe = gson.fromJson(new JsonObject(), SimpleLocationProbe.class);
                //wifiProbe.registerPassiveListener(LocalFunfManager.this);
                //locationProbe.registerPassiveListener(LocalFunfManager.this);

                Log.i(MainActivity.TAG, "running funf "+getFunfVersion());




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
        Log.i(MainActivity.TAG, "private void reloadPipeline() " + getRequestedPipelineName());
        if (getRequestedPipelineName().equals(REMOTE_PIPELINE_NAME)) setRemotePipeline();
        else if (getRequestedPipelineName().equals(LOCAL_PIPELINE_NAME)) setLocalPipeline();
        updateUI();
    }

    public String getCurrentPipelineName() {
        localPipeline = (BasicPipeline) funfManager.getRegisteredPipeline(LOCAL_PIPELINE_NAME);
        remotePipeline = (BasicPipeline) funfManager.getRegisteredPipeline(REMOTE_PIPELINE_NAME);
        if (remotePipeline.isEnabled()) return REMOTE_PIPELINE_NAME;
        if (localPipeline.isEnabled()) return LOCAL_PIPELINE_NAME;

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
       // getCurrentPipeline().setDataRequests(config);
        //getCurrentPipeline().setDataRequests(((BasicPipeline)funfManager.getRegisteredPipeline(LOCAL_PIPELINE_NAME)).getDataRequests());
        //BasicPipeline pipeline = ((BasicPipeline) funfManager.getRegisteredPipeline(REMOTE_PIPELINE_NAME));
        //pipeline.setDataRequests(((BasicPipeline)funfManager.getRegisteredPipeline(LOCAL_PIPELINE_NAME)).getDataRequests());
        //reloadPipeline();
        //funfManager.unrequestAllData(this);
        if (!(funfManager==null)) funfManager.saveAndReload(getCurrentPipelineName(), config);
        //Log.i(MainActivity.TAG, ">>> " + getCurrentPipelineConfig().toString());
    }


    public boolean collectionEnabled() {

        if (funfManager == null) return false;
        Log.i(MainActivity.TAG, ""+funfManager.isEnabled(LOCAL_PIPELINE_NAME));
        Log.i(MainActivity.TAG, ""+funfManager.isEnabled(REMOTE_PIPELINE_NAME));
        return funfManager.isEnabled(LOCAL_PIPELINE_NAME) || funfManager.isEnabled(REMOTE_PIPELINE_NAME);

    }


    public void setLocalPipeline() {
        Log.i(MainActivity.TAG, "setting local pipeline");
        funfManager.enablePipeline(LOCAL_PIPELINE_NAME);
        funfManager.disablePipeline(REMOTE_PIPELINE_NAME);
    }

    public void setRemotePipeline() {
        Log.i(MainActivity.TAG, "setting remote pipeline");
        funfManager.disablePipeline(LOCAL_PIPELINE_NAME);
        funfManager.enablePipeline(REMOTE_PIPELINE_NAME);
    }

    public void enable() {
        Log.i(MainActivity.TAG, "public void enable()");
        reloadPipeline();
        updateUI();
    }

    public void disable() {
        funfManager.disablePipeline(REMOTE_PIPELINE_NAME);
        funfManager.disablePipeline(LOCAL_PIPELINE_NAME);
        updateUI();
    }

    public void upload() {
        //getCurrentPipeline().setUpload(new HttpArchive(this.context, "http://raman.imm.dtu.dk"));
        Log.i(MainActivity.TAG, "ARCHIVE: " + getCurrentPipeline().getUpload().toString());
        getCurrentPipeline().onRun(BasicPipeline.ACTION_UPLOAD, null);

    }

    private void updateUI() {
        setChanged();
        notifyObservers();
    }

    @Override
    public void onDataReceived(IJsonObject iJsonObject, IJsonObject iJsonObject1) {

    }

    @Override
    public void onDataCompleted(IJsonObject iJsonObject, JsonElement jsonElement) {
        //wifiProbe.registerPassiveListener(this);
        //locationProbe.registerPassiveListener(this);

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
        SQLiteDatabase db = getCurrentPipeline().getDb();
        Cursor mcursor = db.rawQuery("SELECT name FROM " + NameValueDatabaseHelper.DATA_TABLE.name, null);
        mcursor.moveToFirst();

        HashMap<String, Integer> dataCount = new HashMap<String, Integer>();

        while (!mcursor.isAfterLast()) {
            String name = mcursor.getString(0);
            if (dataCount.containsKey(name)) {
                dataCount.put(name, dataCount.get(name) +1);
            }
            else {
                dataCount.put(name, 1);
            }
            //Log.i(TAG, name);
            mcursor.moveToNext();
        }

        Log.i(MainActivity.TAG, dataCount.toString());
        mcursor.close();
        return dataCount.toString();
    }
}
