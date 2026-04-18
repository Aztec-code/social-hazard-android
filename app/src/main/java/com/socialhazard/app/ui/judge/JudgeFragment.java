package com.socialhazard.app.ui.judge;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentJudgeBinding;
import com.socialhazard.app.ui.adapter.SubmissionAdapter;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.JudgeViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class JudgeFragment extends BaseFragment<FragmentJudgeBinding> {

    private JudgeViewModel viewModel;
    private final SubmissionAdapter adapter = new SubmissionAdapter(submissionId -> {
        if (viewModel != null) {
            viewModel.selectSubmission(submissionId);
        }
    });

    @Override
    protected FragmentJudgeBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentJudgeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(JudgeViewModel.class);
        configureToolbar(getBinding().toolbar, true);
        getBinding().toolbar.setNavigationOnClickListener(v -> viewModel.leaveRoom());
        getBinding().submissionList.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().submissionList.setAdapter(adapter);
        getBinding().confirmButton.setOnClickListener(v -> viewModel.confirmWinner());

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            FragmentJudgeBinding binding = getBinding();
            binding.soloShowcaseLabel.setVisibility(state.isDemoActive() ? View.VISIBLE : View.GONE);
            binding.judgeLabel.setText(state.getJudgeLabel());
            binding.promptText.setText(state.getPromptText());
            binding.confirmButton.setText(state.getPrimaryLabel());
            binding.confirmButton.setEnabled(state.isPrimaryEnabled());
            binding.submissionListState.setText(state.getSubmissionStateMessage());
            binding.submissionListState.setVisibility(state.getSubmissionStateMessage() == null ? View.GONE : View.VISIBLE);
            adapter.submitList(state.getSubmissions());
            binding.submissionList.scheduleLayoutAnimation();
        });
    }
}
