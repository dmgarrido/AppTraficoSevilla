package dgf.android.apptraficosevilla.trafficMap;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.kml.KmlContainer;
import com.google.maps.android.kml.KmlLayer;
import com.google.maps.android.kml.KmlLineString;
import com.google.maps.android.kml.KmlMultiGeometry;
import com.google.maps.android.kml.KmlPlacemark;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import dgf.android.apptraficosevilla.R;
import dgf.android.apptraficosevilla.detail.DetailTrafficActivity;

public class UpdateMapTask extends AsyncTask<Void, Void, byte[]> {

    //private final float MAX_DISTANCE_MARKERS = 1000; //metros

    //En teoría esta URL se debería descargar con el método downloadUrlApiEstadoTrafico
    //pero para la app del proyecto del curso la vamos a "hardcodear" para simplificar
    //private final String mUrlApiEstadoTrafico;

    private ProgressDialog mProgressDialog;
    private String mPrefMarkers;
    private Context mContext;
    private GoogleMap mMap;
    private Location mLocation;
    private int mMaxDistance;

    // Declare a variable for the cluster manager.
    private ClusterManager<TrafficItem> mClusterManager;

    public UpdateMapTask(Context context, GoogleMap map, String pref_markers, Location location, int pref_maxDistance) {
        mContext = context;
        mMap = map;
        mPrefMarkers = pref_markers;
        mLocation = location;
        mMaxDistance = pref_maxDistance;
    }

    protected void onPreExecute() {
        super.onPreExecute();

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setMessage(mContext.getString(R.string.progress_msg_cargando));
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.show();
    }

