package com.example.studymatch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://studymatch-8319.restdb.io/";
    private static volatile Retrofit retrofit;

    public static RestDbService getService() {
        if (retrofit == null) {
            synchronized (ApiClient.class) {
                if (retrofit == null) {
                    Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();

                    OkHttpClient httpClient = new OkHttpClient.Builder()
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .addInterceptor(chain -> {
                                okhttp3.Request request = chain.request().newBuilder()
                                        .header("x-apikey", RestDbService.API_KEY)
                                        .header("Accept", "application/json")
                                        .build();
                                okhttp3.Response response = chain.proceed(request);

                                // Plano Development: 1 pedido/segundo. Em caso de 429, espera e repete.
                                int tentativas = 0;
                                while (response.code() == 429 && tentativas < 3) {
                                    response.close();
                                    try {
                                        Thread.sleep(1200);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        throw new IOException("Pedido interrompido", e);
                                    }
                                    response = chain.proceed(request);
                                    tentativas++;
                                }
                                return response;
                            })
                            .build();

                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(httpClient)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofit.create(RestDbService.class);
    }
}