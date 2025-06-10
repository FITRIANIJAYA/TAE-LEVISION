package com.example.tae_levision.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.tae_levision.R;
import com.example.tae_levision.activities.DetailActivity;
import com.example.tae_levision.adapters.FilmAdapter;
import com.example.tae_levision.database.FavoritesManager;
import com.example.tae_levision.models.Film;

import java.util.List;

public class FavoriteFragment extends Fragment {

    private static final String TAG = "FavoriteFragment";
    private RecyclerView rvFavorite;
    private FilmAdapter filmAdapter;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private FavoritesManager favoritesManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvFavorite = view.findViewById(R.id.rv_favorite);
        emptyState = view.findViewById(R.id.empty_state);
        progressBar = view.findViewById(R.id.progress_bar);

        favoritesManager = new FavoritesManager(requireContext());
        rvFavorite.setLayoutManager(new GridLayoutManager(getContext(), 2));

        loadFavoriteMovies();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavoriteMovies();
    }

    private void loadFavoriteMovies() {
        progressBar.setVisibility(View.VISIBLE);
        rvFavorite.setVisibility(View.GONE);
        emptyState.setVisibility(View.GONE);

        List<Film> favoriteList = favoritesManager.getFavorites();

        progressBar.setVisibility(View.GONE);

        if (favoriteList == null || favoriteList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            rvFavorite.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            rvFavorite.setVisibility(View.VISIBLE);

            if (filmAdapter == null) {
                filmAdapter = new FilmAdapter(requireContext(), favoriteList);
                filmAdapter.setOnItemClickListener(this::navigateToDetailActivity);
                rvFavorite.setAdapter(filmAdapter);
            } else {
                filmAdapter.updateData(favoriteList);
                filmAdapter.setOnItemClickListener(this::navigateToDetailActivity);
            }
        }
    }

    // Consistent with SearchFragment: pass the whole Film object
    private void navigateToDetailActivity(Film film) {
        if (film == null) {
            Log.e(TAG, "Cannot navigate to DetailActivity: film is null");
            return;
        }
        try {
            Intent intent = new Intent(requireContext(), DetailActivity.class);
            intent.putExtra("film", film); // Pass the Film object
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to DetailActivity", e);
            Toast.makeText(requireContext(), "Gagal membuka detail film", Toast.LENGTH_SHORT).show();
        }
    }
}