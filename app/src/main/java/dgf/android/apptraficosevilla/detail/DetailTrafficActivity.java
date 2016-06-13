package dgf.android.apptraficosevilla.detail;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import dgf.android.apptraficosevilla.R;

public class DetailTrafficActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;

    private String mDetailTraffic;
    private String mTitleTraffic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_traffic);

        mDetailTraffic = getIntent().getStringExtra("detailTraffic");
        mTitleTraffic = getIntent().getStringExtra("titleTraffic");

        TextView detailTextView = (TextView)findViewById(R.id.detail_traffic_text_view);
        detailTextView.setText(mDetailTraffic);

        TextView titleTextView = (TextView)findViewById(R.id.title_traffic_text_view);
        titleTextView.setText(mTitleTraffic);

        ImageView detailImage = (ImageView)findViewById(R.id.detail_icon);
        if (mDetailTraffic.contains("FLUIDO")) {
            detailImage.setImageResource(R.drawable.traffic_light_green);
        } else {
            if (mDetailTraffic.contains("INTENSO")) {
                detailImage.setImageResource(R.drawable.traffic_light_yellow);
            } else {
                detailImage.setImageResource(R.drawable.traffic_light_red);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }

        return true;
    }

    private Intent createShareIntent() {

        String shareMsg = "Estado del tráfico en " + mTitleTraffic + ": " + mDetailTraffic;

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.putExtra(Intent.EXTRA_TEXT, shareMsg);
        intent.setType("text/plain");

        return intent;

    }
}
