package com.example.tae_levision.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tae_levision.R;
import com.example.tae_levision.models.Film;

import java.util.List;
import java.util.Map;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder> {

    private final Context context;
    private final List<Film> films;
    private final Map<Integer, String> genreMap;

    public CarouselAdapter(Context context, List<Film> films, Map<Integer, String> genreMap) {
        this.context = context;
        this.films = films;
        this.genreMap = genreMap;
    }

    @NonNull
    @Override
    public CarouselViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carousel, parent, false);
        return new CarouselViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarouselViewHolder holder, int position) {
        if (films == null || films.isEmpty() || position >= films.size()) return;
        Film film = films.get(position);
        holder.tvTitle.setText(film.getTitle());

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty() && genreMap != null) {
            StringBuilder genres = new StringBuilder();
            for (Integer id : film.getGenreIds()) {
                String name = genreMap.get(id);
                if (name != null) {
                    if (genres.length() > 0) genres.append(", ");
                    genres.append(name);
                }
            }
            holder.tvGenre.setText(genres.length() > 0 ? genres.toString() : "Unknown");
        } else {
            holder.tvGenre.setText("Unknown");
        }

        Glide.with(context)
                .load(film.getBackdropPath())
                .placeholder(R.drawable.backdrop)
                .error(R.drawable.backdrop)
                .into(holder.ivBackdrop);
    }

    @Override
    public int getItemCount() {
        return films == null ? 0 : films.size();
    }

    static class CarouselViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBackdrop;
        TextView tvTitle, tvGenre;

        CarouselViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBackdrop = itemView.findViewById(R.id.imageBackdrop);
            tvTitle = itemView.findViewById(R.id.textTitle);
            tvGenre = itemView.findViewById(R.id.textGenre);
        }
    }
}