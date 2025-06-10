package com.example.tae_levision.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.tae_levision.models.Film;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesManager {
    private static final String TAG = "FavoritesManager";
    private static final String PREFS_NAME = "film_favorites";
    private static final String KEY_FAVORITES = "favorites";
    
    private SharedPreferences sharedPreferences;
    private Gson gson;
    
    public FavoritesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }
    
    public List<Film> getFavorites() {
        String favoritesJson = sharedPreferences.getString(KEY_FAVORITES, "");
        if (favoritesJson.isEmpty()) {
            return new ArrayList<>();
        }
        
        try {
            Type type = new TypeToken<List<Film>>(){}.getType();
            return gson.fromJson(favoritesJson, type);
        } catch (Exception e) {
            Log.e(TAG, "Error getting favorites", e);
            return new ArrayList<>();
        }
    }
    
    public boolean addFavorite(Film film) {
        List<Film> favorites = getFavorites();
        
        // Check if film already exists in favorites
        for (Film favorite : favorites) {
            if (favorite.getId() == film.getId() || 
                (favorite.getTitle() != null && favorite.getTitle().equals(film.getTitle()))) {
                Log.d(TAG, "Film is already in favorites");
                return false;  // Film already exists
            }
        }
        
        // Add film to favorites
        favorites.add(film);
        return saveFavorites(favorites);
    }
    
    public boolean removeFavorite(Film film) {
        return removeFavorite(film.getTitle());
    }
    
    public boolean removeFavorite(String title) {
        List<Film> favorites = getFavorites();
        boolean removed = false;
        
        for (int i = 0; i < favorites.size(); i++) {
            Film favorite = favorites.get(i);
            if (favorite.getTitle() != null && favorite.getTitle().equals(title)) {
                favorites.remove(i);
                removed = true;
                break;
            }
        }
        
        if (removed) {
            return saveFavorites(favorites);
        }
        return false;
    }
    
    public boolean isFavorite(String title) {
        List<Film> favorites = getFavorites();
        for (Film favorite : favorites) {
            if (favorite.getTitle() != null && favorite.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isFavorite(int id) {
        List<Film> favorites = getFavorites();
        for (Film favorite : favorites) {
            if (favorite.getId() == id) {
                return true;
            }
        }
        return false;
    }
    
    private boolean saveFavorites(List<Film> favorites) {
        try {
            String favoritesJson = gson.toJson(favorites);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_FAVORITES, favoritesJson);
            return editor.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error saving favorites", e);
            return false;
        }
    }
}