    @Override
    protected byte[] doInBackground(Void... voids) {

        //Nos vamos a descar un array de bytes que convertiremos en capa kml
        byte[] kmlEstadoTraficoBytes;

        try {
            InputStream is = getUrlApiEstadoTrafico().openStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[16384]; //1024 * 16
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            is.close();
            buffer.flush();

            kmlEstadoTraficoBytes = buffer.toByteArray();

            return kmlEstadoTraficoBytes;

        }catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(byte[] kmlEstadoTraficoBytes) {

        if (kmlEstadoTraficoBytes == null) {
            Toast.makeText(mContext, "No se han podido cargar los datos. Inténtelo de nuevo más tarde.", Toast.LENGTH_LONG).show();

        } else {
            addKmlLayerToMap(kmlEstadoTraficoBytes);
        }
        mProgressDialog.dismiss();
    }


    private URL getUrlApiEstadoTrafico() throws MalformedURLException {
        //En teoría el String con la URL se debería descargar con el método downloadUrlApiEstadoTrafico
        //pero para la app del proyecto del curso la vamos a "hardcodear" para simplificar
        //final String urlApiEstadoTrafico = downloadUrlApiEstadoTrafico();
        final String urlApiEstadoTrafico = "http://trafico.sevilla.org/estado_trafico.xml";

        return new URL(urlApiEstadoTrafico);
    }


    private void addKmlLayerToMap(byte[] kmlEstadoTraficoBytes) {

        //Convertimos el array de bytes en la capa kml que contiene el estado del tráfico
        //y la añadimos al mapa
        try {
            KmlLayer layerEstadoTrafico = new KmlLayer(mMap, new ByteArrayInputStream(kmlEstadoTraficoBytes), mContext);
            layerEstadoTrafico.addLayerToMap();

            //Añadimos clustering al mapa
            setUpMapCluster(layerEstadoTrafico);

        } catch (XmlPullParserException e) {
            Toast.makeText(mContext, "Error cargar los datos. Inténtelo de nuevo más tarde.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } catch (IOException ioe) {
            Toast.makeText(mContext, "Error cargar los datos. Inténtelo de nuevo más tarde.", Toast.LENGTH_LONG).show();
            ioe.printStackTrace();
        }

    }


    private void setUpMapCluster(KmlLayer layerEstadoTrafico) {

        //Mediante la utilidad de marker clustering se agrupan los markers y según nos acercamos
        // con el zoom se van desagrupando. Así queda más claro cuando tenemos muchos markers

        // Initialize the manager with the context and the map.
        mClusterManager = new ClusterManager<>(mContext, mMap);

        //Añadimos los markers
        addTrafficItems(layerEstadoTrafico);

        //Añadimos un cluster renderer para customizar los markers del cluster
        mClusterManager.setRenderer(new TrafficItemRenderer());

        //Customizamos la info window que se muestra al pulsar un marker
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new TrafficInfoWindowAdapter());

        // Point the map's listeners at the listeners implemented by the cluster
        // manager.
        mMap.setOnCameraChangeListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //Toast.makeText(mContext, marker.getSnippet(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, DetailTrafficActivity.class);
                intent.putExtra("titleTraffic", marker.getTitle());
                intent.putExtra("detailTraffic", marker.getSnippet());
                mContext.startActivity(intent);
            }
        });

    }

    private void addTrafficItems(KmlLayer layer) {

        //Añadimos un marker o cluster item por cada placemark nuestro mapa
        KmlContainer container = layer.getContainers().iterator().next();
        container = container.getContainers().iterator().next();

        for (KmlPlacemark placemark : container.getPlacemarks()) {

            String descriptionHtml = placemark.getProperty("description");

            //Sólo añadimos los marcadores que contienen la etiqueta de Estado Trafico
            if (descriptionHtml.contains("Estado Trafico")) {
                KmlMultiGeometry multiGeometry = (KmlMultiGeometry) placemark.getGeometry();
                KmlLineString lineString = (KmlLineString) multiGeometry.getGeometryObject().iterator().next();
                LatLng coordinates = lineString.getGeometryObject().iterator().next();

                //Creamos un objeto de tipo TrafficItem (ClusterItem)
                //Le pasamos las coordenadas y la descripción que sacamos de la KmlLayer
                TrafficItem trafficItem = new TrafficItem(coordinates, descriptionHtml);

                //Sólo añadimos los markers que el usuario quiere que se muestren
                if (!mPrefMarkers.equals("ninguno")) {

                    if (mLocation != null){
                        Location location = new Location("Location");
                        location.setLatitude(trafficItem.getPosition().latitude);
                        location.setLongitude(trafficItem.getPosition().longitude);

                        if (location.distanceTo(mLocation) <= mMaxDistance) {
                            //Log.v("Trafico", "Distancia");
                            if (mPrefMarkers.equals("intenso")) {
                                if (!trafficItem.getIntensidad().equals("FLUIDO")){
                                    mClusterManager.addItem(trafficItem);
                                }
                            } else {
                                mClusterManager.addItem(trafficItem);
                            }
                        }
                    } else {
                      //  Log.v("Trafico", "mLocation is null");
                        if (mPrefMarkers.equals("intenso")) {
                            if (!trafficItem.getIntensidad().equals("FLUIDO")){
                                mClusterManager.addItem(trafficItem);
                            }
                        } else {
                            mClusterManager.addItem(trafficItem);
                        }
                    }

                    /*Esto es sin usar la localización
                    if (mPrefMarkers.equals("intenso")) {
                        if (!trafficItem.getIntensidad().equals("FLUIDO")){
                            mClusterManager.addItem(trafficItem);
                        }
                    } else {
                        mClusterManager.addItem(trafficItem);
                    }*/

                }

            }
        }

    }

    private class TrafficItemRenderer extends DefaultClusterRenderer<TrafficItem> {

        public TrafficItemRenderer() {
            super(mContext, mMap, mClusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(TrafficItem trafficItem, MarkerOptions markerOptions) {

            markerOptions.title(trafficItem.getTitle());
            markerOptions.snippet(trafficItem.getSnippet());

            switch (trafficItem.getIntensidad()) {
                case "FLUIDO":
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    return;
                case "INTENSO":
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                    return;
                default:
                    return;
            }
        }
    }

    class TrafficInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View mWindow;

        TrafficInfoWindowAdapter() {
            //mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mWindow = LayoutInflater.from(mContext).inflate(R.layout.custom_info_window, null);

            //El WebView no funciona en este caso porque el infoWindow no es una live view, y no
            // renderiza la información a tiempo para mostrarla
            //mWindow = getLayoutInflater().inflate(R.layout.custom_info_windows_webview, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView title = (TextView)mWindow.findViewById(R.id.title_info_window);
            TextView info = (TextView)mWindow.findViewById(R.id.txt_info_window);

            info.setText(marker.getSnippet());
            title.setText(marker.getTitle());

            return mWindow;
        }
    }

    private String downloadUrlApiEstadoTrafico () {

        //NOTA: Para la app del proyecto del curso no usamos este método
        //Buscamos la URL en el catálogo de la API de datos abiertos del Ayuntamiento de Sevilla

        HttpURLConnection connection = null;

        final String APICATALOGO_BASE_URL = "http://services1.arcgis.com/hcmP7kr0Cx3AcTJk/ArcGIS/rest/services/C%C3%A1maras_CCT_de_Sevilla_shape/FeatureServer/0/query?where=1%3D1&outFields=*&f=json";
        //final String ID_API_QUERY_PARAM = "id";

        //String idApiEstadoTrafico = "estado-del-trafico";
        String infoDatosEstadoTraficoJSON = "";

        String urlStringInfoDatosEstadoTrafico = Uri.parse(APICATALOGO_BASE_URL)
                .buildUpon()
                //.appendQueryParameter(ID_API_QUERY_PARAM, idApiEstadoTrafico)
                .build().toString();

        try {
            URL urlInfoDatosEstadoTrafico = new URL(urlStringInfoDatosEstadoTrafico);

            connection = (HttpURLConnection)urlInfoDatosEstadoTrafico.openConnection();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlStringInfoDatosEstadoTrafico);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            infoDatosEstadoTraficoJSON = out.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        //return getUrlStringFromJSON(infoDatosEstadoTraficoJSON);
        return null;
    }


}
