package com.socialhazard.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.socialhazard.app.model.ScoreRowUiModel;
import com.socialhazard.app.databinding.ItemScoreRowBinding;
import com.socialhazard.app.util.AvatarCatalog;

public final class ScoreboardAdapter extends ListAdapter<ScoreRowUiModel, ScoreboardAdapter.ScoreViewHolder> {

    public ScoreboardAdapter() {
        super(UiDiffCallbacks.SCORE_ROWS);
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ScoreViewHolder(ItemScoreRowBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static final class ScoreViewHolder extends RecyclerView.ViewHolder {

        private final ItemScoreRowBinding binding;

        ScoreViewHolder(ItemScoreRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(ScoreRowUiModel item) {
            binding.rankValue.setText(item.getRank());
            binding.avatarImage.setImageResource(AvatarCatalog.drawableFor(item.getAvatarId()));
            binding.playerName.setText(item.getDisplayName());
            binding.playerSummary.setText(item.getSummary());
            binding.scoreValue.setText(String.valueOf(item.getScore()));
            binding.scoreRowCard.setStrokeWidth(item.isHighlighted() ? 3 : 1);
            binding.scoreRowCard.setStrokeColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isHighlighted() ? com.socialhazard.app.R.color.sh_primary : com.socialhazard.app.R.color.sh_outline
            ));
            binding.scoreRowCard.setCardBackgroundColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isHighlighted() ? com.socialhazard.app.R.color.sh_surface_emphasis : com.socialhazard.app.R.color.sh_surface_alt
            ));
            binding.rankValue.setTextColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isHighlighted() ? com.socialhazard.app.R.color.sh_primary : com.socialhazard.app.R.color.sh_text_secondary
            ));
            binding.scoreRowCard.setAlpha(item.isHighlighted() ? 1f : 0.96f);
        }
    }
}
