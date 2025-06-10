package com.example.tae_levision.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tae_levision.R;
import com.example.tae_levision.activities.DetailActivity;
import com.example.tae_levision.adapters.FilmAdapter;
import com.example.tae_levision.api.ApiClient;
import com.example.tae_levision.api.ApiService;
import com.example.tae_levision.models.ApiResponse;
import com.example.tae_levision.models.Film;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final String PREFS_NAME = "SearchPreferences";
    private static final String KEY_RECENT_FILMS = "recent_films";
    private static final int MAX_RECENT_FILMS = 5;
    private static final long SEARCH_DELAY_MS = 300; // Debounce delay untuk pencarian

    private EditText etSearch;
    private ImageButton btnSearch;
    private RecyclerView rvSearchResult;
    private RecyclerView rvRecentSearches;
    private ProgressBar progressBar;
    private FilmAdapter filmAdapter;
    private FilmAdapter recentFilmsAdapter;
    private View emptyState;
    private View recentSearchesContainer;
    private TextView clearRecentSearches;
    private List<Film> filmList;
    private List<Film> recentFilmsList;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;
    private Gson gson;

    // Handler untuk debouncing pencarian
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<ApiResponse> currentSearchCall;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize Gson untuk konversi objek Film ke JSON
        gson = new Gson();

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize API Service
        apiService = ApiClient.getClient().create(ApiService.class);

        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search_clear);
        rvSearchResult = view.findViewById(R.id.rv_search_result);
        rvRecentSearches = view.findViewById(R.id.rv_recent_searches);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyState = view.findViewById(R.id.empty_state);
        recentSearchesContainer = view.findViewById(R.id.recent_searches_container);
        clearRecentSearches = view.findViewById(R.id.clear_recent_searches);

        // Initialize film list and adapter for search results
        filmList = new ArrayList<>();
        rvSearchResult.setLayoutManager(new GridLayoutManager(getContext(), 2));
        filmAdapter = new FilmAdapter(requireContext(), filmList);
        filmAdapter.setOnItemClickListener(film -> {
            saveRecentFilm(film);
            navigateToDetailActivity(film);
        });
        rvSearchResult.setAdapter(filmAdapter);

        // Initialize recent films list and adapter
        recentFilmsList = getRecentFilms();
        rvRecentSearches.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recentFilmsAdapter = new FilmAdapter(requireContext(), recentFilmsList);
        recentFilmsAdapter.setOnItemClickListener(this::navigateToDetailActivity);
        rvRecentSearches.setAdapter(recentFilmsAdapter);

        // Show clear button when text is entered
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && etSearch.getText().length() > 0) {
                btnSearch.setVisibility(View.VISIBLE);
            } else if (!hasFocus && etSearch.getText().length() == 0) {
                btnSearch.setVisibility(View.GONE);
            }
        });

        // Pencarian saat user menekan Enter
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    searchFilm(query);
                    return true;
                }
            }
            return false;
        });

        // Set up search/clear button
        btnSearch.setOnClickListener(v -> {
            if (etSearch.getText().length() > 0) {
                // Clear text if there is text
                etSearch.setText("");
                btnSearch.setVisibility(View.GONE);
                // Reset search results
                filmList.clear();
                filmAdapter.notifyDataSetChanged();
                showEmptyState(false);
                displayRecentFilms();
            }
        });

        // Set up clear recent searches button
        clearRecentSearches.setOnClickListener(v -> {
            clearRecentFilms();
            Toast.makeText(requireContext(), "Recent search has been deleted", Toast.LENGTH_SHORT).show();
        });

        // Pencarian real-time saat input teks berubah
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Show search button when text is present
                if (s.length() > 0) {
                    btnSearch.setVisibility(View.VISIBLE);

                    // Cancel previous search if any
                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }

                    if (currentSearchCall != null) {
                        currentSearchCall.cancel();
                    }

                    // Schedule new search with delay (debounce)
                    final String query = s.toString().trim();
                    searchRunnable = () -> searchFilm(query);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else {
                    btnSearch.setVisibility(View.GONE);
                    // Cancel any pending searches
                    searchHandler.removeCallbacks(searchRunnable);

                    if (currentSearchCall != null) {
                        currentSearchCall.cancel();
                    }

                    // Hide results when search is cleared
                    filmList.clear();
                    filmAdapter.notifyDataSetChanged();
                    showEmptyState(false);
                    displayRecentFilms();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not used
            }
        });

        // Display recent films when fragment is created
        displayRecentFilms();

        return view;
    }

    private void searchFilm(String query) {
        if (query.isEmpty()) {
            displayRecentFilms();
            filmList.clear();
            filmAdapter.notifyDataSetChanged();
            return;
        }

        showLoading(true);
        recentSearchesContainer.setVisibility(View.GONE);

        // Cancel previous call if exists
        if (currentSearchCall != null) {
            currentSearchCall.cancel();
        }

        // Use TMDB API to search for films
        currentSearchCall = apiService.searchFilms(query, 1, false);
        currentSearchCall.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (call.isCanceled()) {
                    return;
                }

                showLoading(false);

                try {
                    if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                        List<Film> results = response.body().getResults();

                        filmList.clear();
                        filmList.addAll(results);
                        filmAdapter.notifyDataSetChanged();

                        if (results.isEmpty()) {
                            showEmptyState(true);
                        } else {
                            showEmptyState(false);
                        }
                    } else {
                        showEmptyState(true);
                        Toast.makeText(getContext(), "Gagal mengambil hasil pencarian", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing search results", e);
                    showEmptyState(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (call.isCanceled()) {
                    return;
                }

                showLoading(false);
                showEmptyState(true);

                if (!(t instanceof java.io.IOException)) {
                    // Tampilkan error hanya jika bukan karena request dibatalkan
                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Bersihkan Handler callbacks saat fragment dihancurkan
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }

        if (currentSearchCall != null) {
            currentSearchCall.cancel();
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            rvSearchResult.setVisibility(View.GONE);
            emptyState.setVisibility(View.GONE);
            recentSearchesContainer.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyState.setVisibility(View.VISIBLE);
            rvSearchResult.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvSearchResult.setVisibility(View.VISIBLE);
        }
    }

    // Method to navigate to detail activity with film data
    private void navigateToDetailActivity(Film film) {
        try {
            if (film == null) {
                Log.e(TAG, "Cannot navigate to DetailActivity: film is null");
                return;
            }

            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("film", film); // Kirim objek Film secara langsung
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to DetailActivity", e);
            Toast.makeText(requireContext(), "Gagal membuka detail film", Toast.LENGTH_SHORT).show();
        }
    }

    // Simpan film ke SharedPreferences
    private void saveRecentFilm(Film film) {
        if (film == null) {
            return;
        }

        List<Film> recentFilms = getRecentFilms();

        // Hapus film ini jika sudah ada di list (untuk menghindari duplikat)
        for (int i = 0; i < recentFilms.size(); i++) {
            if (recentFilms.get(i).getId() == film.getId()) {
                recentFilms.remove(i);
                break;
            }
        }

        // Tambahkan film baru di awal list
        recentFilms.add(0, film);

        // Batasi jumlah film terbaru ke MAX_RECENT_FILMS
        if (recentFilms.size() > MAX_RECENT_FILMS) {
            recentFilms = recentFilms.subList(0, MAX_RECENT_FILMS);
        }

        // Simpan ke SharedPreferences
        saveFilmsToPrefs(recentFilms);

        // Update UI
        recentFilmsList.clear();
        recentFilmsList.addAll(recentFilms);
        recentFilmsAdapter.notifyDataSetChanged();
    }

    // Simpan list film ke SharedPreferences (konversi ke JSON)
    private void saveFilmsToPrefs(List<Film> films) {
        String jsonFilms = gson.toJson(films);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_RECENT_FILMS, jsonFilms);
        editor.apply();
    }

    // Ambil recent films dari SharedPreferences
    private List<Film> getRecentFilms() {
        String jsonFilms = sharedPreferences.getString(KEY_RECENT_FILMS, "");

        if (jsonFilms.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<Film>>(){}.getType();
        try {
            List<Film> films = gson.fromJson(jsonFilms, type);
            return films != null ? films : new ArrayList<>();
        } catch (Exception e) {
            Log.e(TAG, "Error parsing recent films", e);
            return new ArrayList<>();
        }
    }

    // Hapus semua film terbaru
    private void clearRecentFilms() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_RECENT_FILMS);
        editor.apply();

        recentFilmsList.clear();
        recentFilmsAdapter.notifyDataSetChanged();

        // Sembunyikan container jika tidak ada film terbaru
        recentSearchesContainer.setVisibility(View.GONE);
    }

    // Tampilkan film terbaru di UI
    private void displayRecentFilms() {
        List<Film> recentFilms = getRecentFilms();

        // Update list dan beritahu adapter
        recentFilmsList.clear();
        recentFilmsList.addAll(recentFilms);
        recentFilmsAdapter.notifyDataSetChanged();

        // Tampilkan/sembunyikan container berdasarkan ada tidaknya film terbaru
        if (recentFilms.isEmpty()) {
            recentSearchesContainer.setVisibility(View.GONE);
        } else {
            recentSearchesContainer.setVisibility(View.VISIBLE);
        }
    }
}
