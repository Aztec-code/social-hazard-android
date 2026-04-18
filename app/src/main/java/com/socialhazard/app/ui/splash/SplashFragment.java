package com.socialhazard.app.ui.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentSplashBinding;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.SplashViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class SplashFragment extends BaseFragment<FragmentSplashBinding> {

    @Override
    protected FragmentSplashBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSplashBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SplashViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(SplashViewModel.class);
        viewModel.getNavigationEvents().observe(getViewLifecycleOwner(), event -> {
            SplashViewModel.Destination destination = event.getContentIfNotHandled();
            if (destination == SplashViewModel.Destination.HOME) {
                navController().navigate(R.id.action_splashFragment_to_homeFragment);
            } else if (destination == SplashViewModel.Destination.PROFILE) {
                navController().navigate(R.id.action_splashFragment_to_profileSetupFragment);
            }
        });
        viewModel.start();
    }
}
