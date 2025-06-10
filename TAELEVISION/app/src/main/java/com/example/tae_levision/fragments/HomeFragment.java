package com.example.tae_levision.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tae_levision.R;
import com.example.tae_levision.activities.DetailActivity;
import com.example.tae_levision.adapters.CarouselAdapter;
import com.example.tae_levision.adapters.FilmAdapter;
import com.example.tae_levision.adapters.GenreAdapter;
import com.example.tae_levision.api.ApiClient;
import com.example.tae_levision.api.ApiService;
import com.example.tae_levision.models.Film;
import com.example.tae_levision.models.ApiResponse;
import com.example.tae_levision.models.Genre;
import com.example.tae_levision.models.GenreResponse;
import com.example.tae_levision.utils.ThemeManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerViewFilms;
    private RecyclerView recyclerViewGenres;
    private ViewPager2 carouselViewPager;
    private TabLayout carouselIndicator;
    private ProgressBar progressBar;
    private FilmAdapter filmAdapter;
    private GenreAdapter genreAdapter;
    private CarouselAdapter carouselAdapter;
    private Button btnRefresh;
    private Button btnLoadMore;
    private Toast noConnectionToast;
    private CoordinatorLayout rootLayout;
    private AppBarLayout appBarLayout;

    // No connection view components
    private RelativeLayout noConnectionView;
    private Button btnRefreshConnection;

    private List<Film> allFilms = new ArrayList<>();
    private List<Film> trendingFilms = new ArrayList<>();
    private List<Film> filteredFilms = new ArrayList<>();
    private int currentGenreId = 0; // 0 means "All"
    private int currentPage = 1;
    private ApiService apiService;

    private final List<Genre> genreList = new ArrayList<>();
    private final Map<Integer, String> genreMap = new HashMap<>();

    // Auto-scroll handler for carousel
    private final Handler carouselHandler = new Handler(Looper.getMainLooper());
    private final Runnable carouselRunnable = new Runnable() {
        @Override
        public void run() {
            if (carouselViewPager == null || carouselAdapter == null) return;
            int currentItem = carouselViewPager.getCurrentItem();
            int totalCount = carouselAdapter.getItemCount();
            if (totalCount > 0) {
                if (currentItem == totalCount - 1) {
                    carouselViewPager.setCurrentItem(0, false);
                } else {
                    carouselViewPager.setCurrentItem(currentItem + 1, true);
                }
            }
            carouselHandler.postDelayed(this, 5000);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        rootLayout = view.findViewById(R.id.homeRoot);
        appBarLayout = view.findViewById(R.id.appBarHome);
        recyclerViewFilms = view.findViewById(R.id.recyclerViewHome);
        recyclerViewGenres = view.findViewById(R.id.recyclerViewGenres);
        carouselViewPager = view.findViewById(R.id.carouselViewPager);
        carouselIndicator = view.findViewById(R.id.carousel_indicator);
        progressBar = view.findViewById(R.id.progressBarHome);
        btnRefresh = view.findViewById(R.id.btn_refresh);

        // Initialize no connection view components
        noConnectionView = view.findViewById(R.id.noConnectionView);
        btnRefreshConnection = view.findViewById(R.id.btn_refresh_connection);

        // Reposition the progress bar below appbar
        repositionProgressBar();

        // Set button text to make purpose clear
        btnRefresh.setText("Refresh");

        // Create and add load more button dynamically
        addLoadMoreButton(view);

        ImageButton btnThemeToggle = view.findViewById(R.id.btn_theme_toggle);
        updateThemeIcon(btnThemeToggle);

        btnThemeToggle.setOnClickListener(v -> {
            boolean isDark = ThemeManager.isDarkMode(requireContext());
            ThemeManager.setDarkMode(requireContext(), !isDark);
            updateThemeIcon(btnThemeToggle);
            requireActivity().recreate();
        });

        apiService = ApiClient.getClient().create(ApiService.class);

        recyclerViewFilms.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewGenres.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        initializeAdapters();

        btnRefresh.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                currentPage = 1;
                showLoading();
                loadFilms();
            } else {
                showNoConnection();
            }
        });

        // Add click listener for the no connection view's refresh button
        btnRefreshConnection.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                currentPage = 1;
                showLoading();
                loadFilms();
            } else {
                showNoConnection();
            }
        });

        fetchGenresFromApi();

        return view;
    }

    private void repositionProgressBar() {
        // Remove progress bar from its current parent
        ViewGroup parent = (ViewGroup) progressBar.getParent();
        if (parent != null) {
            parent.removeView(progressBar);
        }

        // Create new layout params for the progress bar
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT
        );

        // Position it below the app bar
        params.topMargin = 16;
        params.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;

        // Set behavior to appear below appbar
        AppBarLayout.ScrollingViewBehavior behavior = new AppBarLayout.ScrollingViewBehavior();
        params.setBehavior(behavior);

        // Add progress bar back to coordinator layout
        rootLayout.addView(progressBar, params);

        // Make sure it's visible when needed but initially hidden
        progressBar.setVisibility(View.GONE);
    }

    private void addLoadMoreButton(View rootView) {
        // Find the parent LinearLayout container that holds everything
        ViewGroup parentContainer = (ViewGroup) recyclerViewFilms.getParent();

        // Create Load More button dynamically
        btnLoadMore = new Button(getContext());
        btnLoadMore.setText("Load More Films");
        btnLoadMore.setId(View.generateViewId());

        // Configure button layout parameters
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 20;
        params.bottomMargin = 40;
        btnLoadMore.setLayoutParams(params);

        // Set button style properties
        btnLoadMore.setBackgroundColor(getResources().getColor(R.color.purple_primary));
        btnLoadMore.setTextColor(getResources().getColor(android.R.color.white));
        btnLoadMore.setPadding(0, 20, 0, 20);

        // Initially hide the button
        btnLoadMore.setVisibility(View.GONE);

        // Add click listener
        btnLoadMore.setOnClickListener(v -> {
            if (isNetworkAvailable()) {
                currentPage++;
                progressBar.setVisibility(View.VISIBLE);
                if (currentGenreId == 0) {
                    loadMorePopularFilms();
                } else {
                    loadMoreFilmsByGenre();
                }
            } else {
                showNoConnection();
            }
        });

        // Add the button to parent container after the RecyclerView
        if (parentContainer instanceof LinearLayout) {
            ((LinearLayout) parentContainer).addView(btnLoadMore);
        }
    }

    private void updateThemeIcon(ImageButton btnThemeToggle) {
        boolean isDark = ThemeManager.isDarkMode(requireContext());
        if (isDark) {
            btnThemeToggle.setImageResource(R.drawable.ic_light_mode);
        } else {
            btnThemeToggle.setImageResource(R.drawable.ic_dark_mode);
        }
    }

    private void fetchGenresFromApi() {
        if (!isNetworkAvailable()) {
            showNoConnection();
            return;
        }

        apiService.getGenres("en-US").enqueue(new Callback<GenreResponse>() {
            @Override
            public void onResponse(Call<GenreResponse> call, Response<GenreResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getGenres() != null) {
                    genreList.clear();
                    genreMap.clear();
                    genreList.add(new Genre(0, "All"));
                    for (Genre g : response.body().getGenres()) {
                        genreList.add(g);
                        genreMap.put(g.getId(), g.getName());
                    }
                    initializeGenreAdapter();
                    if (isNetworkAvailable()) {
                        showLoading();
                        loadFilms();
                    } else {
                        showNoConnection();
                    }
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<GenreResponse> call, Throwable t) {
                showError();
                if (!isNetworkAvailable()) {
                    showNoConnection();
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        if (getContext() == null) return false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) return false;
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoConnection() {
        // Cancel any existing toast to prevent stacking
        if (noConnectionToast != null) {
            noConnectionToast.cancel();
        }

        // Hide regular UI elements
        progressBar.setVisibility(View.GONE);
        btnRefresh.setVisibility(View.GONE);
        btnLoadMore.setVisibility(View.GONE);

        // Hide main content
        View nestedScrollView = rootLayout.findViewById(R.id.mainContentScroll);
        if (nestedScrollView != null) {
            nestedScrollView.setVisibility(View.GONE);
        }

        // Show no connection view
        noConnectionView.setVisibility(View.VISIBLE);

        // Clear all data
        allFilms.clear();
        filteredFilms.clear();
        trendingFilms.clear();
        filmAdapter.updateData(filteredFilms);
        if (carouselAdapter != null) carouselAdapter.notifyDataSetChanged();
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.GONE);
        btnLoadMore.setVisibility(View.GONE);

        // Show main content
        View nestedScrollView = rootLayout.findViewById(R.id.mainContentScroll);
        if (nestedScrollView != null) {
            nestedScrollView.setVisibility(View.VISIBLE);
        }

        // Hide no connection view
        noConnectionView.setVisibility(View.GONE);

        carouselViewPager.setVisibility(View.VISIBLE);
        carouselIndicator.setVisibility(View.VISIBLE);
    }

    private void initializeAdapters() {
        filmAdapter = new FilmAdapter(getContext(), filteredFilms);
        filmAdapter.setOnItemClickListener(this::navigateToDetailActivity);
        recyclerViewFilms.setAdapter(filmAdapter);

        carouselAdapter = new CarouselAdapter(getContext(), trendingFilms, genreMap);
        carouselViewPager.setAdapter(carouselAdapter);
        carouselViewPager.setOffscreenPageLimit(1);
        carouselViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                carouselHandler.removeCallbacks(carouselRunnable);
                carouselHandler.postDelayed(carouselRunnable, 5000);
            }
        });
    }

    private void initializeGenreAdapter() {
        genreAdapter = new GenreAdapter(getContext(), genreList, (genre, position) -> {
            currentGenreId = genre.getId();
            currentPage = 1; // Reset to first page when changing genre
            if (isNetworkAvailable()) {
                showLoading();
                loadFilmsByGenre();
            } else {
                showNoConnection();
            }
        });
        recyclerViewGenres.setAdapter(genreAdapter);
    }

    private void navigateToDetailActivity(Film film) {
        if (film == null) {
            Log.e(TAG, "Cannot navigate to DetailActivity: film is null");
            return;
        }
        try {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("film", film);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to DetailActivity", e);
            Toast.makeText(requireContext(), "Gagal membuka detail film", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupCarouselIndicator() {
        if (trendingFilms.isEmpty()) return;
        new TabLayoutMediator(carouselIndicator, carouselViewPager, (tab, position) -> {}).attach();
        carouselHandler.postDelayed(carouselRunnable, 5000);
    }

    private void loadFilms() {
        progressBar.setVisibility(View.VISIBLE);
        btnRefresh.setVisibility(View.GONE);
        btnLoadMore.setVisibility(View.GONE);
        if (!isNetworkAvailable()) {
            showNoConnection();
            return;
        }
        loadTrendingFilms();
        if (currentGenreId == 0) {
            loadPopularFilms();
        } else {
            loadFilmsByGenre();
        }
    }

    private void loadTrendingFilms() {
        apiService.getTrendingFilms().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    trendingFilms.clear();
                    List<Film> results = response.body().getResults();
                    int count = Math.min(results.size(), 3); // Only 3 films
                    for (int i = 0; i < count; i++) {
                        trendingFilms.add(results.get(i));
                    }
                    carouselAdapter = new CarouselAdapter(getContext(), trendingFilms, genreMap);
                    carouselViewPager.setAdapter(carouselAdapter);
                    setupCarouselIndicator();
                } else {
                    carouselViewPager.setVisibility(View.GONE);
                    carouselIndicator.setVisibility(View.GONE);
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                carouselViewPager.setVisibility(View.GONE);
                carouselIndicator.setVisibility(View.GONE);
                if (!isNetworkAvailable()) {
                    showNoConnection();
                }
            }
        });
    }

    private void loadPopularFilms() {
        apiService.getPopularFilms(currentPage, "en-US").enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    allFilms.clear();
                    filteredFilms.clear();
                    allFilms.addAll(response.body().getResults());
                    filteredFilms.addAll(allFilms);
                    filmAdapter.updateData(filteredFilms);

                    // Show "Load More" button if online and films loaded
                    btnLoadMore.setVisibility(isNetworkAvailable() && !filteredFilms.isEmpty() ?
                            View.VISIBLE : View.GONE);
                } else {
                    showError();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLoadMore.setVisibility(View.GONE);
                if (!isNetworkAvailable()) {
                    showNoConnection();
                } else {
                    showError();
                }
            }
        });
    }

    private void loadMorePopularFilms() {
        apiService.getPopularFilms(currentPage, "en-US").enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    List<Film> newFilms = response.body().getResults();
                    if (!newFilms.isEmpty()) {
                        allFilms.addAll(newFilms);
                        filteredFilms.addAll(newFilms);
                        filmAdapter.updateData(filteredFilms);
                        btnLoadMore.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getContext(), "Tidak ada film lain untuk dimuat", Toast.LENGTH_SHORT).show();
                        currentPage--; // Revert page increment
                    }
                } else {
                    currentPage--; // Revert page increment on error
                    showError();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                currentPage--; // Revert page increment on error
                if (!isNetworkAvailable()) {
                    showNoConnection();
                } else {
                    showError();
                }
            }
        });
    }

    private void loadFilmsByGenre() {
        if (currentGenreId == 0) {
            loadPopularFilms();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        apiService.getFilmsByGenre(String.valueOf(currentGenreId), 1).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    List<Film> results = response.body().getResults();
                    filteredFilms.clear();
                    filteredFilms.addAll(results);
                    filmAdapter.updateData(filteredFilms);

                    // Show "Load More" button if online and films loaded
                    btnLoadMore.setVisibility(isNetworkAvailable() && !filteredFilms.isEmpty() ?
                            View.VISIBLE : View.GONE);

                    if (results.isEmpty()) {
                        Toast.makeText(getContext(), "No films found for this genre", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Failed to load films for this genre", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnLoadMore.setVisibility(View.GONE);
                if (!isNetworkAvailable()) {
                    showNoConnection();
                } else {
                    Toast.makeText(getContext(), "Network error, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMoreFilmsByGenre() {
        apiService.getFilmsByGenre(String.valueOf(currentGenreId), currentPage).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().getResults() != null) {
                    List<Film> newFilms = response.body().getResults();
                    if (!newFilms.isEmpty()) {
                        filteredFilms.addAll(newFilms);
                        filmAdapter.updateData(filteredFilms);
                        btnLoadMore.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(getContext(), "Tidak ada film lain untuk genre ini", Toast.LENGTH_SHORT).show();
                        currentPage--; // Revert page increment
                    }
                } else {
                    currentPage--; // Revert page increment on error
                    Toast.makeText(getContext(), "Gagal memuat film tambahan", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                currentPage--; // Revert page increment on error
                if (!isNetworkAvailable()) {
                    showNoConnection();
                } else {
                    Toast.makeText(getContext(), "Network error, please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showError() {
        Toast.makeText(getContext(), "Gagal memuat data film. Silakan coba lagi.", Toast.LENGTH_SHORT).show();
        btnRefresh.setVisibility(View.VISIBLE);
        btnLoadMore.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
        carouselHandler.removeCallbacks(carouselRunnable);
        if (noConnectionToast != null) {
            noConnectionToast.cancel();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (carouselViewPager != null && carouselAdapter != null && carouselAdapter.getItemCount() > 0) {
            carouselHandler.postDelayed(carouselRunnable, 5000);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        carouselHandler.removeCallbacks(carouselRunnable);
        if (noConnectionToast != null) {
            noConnectionToast.cancel();
        }
    }
}