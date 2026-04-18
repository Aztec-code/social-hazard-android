package com.socialhazard.app.ui.adapter;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.socialhazard.app.model.PlayerSlotUiModel;
import com.socialhazard.app.databinding.ItemPlayerSlotBinding;
import com.socialhazard.app.util.AvatarCatalog;

public final class PlayerAdapter extends ListAdapter<PlayerSlotUiModel, PlayerAdapter.PlayerViewHolder> {

    public PlayerAdapter() {
        super(UiDiffCallbacks.PLAYER_SLOTS);
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PlayerViewHolder(ItemPlayerSlotBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static final class PlayerViewHolder extends RecyclerView.ViewHolder {

        private final ItemPlayerSlotBinding binding;

        PlayerViewHolder(ItemPlayerSlotBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PlayerSlotUiModel item) {
            binding.playerName.setText(item.getDisplayName());
            binding.avatarImage.setImageResource(AvatarCatalog.drawableFor(item.getAvatarId()));
            binding.playerSubtitle.setText(item.getSubtitle());
            binding.playerSeat.setText(item.getSeatLabel());
            binding.playerAccent.setBackgroundTintList(ColorStateList.valueOf(item.getAccentColor()));
            binding.hostBadge.setVisibility(item.isHost() ? android.view.View.VISIBLE : android.view.View.GONE);
            binding.slotCard.setStrokeColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isHost() ? com.socialhazard.app.R.color.sh_primary : com.socialhazard.app.R.color.sh_outline
            ));
            binding.slotCard.setAlpha(item.isOccupied() ? 1f : 0.68f);
        }
    }
}
