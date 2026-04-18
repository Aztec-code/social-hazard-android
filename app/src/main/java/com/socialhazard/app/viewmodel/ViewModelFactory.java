package com.socialhazard.app.viewmodel;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.socialhazard.app.SocialHazardApplication;
import com.socialhazard.app.repository.GameRepository;

public final class ViewModelFactory implements ViewModelProvider.Factory {

    private final GameRepository repository;

    public ViewModelFactory(GameRepository repository) {
        this.repository = repository;
    }

    public static ViewModelFactory from(@NonNull Fragment fragment) {
        return ((SocialHazardApplication) fragment.requireActivity().getApplication()).getViewModelFactory();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass == SplashViewModel.class) {
            return (T) new SplashViewModel(repository);
        }
        if (modelClass == HomeViewModel.class) {
            return (T) new HomeViewModel(repository);
        }
        if (modelClass == ProfileSetupViewModel.class) {
            return (T) new ProfileSetupViewModel(repository);
        }
        if (modelClass == RoomEntryViewModel.class) {
            return (T) new RoomEntryViewModel(repository);
        }
        if (modelClass == LobbyViewModel.class) {
            return (T) new LobbyViewModel(repository);
        }
        if (modelClass == GameViewModel.class) {
            return (T) new GameViewModel(repository);
        }
        if (modelClass == JudgeViewModel.class) {
            return (T) new JudgeViewModel(repository);
        }
        if (modelClass == RoundResultViewModel.class) {
            return (T) new RoundResultViewModel(repository);
        }
        if (modelClass == FinalScoreboardViewModel.class) {
            return (T) new FinalScoreboardViewModel(repository);
        }
        if (modelClass == SettingsViewModel.class) {
            return (T) new SettingsViewModel(repository);
        }
        if (modelClass == DemoModeViewModel.class) {
            return (T) new DemoModeViewModel(repository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
