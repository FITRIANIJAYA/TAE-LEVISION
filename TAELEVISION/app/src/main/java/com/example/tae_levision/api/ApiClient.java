package com.example.tae_levision.api;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static final String API_KEY = "51b5ee054e24f0501b67270d97442856";
    private static final String ACCESS_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1MWI1ZWUwNTRlMjRmMDUwMWI2NzI3MGQ5NzQ0Mjg1NiIsIm5iZiI6MTc0OTM5NDczMy43MjEsInN1YiI6IjY4NDVhNTJkMjFjY2VjM2FhNTM0MjM4YSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.8OXC16aNfACDSTn055TVzMuECEmTDaxzrk6DoP1tk30";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Add headers interceptor
            Interceptor headersInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    
                    // Create URL with API key as query parameter for endpoints that need it
                    okhttp3.HttpUrl url = original.url().newBuilder()
                            .addQueryParameter("api_key", API_KEY)
                            .build();
                    
                    // Build request with Authorization header for Bearer token
                    Request request = original.newBuilder()
                            .url(url)
                            .header("Authorization", "Bearer " + ACCESS_TOKEN)
                            .header("accept", "application/json")
                            .method(original.method(), original.body())
                            .build();
                            
                    return chain.proceed(request);
                }
            };

            // Build OkHttpClient with interceptors
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(headersInterceptor)
                    .addInterceptor(logging)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();

            // Build Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
