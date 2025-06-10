package com.example.tae_levision.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MovieDetailsResponse {
    @SerializedName("id")
    private int id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("poster_path")
    private String posterPath;
    
    @SerializedName("backdrop_path")
    private String backdropPath;
    
    @SerializedName("overview")
    private String overview;
    
    @SerializedName("release_date")
    private String releaseDate;
    
    @SerializedName("vote_average")
    private float voteAverage;
    
    @SerializedName("runtime")
    private int runtime;
    
    @SerializedName("genres")
    private List<Genre> genres;
    
    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";
    
    public int getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getPosterPath() {
        if (posterPath != null && !posterPath.isEmpty()) {
            return IMAGE_BASE_URL + posterPath;
        }
        return null;
    }
    
    public String getBackdropPath() {
        if (backdropPath != null && !backdropPath.isEmpty()) {
            return IMAGE_BASE_URL + backdropPath;
        }
        return null;
    }
    
    public String getOverview() {
        return overview;
    }
    
    public String getReleaseDate() {
        return releaseDate;
    }
    
    public float getVoteAverage() {
        return voteAverage;
    }
    
    public int getRuntime() {
        return runtime;
    }
    
    public List<Genre> getGenres() {
        return genres;
    }
    
    public static class Genre {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;
        
        public int getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
    }
}
