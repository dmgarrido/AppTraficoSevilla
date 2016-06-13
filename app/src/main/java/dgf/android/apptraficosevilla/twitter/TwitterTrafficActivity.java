package dgf.android.apptraficosevilla.twitter;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import dgf.android.apptraficosevilla.R;
import io.fabric.sdk.android.Fabric;

public class TwitterTrafficActivity extends AppCompatActivity {

    final static String SEARCH_QUERY = "(from:@EmergenciasSev AND #Tráfico) OR (#DGTSevilla AND #DGT)";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_layout);

        ListView listView = (ListView)findViewById(R.id.list_view);

        TwitterAuthConfig authConfig =  new TwitterAuthConfig(getApplicationContext().getResources().getString(R.string.twitter_consumer_key),
                getApplicationContext().getResources().getString(R.string.twitter_consumer_secret));
        Fabric.with(this, new Twitter(authConfig));

        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        final SearchTimeline searchTimeline = new SearchTimeline.Builder()
                .query(SEARCH_QUERY)
                .build();
        final TweetTimelineListAdapter adapter = new TweetTimelineListAdapter.Builder(this)
                .setTimeline(searchTimeline)
                .build();
        listView.setAdapter(adapter);


        if (swipeLayout != null) {
            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    swipeLayout.setRefreshing(true);
                    adapter.refresh(new Callback<TimelineResult<Tweet>>() {
                        @Override
                        public void success(Result<TimelineResult<Tweet>> result) {
                            swipeLayout.setRefreshing(false);
                        }

                        @Override
                        public void failure(TwitterException exception) {
                            // Toast or some other action
                            Toast.makeText(TwitterTrafficActivity.this, "Error refreshing tweets", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }
    }
}
