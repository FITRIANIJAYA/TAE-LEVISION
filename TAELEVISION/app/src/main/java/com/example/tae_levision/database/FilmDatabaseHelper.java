package com.example.tae_levision.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.tae_levision.models.Film;

import java.util.ArrayList;
import java.util.List;

public class FilmDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "FilmDatabaseHelper";
    private static final String DATABASE_NAME = "film_database";
    private static final int DATABASE_VERSION = 1;

    // Table and columns
    private static final String TABLE_FAVORITES = "favorites";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_POSTER = "poster_url";
    private static final String COLUMN_BACKDROP = "backdrop_url";
    private static final String COLUMN_OVERVIEW = "overview";
    private static final String COLUMN_RELEASE_DATE = "release_date";
    private static final String COLUMN_VOTE_AVERAGE = "vote_average";

    public FilmDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY,"
                + COLUMN_TITLE + " TEXT UNIQUE,"
                + COLUMN_POSTER + " TEXT,"
                + COLUMN_BACKDROP + " TEXT,"
                + COLUMN_OVERVIEW + " TEXT,"
                + COLUMN_RELEASE_DATE + " TEXT,"
                + COLUMN_VOTE_AVERAGE + " REAL"
                + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }

    // Add a film to favorites
    public void addFavorite(Film film) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(COLUMN_ID, film.getId());
            values.put(COLUMN_TITLE, film.getTitle());
            values.put(COLUMN_POSTER, film.getPosterPath());
            values.put(COLUMN_BACKDROP, film.getBackdropPath());
            values.put(COLUMN_OVERVIEW, film.getOverview());
            values.put(COLUMN_RELEASE_DATE, film.getReleaseDate());
            values.put(COLUMN_VOTE_AVERAGE, film.getVoteAverage());

            // Cek apakah film sudah ada di favorit
            if (!isFavorite(film.getTitle())) {
                db.insert(TABLE_FAVORITES, null, values);
                Log.d(TAG, "Film berhasil ditambahkan: " + film.getTitle());
            } else {
                Log.d(TAG, "Film sudah ada di favorit: " + film.getTitle());
            }
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "Error menambahkan film ke favorit", e);
        }
    }

    // Get all favorite films
    public List<Film> getAllFavorites() {
        List<Film> filmList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_FAVORITES;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                try {
                    // Gunakan constructor Film dengan parameter yang sesuai
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                    String posterPath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POSTER));

                    Film film = new Film(title, posterPath);
                    film.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    film.setBackdropPath(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BACKDROP)));
                    film.setOverview(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OVERVIEW)));
                    film.setReleaseDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELEASE_DATE)));
                    film.setVoteAverage(cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_VOTE_AVERAGE)));

                    filmList.add(film);
                } catch (Exception e) {
                    Log.e(TAG, "Error saat membaca data film", e);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return filmList;
    }

    // Check if a film is in favorites
    public boolean isFavorite(String title) {
        try {
            SQLiteDatabase db = this.getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + COLUMN_TITLE + "=?";
            Cursor cursor = db.rawQuery(query, new String[]{title});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            db.close();
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "Error saat cek status favorit", e);
            return false;
        }
    }

    // Remove a film from favorites
    public void removeFavorite(String title) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            int rowsDeleted = db.delete(TABLE_FAVORITES, COLUMN_TITLE + "=?", new String[]{title});
            Log.d(TAG, "Film dihapus dari favorit: " + title + " (Rows deleted: " + rowsDeleted + ")");
            db.close();
        } catch (Exception e) {
            Log.e(TAG, "Error saat menghapus film dari favorit", e);
        }
    }
}