package com.example.tae_levision.api;

import android.net.Uri;

public class YoutubeSearchHelper {
    public static String generateSearchUrl(String query) {
        String baseUrl = "https://www.youtube.com/results";
        Uri uri = Uri.parse(baseUrl)
                .buildUpon()
                .appendQueryParameter("search_query", query + " trailer")
                .build();
        return uri.toString();
    }
}
