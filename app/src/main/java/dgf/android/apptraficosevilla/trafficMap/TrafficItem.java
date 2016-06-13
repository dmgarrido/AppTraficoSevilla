package dgf.android.apptraficosevilla.trafficMap;

import android.text.Html;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class TrafficItem implements ClusterItem {

    private final LatLng mPosition;
    private String mTitle;
    private String mSnippet;

    public TrafficItem(LatLng latLng, String descriptionHtml) {
        mPosition = latLng;
        mTitle = Html.fromHtml(descriptionHtml.substring(0,descriptionHtml.indexOf("Estado Trafico"))).toString();
        mSnippet = Html.fromHtml(parseSnippet(descriptionHtml)).toString();
    }

    private String parseSnippet(String descriptionHtml) {
        //A partir de la descripción completa obtenemos la porción que nos interesa, que es la
        //información del tráfico

        int inicioTitulo = 0;
        int finTitulo = descriptionHtml.indexOf("Estado Trafico");

        int inicioInfoEstadoTrafico = finTitulo + 14;
        int finInfoEstadoTrafico = descriptionHtml.indexOf("Validez");

        //String titulo = description.substring(inicioTitulo, finTitulo);
        String infoEstadoTrafico = "TRÁFICO "+ descriptionHtml.substring(inicioInfoEstadoTrafico, finInfoEstadoTrafico);

        return infoEstadoTrafico;
    }

    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public String getIntensidad() {

        if (mSnippet.contains("FLUIDO")) {
            return "FLUIDO";
        } else {
            if (mSnippet.contains("INTENSO")) {
                return "INTENSO";
            } else {
                return "OTRO";
            }
        }
    }
}
