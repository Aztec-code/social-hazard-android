package com.socialhazard.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.socialhazard.app.model.AnswerCardUiModel;
import com.socialhazard.app.databinding.ItemAnswerCardBinding;

public final class AnswerCardAdapter extends ListAdapter<AnswerCardUiModel, AnswerCardAdapter.AnswerCardViewHolder> {

    public interface Listener {
        void onCardSelected(String cardId);
    }

    private final Listener listener;

    public AnswerCardAdapter(Listener listener) {
        super(UiDiffCallbacks.ANSWER_CARDS);
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnswerCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new AnswerCardViewHolder(ItemAnswerCardBinding.inflate(inflater, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerCardViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static final class AnswerCardViewHolder extends RecyclerView.ViewHolder {

        private final ItemAnswerCardBinding binding;
        private final Listener listener;

        AnswerCardViewHolder(ItemAnswerCardBinding binding, Listener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(AnswerCardUiModel item) {
            binding.answerText.setText(item.getText());
            binding.answerFooter.setText(item.getFooter());
            binding.selectionBadge.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
            binding.answerCard.setStrokeWidth(item.isSelected() ? 3 : 1);
            binding.answerCard.setStrokeColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isSelected() ? com.socialhazard.app.R.color.sh_primary : com.socialhazard.app.R.color.sh_outline
            ));
            binding.answerCard.setCardBackgroundColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isSelected() ? com.socialhazard.app.R.color.sh_surface_emphasis : com.socialhazard.app.R.color.sh_surface_alt
            ));
            binding.answerCard.setAlpha(item.isSelected() ? 1f : 0.96f);
            binding.answerCard.setOnClickListener(view -> listener.onCardSelected(item.getId()));
        }
    }
}
