package com.example.tae_levision.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tae_levision.R;
import com.example.tae_levision.activities.DetailActivity;
import com.example.tae_levision.models.Film;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class FilmAdapter extends RecyclerView.Adapter<FilmAdapter.FilmViewHolder> {
    private static final String TAG = "FilmAdapter";
    private List<Film> filmList;
    private Context context;
    private OnItemClickListener itemClickListener;

    // Interface for item click
    public interface OnItemClickListener {
        void onItemClick(Film film);
    }

    public FilmAdapter(Context context, List<Film> filmList) {
        this.context = context;
        this.filmList = filmList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    @NonNull
    @Override
    public FilmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_film, parent, false);
        return new FilmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilmViewHolder holder, int position) {
        Film film = filmList.get(position);
        holder.bind(film);
    }

    @Override
    public int getItemCount() {
        return filmList == null ? 0 : filmList.size();
    }

    public void updateData(List<Film> newFilmList) {
        this.filmList = newFilmList;
        notifyDataSetChanged();
    }

    public class FilmViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView poster;
        private final TextView title;

        public FilmViewHolder(@NonNull View itemView) {
            super(itemView);
            poster = itemView.findViewById(R.id.imagePoster);
            title = itemView.findViewById(R.id.textTitle);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Film film = filmList.get(position);
                    if (itemClickListener != null) {
                        itemClickListener.onItemClick(film);
                    } else {
                        navigateToDetailActivity(film);
                    }
                }
            });
        }

        public void bind(Film film) {
            title.setText(film.getTitle());

            String posterUrl = film.getPosterPath();
            if (posterUrl != null && !posterUrl.isEmpty()) {
                Glide.with(context)
                        .load(posterUrl)
                        .placeholder(R.drawable.poster)
                        .error(R.drawable.poster)
                        .into(poster);
            } else {
                poster.setImageResource(R.drawable.poster);
            }
        }
    }

    private void navigateToDetailActivity(Film film) {
        if (film == null) {
            Log.e(TAG, "Cannot navigate to DetailActivity: film is null");
            return;
        }
        try {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("film_id", film.getId());
            intent.putExtra("film_title", film.getTitle() != null ? film.getTitle() : "Unknown Title");
            intent.putExtra("film_poster", film.getPosterPath() != null ? film.getPosterPath() : "");
            intent.putExtra("film_backdrop", film.getBackdropPath() != null ? film.getBackdropPath() : "");
            intent.putExtra("film_synopsis", film.getOverview() != null ? film.getOverview() : "No synopsis available");
            intent.putExtra("film_release_date", film.getReleaseDate() != null ? film.getReleaseDate() : "Unknown");
            intent.putExtra("film_rating", film.getVoteAverage());
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to DetailActivity", e);
            Toast.makeText(context, "Gagal membuka detail film", Toast.LENGTH_SHORT).show();
        }
    }
}