package flex.com.sklepik;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import io.realm.RealmResults;

import static flex.com.sklepik.R.id.map;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private double naviLat, naviLong;
    private double latitude, longitude;

    @BindView(R.id.adView)
    AdView adView;
    @BindView(R.id.butnavi)
    ImageButton butnavi;
    private GPStracker gpStracker;
    private Location location;
    private RealmResults<RowModel> rowModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map2);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);

        gpStracker = new GPStracker(getApplicationContext());
        mapFragment.getMapAsync(this);
        ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        gpStracker = new GPStracker(getApplicationContext());

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Uprawnienia nie przyznane", Toast.LENGTH_SHORT).show();
        }
        LocationManager lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 3, this);
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Location loc = gpStracker.getlocation();
        latitude = loc.getLatitude();
        longitude = loc.getLongitude();

        Intent intent = getIntent();

        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        moveCameraToActualPosition(latitude, longitude);
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
        Realm realm = Realm.getDefaultInstance();

        rowModels = realm.where(RowModel.class).equalTo("name", "Zabka").findAll();
        if (!rowModels.isEmpty()) {
            String s;
            for (int i = 0; i < rowModels.size(); i++) {
                Log.d("fdsdfs", rowModels.get(i).getName());

                addMarker(rowModels.get(i).getLongitude(), rowModels.get(i).getLattitude());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
