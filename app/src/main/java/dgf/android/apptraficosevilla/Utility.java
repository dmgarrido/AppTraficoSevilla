package dgf.android.apptraficosevilla;

import android.content.Context;
import android.preference.PreferenceManager;

public class Utility {

    public static String getPreferredMarkers(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_markers_key), context.getString(R.string.pref_markers_default));
    }

    public static boolean getPreferredLocationEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.pref_checkLocation_key), false);
    }

    public static int getPreferredMaxDistance(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(context.getString(R.string.pref_maxDistance_key), 200);
    }
}
