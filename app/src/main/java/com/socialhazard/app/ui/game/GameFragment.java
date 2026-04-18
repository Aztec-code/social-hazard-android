package com.socialhazard.app.ui.game;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentGameBinding;
import com.socialhazard.app.ui.adapter.AnswerCardAdapter;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.GameViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class GameFragment extends BaseFragment<FragmentGameBinding> {

    private GameViewModel viewModel;
    private final AnswerCardAdapter adapter = new AnswerCardAdapter(cardId -> {
        if (viewModel != null) {
            viewModel.selectCard(cardId);
        }
    });

    @Override
    protected FragmentGameBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentGameBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(GameViewModel.class);
        configureToolbar(getBinding().toolbar, true);
        getBinding().toolbar.setNavigationOnClickListener(v -> viewModel.leaveRoom());
        getBinding().answerList.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().answerList.setAdapter(adapter);
        getBinding().submitButton.setOnClickListener(v -> viewModel.submitSelectedCard());

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            FragmentGameBinding binding = getBinding();
            binding.soloShowcaseLabel.setVisibility(state.isDemoActive() ? View.VISIBLE : View.GONE);
            binding.judgeLabel.setText(state.getJudgeLabel());
            binding.promptText.setText(state.getPromptText());
            binding.promptMeta.setText(state.getPromptMeta());
            binding.submitButton.setText(state.getPrimaryLabel());
            binding.submitButton.setEnabled(state.isPrimaryEnabled());
            binding.answerListState.setText(state.getHandStateMessage());
            binding.answerListState.setVisibility(state.getHandStateMessage() == null ? View.GONE : View.VISIBLE);
            adapter.submitList(state.getAnswers());
            binding.answerList.scheduleLayoutAnimation();
        });
    }
}
