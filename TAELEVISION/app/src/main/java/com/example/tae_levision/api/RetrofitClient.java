package com.example.tae_levision.api;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "51b5ee054e24f0501b67270d97442856";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            // Membuat OkHttpClient dengan interceptor untuk menambahkan header autentikasi
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request.Builder requestBuilder = original.newBuilder()
                                .header("accept", "application/json")
                                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1MWI1ZWUwNTRlMjRmMDUwMWI2NzI3MGQ5NzQ0Mjg1NiIsIm5iZiI6MTc0OTM5NDczMy43MjEsInN1YiI6IjY4NDVhNTJkMjFjY2VjM2FhNTM0MjM4YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.8OXC16aNfACDSTn055TVzMuECEmTDaxzrk6DoP1tk30");

                        return chain.proceed(requestBuilder.build());
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}