package com.flex.sklepik;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.flex.sklepik.data.Places;
import com.flex.sklepik.remote.Places_NamesAPI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    List<String> shopsNames;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1972;

    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    private ShopsAdapter adapter;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private ProgressDialog progressDialog;
    private Dialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ConnectionDetector connectionDetector = new ConnectionDetector(this);
        if (!connectionDetector.isConnection()) {
            finish();
        }

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        checkPlayServices();

        shopsNames = new ArrayList<>();

        initCollapsingToolbar();

        Glide.with(getApplicationContext()).load(R.drawable.shoplogo).
                into((ImageView) findViewById(R.id.backdrop));

        adapter = new ShopsAdapter(MainActivity.this, shopsNames);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        AsyncTaskRetro asyncTaskRetro = new AsyncTaskRetro();

        asyncTaskRetro.execute();
    }

    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {

                if (errorDialog == null) {
                    errorDialog = googleApiAvailability.getErrorDialog(this, resultCode, 2404);
                    errorDialog.setCancelable(false);
                }

                if (!errorDialog.isShowing())
                    errorDialog.show();
            }
        }

        return resultCode == ConnectionResult.SUCCESS;
    }

    private class AsyncTaskRetro extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Czekaj...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Places_NamesAPI.Factory.getInstance().getPlaces().enqueue(new Callback<Places>() {

                @Override
                public void onResponse(Call<Places> call, Response<Places> response) {
                    for (int i = 0; i < response.body().getPosts().size(); i++) {
                        shopsNames.add(response.body().getPosts().get(i).getNazwa());
                    }

                    //sortowanie listy z nazwami sklepow
                    Collections.sort(shopsNames);
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onFailure(Call<Places> call, Throwable t) {

                }
            });

            return null;
        }
    }

    @Override
    protected void onResume() {
        recyclerView.setAdapter(adapter);
        super.onResume();
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.app_bar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {

                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
