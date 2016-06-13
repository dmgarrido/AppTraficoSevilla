package dgf.android.apptraficosevilla.trafficMap;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

import dgf.android.apptraficosevilla.Permission;
import dgf.android.apptraficosevilla.R;
import dgf.android.apptraficosevilla.settings.SettingsActivity;
import dgf.android.apptraficosevilla.Utility;
import dgf.android.apptraficosevilla.twitter.TwitterTrafficActivity;

public class TrafficMapFragment extends SupportMapFragment implements OnMapReadyCallback
        , ActivityCompat.OnRequestPermissionsResultCallback {


    private final int MY_LOCATION_REQUEST_CODE = 1;
    private final int LOCATION_MIN_DISTANCE_INTERVAL = 0; //metros
    private final int LOCATION_MIN_TIME_INTERVAL = 30 * 1000; //milisegundos

    private GoogleMap mMap;
    private Context mContext;

    private String mPrefMarkers;
    private boolean mPrefLocationEnabled;
    private int mPrefMaxDistance;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mNewLocation;

    private List<TrafficItem> mTrafficItemList;
    private ClusterManager<TrafficItem> mClusterManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v("Trafico", "onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        mContext = getActivity();

        //Obtengo la preferencia inicial de los marcadores
        mPrefMarkers = Utility.getPreferredMarkers(mContext);
        //Obtengo la preferencia inicial de la localización
        mPrefLocationEnabled = Utility.getPreferredLocationEnabled(mContext);
        //Obtengo la preferencia inicial de la distancia
        mPrefMaxDistance = Utility.getPreferredMaxDistance(mContext);

        // Acquire a reference to the system Location Manager
        mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        mLocationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                mNewLocation = location;
                //Toast.makeText(getActivity(), "Location change", Toast.LENGTH_SHORT).show();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        if (mPrefLocationEnabled && Permission.isLocationPermissionGranted(mContext)) {
            // Register the listener with the Location Manager to receive location updates
            //noinspection ResourceType
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_MIN_TIME_INTERVAL,
                    LOCATION_MIN_DISTANCE_INTERVAL,
                    mLocationListener);
            //noinspection ResourceType
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_MIN_TIME_INTERVAL,
                    LOCATION_MIN_DISTANCE_INTERVAL,
                    mLocationListener);
        }

        getMapAsync(this);
    }

    @Override
    public void onDestroy() {
        Log.v("Trafico", "onDestroy");
        super.onDestroy();
        disableLocation();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.v("Trafico", "onMapReady");
        mMap = googleMap;

        initializeMap();

        if (Utility.getPreferredLocationEnabled(mContext) && Permission.isLocationPermissionGranted(mContext)) {
            //noinspection ResourceType
            mMap.setMyLocationEnabled(true);
            updateMap(getNewLocation());
        } else {
            //Si no está activada localización, tenemos que actualizar el mapa al inicio
            //ya que onConnect no se ejecuta
            updateMap(null);
        }

    }

    private Location getNewLocation() {
        Location newLocation;

        if (mNewLocation == null) {
            //noinspection ResourceType
            newLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (newLocation == null) {
                //noinspection ResourceType
                newLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
        } else {
            newLocation = mNewLocation;
        }
        return newLocation;
    }

    private void initializeMap() {
        //Movemos la cámara a Sevilla y aplicamos zoom
        LatLng latLongSevilla = new LatLng(37.392529, -5.994072);
        float zoom = 13;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLongSevilla, zoom));
    }

    private void updateMap(Location location) {
        Log.v("Trafico", "updateMap");

        mMap.clear();

        final String pref_markers = Utility.getPreferredMarkers(mContext);
        final int pref_maxDistance = Utility.getPreferredMaxDistance(mContext);

        //TODO: Comprobar si hay conexión a internet

        if (Utility.getPreferredLocationEnabled(mContext) && Permission.isLocationPermissionGranted(mContext)) {
            //enableLocation();
            Log.v("Trafico", "con location not null");
            new UpdateMapTask(getActivity(), mMap, pref_markers, location, pref_maxDistance).execute();
        } else {
            Log.v("Trafico", "con location = null");
            new UpdateMapTask(getActivity(), mMap, pref_markers, null, pref_maxDistance).execute();
        }

    }

    private void enableLocation() {
        Log.v("Trafico", "enableLocation");
        if (Permission.isLocationPermissionGranted(mContext)) {
            // Register the listener with the Location Manager to receive location updates
            //noinspection ResourceType
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    LOCATION_MIN_TIME_INTERVAL,
                    LOCATION_MIN_DISTANCE_INTERVAL,
                    mLocationListener);
            //noinspection ResourceType
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    LOCATION_MIN_TIME_INTERVAL,
                    LOCATION_MIN_DISTANCE_INTERVAL,
                    mLocationListener);

            //noinspection ResourceType
            mMap.setMyLocationEnabled(true);
            updateMap(getNewLocation());

        } else {
            // Show rationale and request permission.

            // Should we show an explanation?

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_LOCATION_REQUEST_CODE);
        }
    }

    private void disableLocation() {
        Log.v("Trafico", "disableLocation");
        if (Permission.isLocationPermissionGranted(mContext)) {
            //noinspection ResourceType
            mMap.setMyLocationEnabled(false);

            // Remove the listener you previously added
            //noinspection ResourceType
            mLocationManager.removeUpdates(mLocationListener);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //TODO: Esto no se está llamando y no sé por qué

        Log.v("Trafico", "onRequestPermissionsResult");
        if (requestCode == MY_LOCATION_REQUEST_CODE) {
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "Permiso concedido", Toast.LENGTH_LONG).show();
                //noinspection ResourceType
                mMap.setMyLocationEnabled(true);
                updateMap(getNewLocation());
            }
            else {
                Toast.makeText(getActivity(), "Para activar la función de localización es necesario el permiso de localización", Toast.LENGTH_LONG).show();
                updateMap(null);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v("Trafico", "onResume");

        final String pref_markers = Utility.getPreferredMarkers(mContext);
        final boolean pref_location = Utility.getPreferredLocationEnabled(mContext);
        final int pref_maxDistance = Utility.getPreferredMaxDistance(mContext);

        Log.v("Trafico", String.valueOf(pref_maxDistance));

        //Sólo si se han cambiado las preferencias, se actualiza el mapa
        if (!mPrefMarkers.equals(pref_markers)
                || mPrefLocationEnabled != pref_location
                || mPrefMaxDistance != pref_maxDistance) {
            Log.v("Trafico", "onResume. Ha habido cambio en las preferencias");
            if (pref_location){

                //En enableLocation se actualiza el mapa
                //Si tenemos permiso activamos la localización y actualizamos con localización
                //Si no lo tenemos lo pedimos
                //Si nos lo dan activamos la localización y actualizamos con localización
                //Si no nos lo dan actualizamos con null
                enableLocation();

                //En cuarentena
                /*if (Permission.isLocationPermissionGranted(mContext)) {
                    Log.v("Trafico", "onResume. Enable location");
                    enableLocation();

                    // Register the listener with the Location Manager to receive location updates
                    //noinspection ResourceType
                    mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            LOCATION_MIN_TIME_INTERVAL,
                            LOCATION_MIN_DISTANCE_INTERVAL,
                            mLocationListener);

                    updateMap(getNewLocation());
                } */

            } else {
                Log.v("Trafico", "onResume. Disable location");
                disableLocation();
                updateMap(null);
            }
            mPrefMarkers = pref_markers;
            mPrefLocationEnabled = pref_location;
            mPrefMaxDistance = pref_maxDistance;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                Intent intentSettings = new Intent(getActivity(), SettingsActivity.class);
                startActivity(intentSettings);
                return true;
            case R.id.action_refresh:
                if (Utility.getPreferredLocationEnabled(mContext) && Permission.isLocationPermissionGranted(mContext)) {
                    updateMap(getNewLocation());
                }else {
                    updateMap(null);
                }
                return true;
            case R.id.action_twitter:
                Intent intentTwitter = new Intent(getActivity(), TwitterTrafficActivity.class);
                startActivity(intentTwitter);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
