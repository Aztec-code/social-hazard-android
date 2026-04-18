package com.socialhazard.app.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;

import com.google.android.material.snackbar.Snackbar;
import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentSettingsBinding;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.SettingsViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class SettingsFragment extends BaseFragment<FragmentSettingsBinding> {

    private boolean bindingState;

    @Override
    protected FragmentSettingsBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSettingsBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SettingsViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(SettingsViewModel.class);
        configureToolbar(getBinding().toolbar, true);

        getBinding().soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingState) {
                viewModel.setSoundEnabled(isChecked);
            }
        });
        getBinding().hapticsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingState) {
                viewModel.setHapticsEnabled(isChecked);
            }
        });
        getBinding().motionSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!bindingState) {
                viewModel.setMotionEnabled(isChecked);
            }
        });
        getBinding().changeProfileButton.setOnClickListener(v -> viewModel.restartProfileSetup());
        getBinding().buildValue.setOnClickListener(v -> viewModel.onVersionTapped());
        getBinding().openDemoButton.setOnClickListener(v ->
                navController().navigate(R.id.action_settingsFragment_to_demoModeFragment)
        );
        getBinding().lockDemoButton.setOnClickListener(v -> viewModel.relockDemoMode());

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            bindingState = true;
            FragmentSettingsBinding binding = getBinding();
            binding.soundSwitch.setChecked(state.isSoundEnabled());
            binding.hapticsSwitch.setChecked(state.isHapticsEnabled());
            binding.motionSwitch.setChecked(state.isMotionEnabled());
            binding.demoCard.setVisibility(state.isDemoUnlocked() ? View.VISIBLE : View.GONE);
            binding.demoHint.setVisibility(state.isDemoUnlocked() ? View.GONE : View.VISIBLE);
            bindingState = false;
        });

        viewModel.getRestartProfileEvents().observe(getViewLifecycleOwner(), event -> {
            Boolean shouldRestart = event.getContentIfNotHandled();
            if (Boolean.TRUE.equals(shouldRestart)) {
                NavOptions options = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setPopUpTo(R.id.homeFragment, true)
                        .build();
                navController().navigate(R.id.profileSetupFragment, null, options);
            }
        });

        viewModel.getMessages().observe(getViewLifecycleOwner(), event -> {
            String message = event.getContentIfNotHandled();
            if (message != null) {
                Snackbar.make(getBinding().getRoot(), message, Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
