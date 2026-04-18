package com.socialhazard.app.ui.scoreboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.socialhazard.app.databinding.FragmentFinalScoreboardBinding;
import com.socialhazard.app.model.ScoreRowUiModel;
import com.socialhazard.app.ui.adapter.ScoreboardAdapter;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.FinalScoreboardViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class FinalScoreboardFragment extends BaseFragment<FragmentFinalScoreboardBinding> {

    private final ScoreboardAdapter scoreboardAdapter = new ScoreboardAdapter();

    @Override
    protected FragmentFinalScoreboardBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentFinalScoreboardBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FinalScoreboardViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(FinalScoreboardViewModel.class);
        configureToolbar(getBinding().toolbar, false);
        getBinding().scoreboardList.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().scoreboardList.setAdapter(scoreboardAdapter);
        getBinding().returnHomeButton.setOnClickListener(v -> viewModel.leaveRoom());

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            FragmentFinalScoreboardBinding binding = getBinding();
            binding.title.setText(state.getTitle());
            binding.body.setText(state.getBody());
            binding.returnHomeButton.setText(state.getPrimaryLabel());
            binding.scoreboardState.setText(state.getScoreStateMessage());
            binding.scoreboardState.setVisibility(state.getScoreStateMessage() == null ? View.GONE : View.VISIBLE);
            scoreboardAdapter.submitList(state.getScores());
            bindFeaturedWinner(binding, state.getScores());
            binding.scoreboardList.scheduleLayoutAnimation();
        });
    }

    private void bindFeaturedWinner(FragmentFinalScoreboardBinding binding, java.util.List<ScoreRowUiModel> scores) {
        if (scores == null || scores.isEmpty()) {
            binding.featuredWinnerCard.setVisibility(View.GONE);
            return;
        }
        ScoreRowUiModel winner = scores.get(0);
        binding.featuredWinnerCard.setVisibility(View.VISIBLE);
        binding.featuredWinnerName.setText(winner.getDisplayName());
        binding.featuredWinnerScore.setText(getString(com.socialhazard.app.R.string.scoreboard_feature_score, winner.getScore()));
        binding.featuredWinnerSummary.setText(winner.getSummary());
    }
}
