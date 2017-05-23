package com.flex.sklepik.remote;

import com.flex.sklepik.data.Places;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

/**
 * Created by Armin on 2017-05-16.
 */

public interface PlacesAPI {

    String baseUrl = "http://bakusek.zz.mu/";

    @GET("webservice/market.php")
    Call<Places> getPlaces();
    class Factory {
        public static PlacesAPI service;

        public static PlacesAPI getInstance() {

            if (service == null) {
                Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(baseUrl)
                        .build();
                service = retrofit.create(PlacesAPI.class);
                return service;
            } else {
                return service;
            }
        }
    }
}


