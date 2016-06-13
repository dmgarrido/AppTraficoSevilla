package dgf.android.apptraficosevilla;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

public class Permission {

    public static boolean isLocationPermissionGranted(Context context) {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

}
