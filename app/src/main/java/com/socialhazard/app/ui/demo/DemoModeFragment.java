package com.socialhazard.app.ui.demo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentDemoModeBinding;
import com.socialhazard.app.model.DemoScene;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.DemoModeViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class DemoModeFragment extends BaseFragment<FragmentDemoModeBinding> {

    @Override
    protected FragmentDemoModeBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentDemoModeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DemoModeViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(DemoModeViewModel.class);
        configureToolbar(getBinding().toolbar, true);

        getBinding().startWalkthroughButton.setOnClickListener(v -> viewModel.startSoloWalkthrough());
        getBinding().toLobbyButton.setOnClickListener(v -> viewModel.openScene(DemoScene.LOBBY));
        getBinding().toGameButton.setOnClickListener(v -> viewModel.openScene(DemoScene.SUBMITTING));
        getBinding().toJudgeButton.setOnClickListener(v -> viewModel.openScene(DemoScene.JUDGING));
        getBinding().toResultButton.setOnClickListener(v -> viewModel.openScene(DemoScene.ROUND_RESULT));
        getBinding().toScoreboardButton.setOnClickListener(v -> viewModel.openScene(DemoScene.FINAL_SCOREBOARD));

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            FragmentDemoModeBinding binding = getBinding();
            binding.demoContent.setVisibility(state.isUnlocked() ? View.VISIBLE : View.GONE);
            binding.lockedNotice.setVisibility(state.isUnlocked() ? View.GONE : View.VISIBLE);
            binding.demoStatus.setText(state.isActive() ? R.string.demo_status_live : R.string.demo_status_idle);
        });
    }
}
