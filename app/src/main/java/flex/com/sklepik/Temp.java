/*


package flex.com.sklepik;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import flex.com.sklepik.data.Places;
import flex.com.sklepik.remote.PlacesAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Temp extends FragmentActivity implements OnMarkerClickListener {

     //Progress Dialog
    private ProgressDialog pDialog;

    public String lat;
    public String longi;
    public String name;

    public String nazwa;
    double dlug, szer;
    public double navi_dlug, navi_szer;
    private static final int BLAD_GPS = 1;
    GoogleMap mMap;

    public void setDlug(double dlug) {
        this.dlug = dlug;
    }

    public void setSzer(double szer) {
        this.szer = szer;
    }

    public void setNazwa(String nazwa) {
        this.nazwa = nazwa;
    }

    private static final float DomyslnyZoom = 5;
    LocationManager locationManager;
    List<RowModel> rowModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ImageButton btnNavi = (ImageButton) findViewById(R.id.butnavi);
        rowModels = new ArrayList<>();
        btnNavi.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (navi_szer + navi_dlug == 0)
                    Toast.makeText(Temp.this,
                            "Najpierw wybierz market", Toast.LENGTH_SHORT)
                            .show();
                else {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri
                            .parse("google.navigation:q=" + navi_szer + ","
                                    + navi_dlug));

                    startActivity(i);
                    finish();
                    System.exit(0);
                }
                // TODO Auto-generated method stub

            }
        });

        // Look up the AdView as a resource and load a request.
        AdView adView = (AdView) this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        int status = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) {
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this,
                    requestCode);
            dialog.show();
            return;
        }
        Mapa(); //wczytuje mape
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void Mapa() {

         //Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
         //    Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
         //    Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);

                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                // Create a criteria object to retrieve provider
                Criteria criteria = new Criteria();

                // Get the name of the best provider
                String provider = locationManager.getBestProvider(criteria, true);

                // Get Current Location
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Zaakceptuj uprawnienia!", Toast.LENGTH_LONG).show();
                    return;
                }
                Location myLocation = locationManager.getLastKnownLocation(provider);
                if (myLocation != null) {
                    LatLng ll = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(ll)
                            .zoom(14).build();
                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cameraPosition));
                } else {
                    LatLng ll = new LatLng(52.230625, 21.013129);//WAWA
                    LatLng ll = new LatLng(53.363631, 17.040005);//Złotów
                    CameraPosition cp = new CameraPosition.Builder()
                            .target(ll)
                            .zoom(6).build();
                    mMap.animateCamera(CameraUpdateFactory
                            .newCameraPosition(cp));
                }
                mMap.setOnMarkerClickListener(this);
            }
        }
    }

    @Override
    protected void onResume() {
         //TODO Auto-generated method stub
        super.onResume();
        // loading the comments via AsyncTask
        new LoadComments().execute();
        coreRetroFit();

        MapStateManager mgr = new MapStateManager(this);
        CameraPosition pozycja = mgr.pobierzZapisanyStan();
        int typ = mgr.pobierzTypMapy();
        if (typ == 0)
            mMap.setMapType(GoogleMap.MAP_TYPE_NONE);
        if (typ == 1)
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (typ == 2)
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if (typ == 3)
            mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        if (typ == 4)
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        if (pozycja != null) {
            CameraUpdate update = CameraUpdateFactory
                    .newCameraPosition(pozycja);

            mMap.moveCamera(update);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public class LoadComments extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(Temp.this);
            pDialog.setMessage("ŁADUJĘ LISTĘ SKLEPÓW");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
            wczytajMape();
            idzDoPolozenia(52.249665, 21.012511, 10);
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {
            // we will develop this method in version 2

            return null;
        }

        public boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager
                    .getActiveNetworkInfo();

            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {
                pDialog.dismiss();
                pDialog = null;
            } catch (Exception e) {
                // nothing
            }
            // we will develop this method in version 2

            ustawMape();
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void ustawMape() {

        uslugaOK();

        dlug = zwroc_dlug();
        szer = zwroc_szer();

         Marker();
        nazwa = zwroc_nazwe();

        if (nazwa.equals("Auchan"))
            MarkerAuchan();
        if (nazwa.equals("Biedronka"))
            MarkerBiedronka();
        if (nazwa.equals("Leclerc"))
            MarkerLeclerc();
        if (nazwa.equals("Kaufland"))
            MarkerKaufland();
        if (nazwa.equals("Lidl"))
            Marker();
        if (nazwa.equals("Makro"))
            MarkerMakro();
        if (nazwa.equals("Piotr"))
            MarkerPiotr();
        if (nazwa.equals("Carrefour"))
            MarkerCarrefour();
        if (nazwa.equals("Tesco"))
            MarkerTesco();
        if (nazwa.equals("Zabka"))
            MarkerZabka();
    }

    public String[] tablica() {
        Bundle b = getIntent().getExtras();
        String[] resultArr = b.getStringArray("selectedItems");
        return resultArr;
    }

    public double zwroc_szer() {
        double szer = Double.parseDouble(lat);
        return szer;
    }

    public double zwroc_dlug() {
        double dlug = Double.parseDouble(longi);
        return dlug;
    }

    public String zwroc_nazwe() {
        String nazwa = name;
        return nazwa;
    }

    void idzDoPolozenia(double szer, double dlug, float przyb) {

        LatLng ll = new LatLng(szer, dlug);
        CameraUpdate zaktualizuj = CameraUpdateFactory.newLatLngZoom(ll, przyb);
        mMap.moveCamera(zaktualizuj);
    }

    public boolean uslugaOK() {

        int jestDostepna = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (jestDostepna == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(jestDostepna)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(jestDostepna,
                    this, BLAD_GPS);
            dialog.show();
        } else {
            Toast.makeText(this, "Nie mogę połączyć się z Google Play",
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    public boolean wczytajMape() {
        if (mMap == null) {
            SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mMap = mapFrag.getMap();
            mMap.setOnMarkerClickListener(this);
            mMap.setMyLocationEnabled(true);
        }
        return (mMap != null);
    }

    public void Marker() {
            MarkerOptions opcje = new MarkerOptions().title(name)
                    .position(new LatLng(szer, dlug));

            mMap.addMarker(opcje);

    }

    private void hideSoftKeyboard(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    protected void onStop() {
         //TODO Auto-generated method stub
        super.onStop();
        MapStateManager mgr = new MapStateManager(this);
        mgr.zapiszStanMapy(mMap);
    }

    @Override
    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {

        LatLng gps = marker.getPosition();
        szer = gps.latitude;
        dlug = gps.longitude;
        navi_dlug = dlug;
        navi_szer = szer;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(szer, dlug, 1);
            String adres = addresses.get(0).getAddressLine(0);
            Toast.makeText(this, "Adres: " + adres, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        mMap.clear();
        startActivity(new Intent(getApplicationContext(),
                MainActivity.class));

        finish();
        System.exit(0);
        return;
    }

    private void coreRetroFit() {
        PlacesAPI.Factory.getInstance().getPlaces().enqueue(new Callback<Places>() {

            @Override
            public void onResponse(Call<Places> call, Response<Places> response) {
                for (int i = 0; i < response.body().getPosts().size(); i++) {

                    RowModel rowModel = new RowModel(response.body().getPosts().get(i).getNazwa(),
                            Double.parseDouble(response.body().getPosts().get(i).getSzer()),
                            Double.parseDouble(response.body().getPosts().get(i).getDlug()));
                    rowModels.add(rowModel);
                }

                for (int j = 0; j < rowModels.size(); j++) {
                    if (!rowModels.get(j).getName().equals("Zabka")) {
                        continue;
                    }
                    setNazwa(rowModels.get(j).getName());
                    setSzer(rowModels.get(j).getLongitude());
                    setDlug(rowModels.get(j).getLattitude());
                    Marker();
                }
            }

            @Override
            public void onFailure(Call<Places> call, Throwable t) {

            }
        });
    }

    private void setMap() {
        uslugaOK();

        if (nazwa.equals("Auchan"))
            MarkerAuchan();
        if (nazwa.equals("Biedronka"))
            MarkerBiedronka();
        if (nazwa.equals("Leclerc"))
            MarkerLeclerc();
        if (nazwa.equals("Kaufland"))
            MarkerKaufland();
        if (nazwa.equals("Lidl"))
            Marker();
        if (nazwa.equals("Makro"))
            MarkerMakro();
        if (nazwa.equals("Piotr"))
            MarkerPiotr();
        if (nazwa.equals("Carrefour"))
            MarkerCarrefour();
        if (nazwa.equals("Tesco"))
            MarkerTesco();
        if (nazwa.equals("Zabka"))
            MarkerZabka();
    }
}



*/
