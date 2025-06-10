package com.example.tae_levision.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tae_levision.R;
import com.example.tae_levision.models.Genre;
import com.google.android.material.chip.Chip;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.GenreViewHolder> {

    private final List<Genre> genreList;
    private final Context context;
    private final OnGenreClickListener listener;
    private int selectedPosition = 0; // Default to "All" (first position)

    public interface OnGenreClickListener {
        void onGenreClick(Genre genre, int position);
    }

    public GenreAdapter(Context context, List<Genre> genreList, OnGenreClickListener listener) {
        this.context = context;
        this.genreList = genreList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GenreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_genre, parent, false);
        return new GenreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GenreViewHolder holder, int position) {
        Genre genre = genreList.get(position);
        holder.chipGenre.setText(genre.getName());

        // Set the selected state
        boolean isSelected = position == selectedPosition;
        holder.chipGenre.setChecked(isSelected);
        
        // Change chip appearance based on selection state
        if (isSelected) {
            holder.chipGenre.setChipBackgroundColorResource(R.color.purple_primary);
            holder.chipGenre.setTextColor(ContextCompat.getColor(context, android.R.color.white));
        } else {
            holder.chipGenre.setChipBackgroundColorResource(android.R.color.transparent);
            holder.chipGenre.setTextColor(ContextCompat.getColor(context, R.color.purple_primary));
        }

        holder.chipGenre.setOnClickListener(v -> {
            if (selectedPosition != position) {
                int oldSelected = selectedPosition;
                selectedPosition = position;
                notifyItemChanged(oldSelected);
                notifyItemChanged(selectedPosition);
                
                if (listener != null) {
                    listener.onGenreClick(genre, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return genreList.size();
    }

    static class GenreViewHolder extends RecyclerView.ViewHolder {
        Chip chipGenre;

        GenreViewHolder(@NonNull View itemView) {
            super(itemView);
            chipGenre = (Chip) itemView;
        }
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < getItemCount()) {
            int oldSelected = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(oldSelected);
            notifyItemChanged(selectedPosition);
        }
    }
}
