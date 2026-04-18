package com.socialhazard.app.ui.profile;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentProfileSetupBinding;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.util.AvatarCatalog;
import com.socialhazard.app.util.SimpleTextWatcher;
import com.socialhazard.app.viewmodel.ProfileSetupViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class ProfileSetupFragment extends BaseFragment<FragmentProfileSetupBinding> {

    private boolean bindingState;

    @Override
    protected FragmentProfileSetupBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentProfileSetupBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ProfileSetupViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(ProfileSetupViewModel.class);

        getBinding().nicknameInputLayout.getEditText().addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!bindingState) {
                    viewModel.setNickname(editable.toString());
                }
            }
        });
        getBinding().leftAvatarButton.setOnClickListener(v -> viewModel.showPreviousAvatar());
        getBinding().rightAvatarButton.setOnClickListener(v -> viewModel.showNextAvatar());
        getBinding().continueButton.setOnClickListener(v -> viewModel.saveProfile());

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            bindingState = true;
            FragmentProfileSetupBinding binding = getBinding();
            binding.avatarImage.setImageResource(AvatarCatalog.drawableFor(state.getAvatarId()));
            binding.nicknameInputLayout.setError(state.getNicknameError());
            if (binding.nicknameInputLayout.getEditText() != null
                    && !state.getNickname().contentEquals(binding.nicknameInputLayout.getEditText().getText())) {
                binding.nicknameInputLayout.getEditText().setText(state.getNickname());
                binding.nicknameInputLayout.getEditText().setSelection(binding.nicknameInputLayout.getEditText().getText().length());
            }
            bindingState = false;
        });

        viewModel.getContinueEvents().observe(getViewLifecycleOwner(), event -> {
            Boolean shouldContinue = event.getContentIfNotHandled();
            if (Boolean.TRUE.equals(shouldContinue)) {
                navController().navigate(R.id.action_profileSetupFragment_to_homeFragment);
            }
        });
    }
}
