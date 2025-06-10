package com.example.tae_levision.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.tae_levision.R;
import com.example.tae_levision.api.ApiClient;
import com.example.tae_levision.api.ApiService;
import com.example.tae_levision.api.YoutubeSearchHelper;
import com.example.tae_levision.database.FavoritesManager;
import com.example.tae_levision.models.Film;
import com.example.tae_levision.models.MovieDetailsResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";
    private boolean isFavorite = false;
    private Film film;
    private FavoritesManager favoritesManager;

    private TextView tvDuration;

    private static final List<String> STANDARD_GENRES = Arrays.asList(
            "Action", "Adventure", "Animation", "Comedy", "Crime",
            "Documentary", "Drama", "Family", "Fantasy", "History",
            "Horror", "Music", "Mystery", "Romance", "Sci-Fi",
            "TV Movie", "Thriller", "War", "Western"
    );

    private static final Map<Integer, String> GENRE_MAP = new HashMap<Integer, String>() {{
        put(28, "Action");
        put(12, "Adventure");
        put(16, "Animation");
        put(35, "Comedy");
        put(80, "Crime");
        put(99, "Documentary");
        put(18, "Drama");
        put(10751, "Family");
        put(14, "Fantasy");
        put(36, "History");
        put(27, "Horror");
        put(10402, "Music");
        put(9648, "Mystery");
        put(10749, "Romance");
        put(878, "Sci-Fi");
        put(10770, "TV Movie");
        put(53, "Thriller");
        put(10752, "War");
        put(37, "Western");
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        favoritesManager = new FavoritesManager(this);
        setupToolbar();
        loadFilmFromIntent();

        if (film == null || film.getTitle() == null || film.getTitle().isEmpty()) {
            Toast.makeText(this, "Failed to load film details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();

        // If duration is missing, fetch detail from API
        if (film.getDuration() == null || film.getDuration().isEmpty()) {
            fetchFilmDetailFromApi(film.getId());
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadFilmFromIntent() {
        try {
            film = (Film) getIntent().getSerializableExtra("film");
        } catch (Exception e) {
            Log.e(TAG, "Error getting film from intent", e);
            film = null;
        }
    }

    private void initializeViews() {
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvYear = findViewById(R.id.tvYear);
        ImageView detailPoster = findViewById(R.id.detailPoster);
        ImageView detailBackdrop = findViewById(R.id.detailBackdrop);
        TextView tvSynopsis = findViewById(R.id.tvDesc);
        tvDuration = findViewById(R.id.tvDuration);
        TextView tvReleaseDate = findViewById(R.id.tvReleaseDate);
        TextView tvPopularity = findViewById(R.id.tvPopularity);
        TextView tvLanguage = findViewById(R.id.tvLanguage);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        TextView tvRating = findViewById(R.id.tvRating);
        MaterialButton btnFavorite = findViewById(R.id.btnFavorite);
        ChipGroup genreChipGroup = findViewById(R.id.genreChipGroup);
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);

        toolbarTitle.setText(film.getTitle());
        tvTitle.setText(film.getTitle());
        tvSynopsis.setText(film.getOverview());
        tvYear.setText(film.getReleaseYear());
        tvDuration.setText(film.getDuration() != null ? film.getDuration() : "N/A");
        tvReleaseDate.setText(film.getReleaseDate() != null ? film.getReleaseDate() : "N/A");
        tvPopularity.setText(film.getPopularity() > 0 ? String.format("%.1f", film.getPopularity()) : "N/A");

        String lang = film.getOriginalLanguage();
        if (lang != null && !lang.isEmpty()) {
            tvLanguage.setText(getLanguageName(lang));
        } else {
            tvLanguage.setText("N/A");
        }

        loadImages(detailPoster, detailBackdrop);

        float rating = film.getVoteAverage() / 2;
        ratingBar.setRating(Math.min(rating, 5));
        tvRating.setText(String.format("%.1f", rating));

        setupGenreChipsFromApi(genreChipGroup);
        setupFavoriteButton(btnFavorite);
        setupTrailerButton();
    }

    // Fetch film detail from API if duration is missing
    private void fetchFilmDetailFromApi(int filmId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<MovieDetailsResponse> call = apiService.getMovieDetails(filmId);
        call.enqueue(new Callback<MovieDetailsResponse>() {
            @Override
            public void onResponse(Call<MovieDetailsResponse> call, Response<MovieDetailsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieDetailsResponse detail = response.body();
                    String duration = formatDuration(detail.getRuntime());
                    film.setDuration(duration);
                    if (tvDuration != null) {
                        runOnUiThread(() -> tvDuration.setText(duration != null ? duration : "N/A"));
                    }
                }
            }
            @Override
            public void onFailure(Call<MovieDetailsResponse> call, Throwable t) {
                Log.e(TAG, "Failed to fetch film detail", t);
            }
        });
    }

    // Helper to format runtime (minutes) to "xh ym"
    private String formatDuration(Integer runtime) {
        if (runtime == null || runtime <= 0) return "N/A";
        int hours = runtime / 60;
        int minutes = runtime % 60;
        return hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
    }

    private String getLanguageName(String languageCode) {
        if (languageCode == null || languageCode.isEmpty()) {
            return "N/A";
        }
        Map<String, String> languageMap = new HashMap<>();
        languageMap.put("en", "English");
        languageMap.put("es", "Spanish");
        languageMap.put("fr", "French");
        languageMap.put("de", "German");
        languageMap.put("it", "Italian");
        languageMap.put("ja", "Japanese");
        languageMap.put("ko", "Korean");
        languageMap.put("zh", "Chinese");
        languageMap.put("ru", "Russian");
        languageMap.put("pt", "Portuguese");
        languageMap.put("hi", "Hindi");
        languageMap.put("id", "Indonesia");
        String name = languageMap.get(languageCode.toLowerCase());
        return name != null ? name : languageCode.toUpperCase();
    }

    private void loadImages(ImageView detailPoster, ImageView detailBackdrop) {
        if (film.getPosterPath() != null && !film.getPosterPath().isEmpty()) {
            Glide.with(this)
                    .load(film.getPosterPath())
                    .placeholder(R.drawable.poster)
                    .error(R.drawable.poster)
                    .into(detailPoster);
        }
        if (film.getBackdropPath() != null && !film.getBackdropPath().isEmpty()) {
            Glide.with(this)
                    .load(film.getBackdropPath())
                    .placeholder(R.drawable.backdrop)
                    .error(R.drawable.backdrop)
                    .into(detailBackdrop);
        }
    }

    private void setupGenreChipsFromApi(ChipGroup genreChipGroup) {
        genreChipGroup.removeAllViews();
        boolean hasGenre = false;
        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            for (Integer genreId : film.getGenreIds()) {
                String genreName = GENRE_MAP.get(genreId);
                if (genreName != null) {
                    addGenreChip(genreChipGroup, genreName);
                    hasGenre = true;
                }
            }
        }
        if (!hasGenre && film.getGenreList() != null && !film.getGenreList().isEmpty()) {
            for (String genre : film.getGenreList()) {
                if (genre != null && !genre.trim().isEmpty()) {
                    addGenreChip(genreChipGroup, genre.trim());
                    hasGenre = true;
                }
            }
        }
        if (!hasGenre) {
            addGenreChip(genreChipGroup, "Unknown");
        }
    }

    private void setupFavoriteButton(MaterialButton btnFavorite) {
        try {
            isFavorite = favoritesManager.isFavorite(film.getTitle());
        } catch (Exception e) {
            Log.e(TAG, "Error checking favorite status", e);
            isFavorite = false;
        }
        updateFavoriteButton(btnFavorite);
        btnFavorite.setOnClickListener(v -> {
            try {
                if (isFavorite) {
                    favoritesManager.removeFavorite(film.getTitle());
                    Toast.makeText(this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                    isFavorite = false;
                } else {
                    favoritesManager.addFavorite(film);
                    Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                    isFavorite = true;
                }
                updateFavoriteButton(btnFavorite);
            } catch (Exception e) {
                Log.e(TAG, "Error managing favorite status", e);
                Toast.makeText(this, "Error managing favorite", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupTrailerButton() {
        findViewById(R.id.btnPlayTrailer).setOnClickListener(v -> {
            try {
                String videoUrl = YoutubeSearchHelper.generateSearchUrl(film.getTitle() + " trailer");
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error opening trailer", e);
                Toast.makeText(this, "Failed to open trailer", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addGenreChip(ChipGroup chipGroup, String genre) {
        Chip chip = new Chip(this);
        chip.setText(genre);
        chip.setChipBackgroundColorResource(R.color.purple_primary);
        chip.setTextColor(getResources().getColor(R.color.white));
        chip.setTextSize(12);
        chipGroup.addView(chip);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateFavoriteButton(MaterialButton button) {
        if (isFavorite) {
            button.setIconResource(R.drawable.ic_favorite);
        } else {
            button.setIconResource(R.drawable.ic_favorite_border);
        }
        button.setIconTintResource(R.color.purple_primary);
    }
}