package com.socialhazard.app.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;

import com.socialhazard.app.model.AnswerCardUiModel;
import com.socialhazard.app.model.PlayerSlotUiModel;
import com.socialhazard.app.model.ScoreRowUiModel;
import com.socialhazard.app.model.SubmissionCardUiModel;

final class UiDiffCallbacks {

    static final DiffUtil.ItemCallback<AnswerCardUiModel> ANSWER_CARDS = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull AnswerCardUiModel oldItem, @NonNull AnswerCardUiModel newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull AnswerCardUiModel oldItem, @NonNull AnswerCardUiModel newItem) {
            return oldItem.getText().equals(newItem.getText())
                    && oldItem.getFooter().equals(newItem.getFooter())
                    && oldItem.isSelected() == newItem.isSelected();
        }
    };

    static final DiffUtil.ItemCallback<SubmissionCardUiModel> SUBMISSIONS = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull SubmissionCardUiModel oldItem, @NonNull SubmissionCardUiModel newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull SubmissionCardUiModel oldItem, @NonNull SubmissionCardUiModel newItem) {
            return oldItem.getLabel().equals(newItem.getLabel())
                    && oldItem.getText().equals(newItem.getText())
                    && oldItem.isSelected() == newItem.isSelected();
        }
    };

    static final DiffUtil.ItemCallback<PlayerSlotUiModel> PLAYER_SLOTS = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull PlayerSlotUiModel oldItem, @NonNull PlayerSlotUiModel newItem) {
            return oldItem.getSeatLabel().equals(newItem.getSeatLabel());
        }

        @Override
        public boolean areContentsTheSame(@NonNull PlayerSlotUiModel oldItem, @NonNull PlayerSlotUiModel newItem) {
            return oldItem.getDisplayName().equals(newItem.getDisplayName())
                    && oldItem.getAvatarId().equals(newItem.getAvatarId())
                    && oldItem.getSubtitle().equals(newItem.getSubtitle())
                    && oldItem.getSeatLabel().equals(newItem.getSeatLabel())
                    && oldItem.isOccupied() == newItem.isOccupied()
                    && oldItem.isHost() == newItem.isHost()
                    && oldItem.getAccentColor() == newItem.getAccentColor();
        }
    };

    static final DiffUtil.ItemCallback<ScoreRowUiModel> SCORE_ROWS = new DiffUtil.ItemCallback<>() {
        @Override
        public boolean areItemsTheSame(@NonNull ScoreRowUiModel oldItem, @NonNull ScoreRowUiModel newItem) {
            return oldItem.getRank().equals(newItem.getRank())
                    && oldItem.getDisplayName().equals(newItem.getDisplayName())
                    && oldItem.getAvatarId().equals(newItem.getAvatarId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ScoreRowUiModel oldItem, @NonNull ScoreRowUiModel newItem) {
            return oldItem.getSummary().equals(newItem.getSummary())
                    && oldItem.getScore() == newItem.getScore()
                    && oldItem.isHighlighted() == newItem.isHighlighted();
        }
    };

    private UiDiffCallbacks() {
    }
}
