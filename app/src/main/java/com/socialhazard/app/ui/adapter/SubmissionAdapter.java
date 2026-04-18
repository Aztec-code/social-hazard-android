package com.socialhazard.app.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.socialhazard.app.model.SubmissionCardUiModel;
import com.socialhazard.app.databinding.ItemSubmissionCardBinding;

public final class SubmissionAdapter extends ListAdapter<SubmissionCardUiModel, SubmissionAdapter.SubmissionViewHolder> {

    public interface Listener {
        void onSubmissionSelected(String submissionId);
    }

    private final Listener listener;

    public SubmissionAdapter(Listener listener) {
        super(UiDiffCallbacks.SUBMISSIONS);
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubmissionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new SubmissionViewHolder(ItemSubmissionCardBinding.inflate(inflater, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull SubmissionViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static final class SubmissionViewHolder extends RecyclerView.ViewHolder {

        private final ItemSubmissionCardBinding binding;
        private final Listener listener;

        SubmissionViewHolder(ItemSubmissionCardBinding binding, Listener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
        }

        void bind(SubmissionCardUiModel item) {
            binding.submissionLabel.setText(item.getLabel());
            binding.submissionText.setText(item.getText());
            binding.selectionBadge.setVisibility(item.isSelected() ? View.VISIBLE : View.GONE);
            binding.submissionCard.setStrokeWidth(item.isSelected() ? 3 : 1);
            binding.submissionCard.setStrokeColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isSelected() ? com.socialhazard.app.R.color.sh_primary : com.socialhazard.app.R.color.sh_outline
            ));
            binding.submissionCard.setCardBackgroundColor(ContextCompat.getColor(
                    binding.getRoot().getContext(),
                    item.isSelected() ? com.socialhazard.app.R.color.sh_surface_emphasis : com.socialhazard.app.R.color.sh_surface_alt
            ));
            binding.submissionCard.setAlpha(item.isSelected() ? 1f : 0.96f);
            binding.submissionCard.setOnClickListener(view -> listener.onSubmissionSelected(item.getId()));
        }
    }
}
