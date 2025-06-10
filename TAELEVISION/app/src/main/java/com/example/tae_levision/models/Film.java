package com.example.tae_levision.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Film implements Serializable {
    private static final long serialVersionUID = 1L;

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

    @SerializedName("popularity")
    private float popularity;

    @SerializedName("original_language")
    private String originalLanguage;

    @SerializedName("genre_ids")
    private List<Integer> genreIds;

    // Custom fields (not from TMDB)
    private String genre;
    private String duration;
    private String director;
    private String language;
    private boolean isFavorite;

    private static final String IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500";

    public Film() {}

    public Film(String title, String posterPath) {
        this.title = title;
        this.posterPath = posterPath;
    }

    public Film(String title, String posterPath, String genre, String overview) {
        this.title = title;
        this.posterPath = posterPath;
        this.genre = genre;
        this.overview = overview;
    }

    public Film(int id, String title, String posterPath, String backdropPath,
                String overview, String releaseDate, float voteAverage, String genre) {
        this.id = id;
        this.title = title;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.genre = genre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getPosterPath() {
        if (posterPath != null && !posterPath.isEmpty()) {
            if (posterPath.startsWith("http")) {
                return posterPath;
            } else {
                return IMAGE_BASE_URL + posterPath;
            }
        }
        return null;
    }
    public void setPosterPath(String posterPath) { this.posterPath = posterPath; }

    public String getBackdropPath() {
        if (backdropPath != null && !backdropPath.isEmpty()) {
            if (backdropPath.startsWith("http")) {
                return backdropPath;
            } else {
                return IMAGE_BASE_URL + backdropPath;
            }
        }
        return null;
    }
    public void setBackdropPath(String backdropPath) { this.backdropPath = backdropPath; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }

    public float getVoteAverage() { return voteAverage; }
    public void setVoteAverage(float voteAverage) { this.voteAverage = voteAverage; }

    public float getPopularity() { return popularity; }
    public void setPopularity(float popularity) { this.popularity = popularity; }

    public String getOriginalLanguage() { return originalLanguage; }
    public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }

    public List<Integer> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public List<String> getGenreList() {
        List<String> result = new ArrayList<>();
        if (genre != null && !genre.isEmpty()) {
            for (String g : genre.split(",")) {
                String trimmed = g.trim();
                if (!trimmed.isEmpty()) result.add(trimmed);
            }
        }
        return result;
    }

    public boolean hasGenres() {
        return genre != null && !genre.trim().isEmpty();
    }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getReleaseYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return "";
    }

    @Override
    public String toString() {
        return "Film{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", overview='" + (overview != null ? overview.substring(0, Math.min(overview.length(), 20)) + "..." : "null") + '\'' +
                '}';
    }
}