package dgf.android.apptraficosevilla.trafficMap;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import dgf.android.apptraficosevilla.R;

public class MapsActivity extends AppCompatActivity {

    private TrafficMapFragment mTrafficMapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mTrafficMapFragment = (TrafficMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    }

}
