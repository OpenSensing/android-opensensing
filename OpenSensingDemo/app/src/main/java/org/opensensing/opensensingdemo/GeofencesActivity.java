package org.opensensing.opensensingdemo;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by arks on 10/17/15.
 */
public class GeofencesActivity extends FragmentActivity implements Observer{

    private GoogleMap mMap;
    private LocalFunfManager localFunfManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence);

        localFunfManager = LocalFunfManager.getLocalFunfManager(this);
        localFunfManager.addObserver(this);
        localFunfManager.start();

        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
            }
        }
    }

    private void setUpMap() {

        if (mMap == null) return;

        if (localFunfManager.getFences().size() == 0) {
            Context context = getApplicationContext();
            CharSequence text = "No GeoFences defined, collection is unbounded";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.show();
            finish();
        }


        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                if (location != null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16));
                    mMap.setOnMyLocationChangeListener(null);
                }
            }
        });




        for (JsonElement fence: localFunfManager.getFences()) {
            Log.i(MainActivity.TAG, fence.toString());
            Double latitude = fence.getAsJsonObject().get("latitude").getAsDouble();
            Double longitude = fence.getAsJsonObject().get("longitude").getAsDouble();
            Double radius = fence.getAsJsonObject().get("radius").getAsDouble();

            mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
            mMap.addCircle(new CircleOptions().center(new LatLng(latitude, longitude)).radius(radius).strokeWidth(2).strokeColor(Color.RED).fillColor(Color.argb(100, 255, 0, 0)));



        }



    }

    protected void onDestroy() {
        super.onDestroy();
        localFunfManager.destroy();
    }

    public void update(Observable observable, Object data) {
        setUpMap();
    }

}
