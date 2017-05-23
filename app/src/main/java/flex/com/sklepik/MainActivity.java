package flex.com.sklepik;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import flex.com.sklepik.data.Places;
import flex.com.sklepik.remote.PlacesAPI;
import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    List<String> shopsNames;
    ArrayList<RowModel> rowModels;

    public Realm mRealm;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBar;
    private ShopsAdapter adapter;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

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

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Uprawnienia nie przyznane", Toast.LENGTH_SHORT).show();
        }

        rowModels = new ArrayList<>();
        shopsNames = new ArrayList<>();
        Realm.init(this);

        mRealm = Realm.getDefaultInstance();

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

    private class AsyncTaskRetro extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            PlacesAPI.Factory.getInstance().getPlaces().enqueue(new Callback<Places>() {

                @Override
                public void onResponse(Call<Places> call, Response<Places> response) {
                    for (int i = 0; i < response.body().getPosts().size(); i++) {
                        RowModel rowModel = new RowModel(response.body().getPosts().get(i).getNazwa(),
                                Double.parseDouble(response.body().getPosts().get(i).getSzer()),
                                Double.parseDouble(response.body().getPosts().get(i).getDlug()));
                        rowModels.add(rowModel);
                    }
                    String oldName;
                    oldName = rowModels.get(0).getName();
                    shopsNames.add(rowModels.get(0).getName());

                    mRealm.beginTransaction();
                    mRealm.copyToRealm(rowModels);
                    mRealm.commitTransaction();

                    for (int j = 0; j < rowModels.size(); j++) {

                        if (rowModels.get(j).getName().equals(oldName)) {
                            continue;
                        }
                        oldName = rowModels.get(j).getName();
                        shopsNames.add(rowModels.get(j).getName());
                    }

                    //sortowanie listy z nazwami sklepow
                    Collections.sort(shopsNames);

                    adapter = new ShopsAdapter(MainActivity.this, shopsNames);
                    recyclerView.setAdapter(adapter);
                }

                @Override
                public void onFailure(Call<Places> call, Throwable t) {

                }
            });

            return null;
        }
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
}
