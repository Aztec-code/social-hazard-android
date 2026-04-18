package com.socialhazard.app.ui.result;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentRoundResultBinding;
import com.socialhazard.app.ui.adapter.ScoreboardAdapter;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.RoundResultViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class RoundResultFragment extends BaseFragment<FragmentRoundResultBinding> {

    private final ScoreboardAdapter scoreboardAdapter = new ScoreboardAdapter();

    @Override
    protected FragmentRoundResultBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentRoundResultBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoundResultViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(RoundResultViewModel.class);
        configureToolbar(getBinding().toolbar, true);
        getBinding().toolbar.setNavigationOnClickListener(v -> viewModel.leaveRoom());
        getBinding().scoreboardList.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().scoreboardList.setAdapter(scoreboardAdapter);
        getBinding().primaryButton.setOnClickListener(v -> viewModel.continueToNextScreen());

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            FragmentRoundResultBinding binding = getBinding();
            binding.winnerName.setText(state.getWinnerName());
            binding.winningAnswer.setText(state.getWinningAnswer());
            binding.body.setText(state.getBody());
            binding.primaryButton.setText(state.getPrimaryLabel());
            binding.scoreboardState.setText(state.getScoreStateMessage());
            binding.scoreboardState.setVisibility(state.getScoreStateMessage() == null ? View.GONE : View.VISIBLE);
            scoreboardAdapter.submitList(state.getScores());
            binding.scoreboardList.scheduleLayoutAnimation();
        });
    }
}
