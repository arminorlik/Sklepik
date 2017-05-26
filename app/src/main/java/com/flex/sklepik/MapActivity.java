package com.flex.sklepik;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.Toast;

import com.flex.sklepik.data.Places;
import com.flex.sklepik.remote.PlacesAPI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.flex.sklepik.R.id.map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private double naviLat, naviLong;
    private double latitude, longitude;
    private GoogleApiClient mGoogleApiClient;

    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.butnavi)
    ImageButton butnavi;
    private Location location;
    private RealmResults<RowModel> rowModels;
    private String shopName;
    private Bundle bundle;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        bundle = getIntent().getExtras();
        shopName = bundle.getString("shopName");

        mapFragment.getMapAsync(this);
        ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        //realm = Realm.getDefaultInstance();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Uprawnienia nie przyznane", Toast.LENGTH_SHORT).show();
        }
        mMap.setMyLocationEnabled(true);
        cameraMove();

        //po wczytaniu mapy wyciągnij dane z bazy sklepów
        realmDatabaseBuild();
    }

    private void addMarker(double lat, double lon) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lon)));
    }

    @OnClick(R.id.butnavi)
    public void btnNavi() {
        if (naviLat + naviLong == 0)
            Toast.makeText(this,
                    "Najpierw wybierz market", Toast.LENGTH_SHORT)
                    .show();
        else {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri
                    .parse("google.navigation:q=" + naviLat + ","
                            + naviLong));

            startActivity(i);
            finish();
            System.exit(0);
        }
    }

    private void moveCameraToActualPosition(double lat, double lon) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lat, lon))
                .zoom(12).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void realmDatabaseBuild() {
        PlacesAPI.Factory.getInstance().getPlaces().enqueue(new Callback<Places>() {

            @Override
            public void onResponse(Call<Places> call, Response<Places> response) {
                for (int i = 0; i < response.body().getPosts().size(); i++) {
                    if( response.body().getPosts().get(i).getNazwa().equals(shopName)){
                        addMarker(Double.parseDouble(response.body().getPosts().get(i).getDlug()),
                                Double.parseDouble(response.body().getPosts().get(i).getSzer()));


                    }

                }

            }

            @Override
            public void onFailure(Call<Places> call, Throwable t) {

            }
        });

       /* rowModels = realm.where(RowModel.class).equalTo("name", shopName).findAll();
        if (!rowModels.isEmpty()) {

            for (int i = 0; i < rowModels.size(); i++) {

                addMarker(rowModels.get(i).getLongitude(), rowModels.get(i).getLattitude());
            }
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //realm.close();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mMap.clear();
        //rowModels = null;
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        LatLng gps = marker.getPosition();

        //ustawienie lat i long dla nawigacji
        naviLat = gps.latitude;
        naviLong = gps.longitude;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(gps.latitude, gps.longitude, 1);
            String adres = addresses.get(0).getAddressLine(0);
            Toast.makeText(this, "Adres: " + adres, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public void cameraMove() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Uprawnienia nie przyznane", Toast.LENGTH_SHORT).show();
        }
        Location myLocation = locationManager.getLastKnownLocation(provider);
        if (myLocation != null) {
            LatLng ll = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(ll)
                    .zoom(11).build();
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        } else {
            LatLng ll = new LatLng(52.230625, 21.013129);//WAWA
            CameraPosition cp = new CameraPosition.Builder()
                    .target(ll)
                    .zoom(10).build();
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cp));
        }
    }
}
