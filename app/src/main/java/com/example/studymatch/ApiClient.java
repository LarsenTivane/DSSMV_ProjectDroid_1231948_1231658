package com.example.studymatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://studymatch-25cf.restdb.io/";
    private static Retrofit retrofit = null;

    public static RestDbService getService() {
        if (retrofit == null) {
            // Configura o Gson para ser permissivo com tipos e decimais
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(RestDbService.class);
    }
} 