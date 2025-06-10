package com.example.tae_levision.api;

import com.example.tae_levision.models.ApiResponse;
import com.example.tae_levision.models.MovieDetailsResponse;
import com.example.tae_levision.models.GenreResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("movie/popular")
    Call<ApiResponse> getPopularFilms(
            @Query("page") int page,
            @Query("language") String language
    );

    @GET("discover/movie")
    Call<ApiResponse> getFilmsByGenre(
            @Query("with_genres") String genreId,
            @Query("page") int page
    );

    // Endpoint untuk detail film (runtime, dsb)
    @GET("movie/{movie_id}")
    Call<MovieDetailsResponse> getMovieDetails(
            @Path("movie_id") int movieId
    );

    @GET("search/movie")
    Call<ApiResponse> searchFilms(
            @Query("query") String query,
            @Query("page") int page,
            @Query("include_adult") boolean includeAdult
    );

    @GET("trending/movie/week")
    Call<ApiResponse> getTrendingFilms();

    @GET("genre/movie/list")
    Call<GenreResponse> getGenres(
            @Query("language") String language
    );
}