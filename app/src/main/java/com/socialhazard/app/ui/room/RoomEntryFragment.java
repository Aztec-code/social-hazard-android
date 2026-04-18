package com.socialhazard.app.ui.room;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentRoomEntryBinding;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.RoomEntryViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class RoomEntryFragment extends BaseFragment<FragmentRoomEntryBinding> {

    public static final String ARG_ENTRY_MODE = "entry_mode";

    private boolean bindingState;

    public static Bundle argsFor(boolean createMode) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ENTRY_MODE, createMode ? "create" : "join");
        return bundle;
    }

    @Override
    protected FragmentRoomEntryBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentRoomEntryBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RoomEntryViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(RoomEntryViewModel.class);
        configureToolbar(getBinding().toolbar, true);
        if (savedInstanceState == null) {
            boolean createMode = !"join".equals(requireArguments().getString(ARG_ENTRY_MODE, "create"));
            viewModel.seed(createMode);
        }

        getBinding().roomCodeInputLayout.getEditText().setFilters(
                new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.LengthFilter(4)}
        );

        getBinding().entryModeGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked || bindingState) {
                return;
            }
            viewModel.setCreateMode(checkedId == R.id.create_mode_button);
        });
        getBinding().roomCodeInputLayout.getEditText().addTextChangedListener(new com.socialhazard.app.util.SimpleTextWatcher() {
            @Override
            public void afterTextChanged(android.text.Editable editable) {
                if (!bindingState) {
                    viewModel.setRoomCode(editable.toString());
                }
            }
        });
        getBinding().primaryButton.setOnClickListener(v -> viewModel.submit());

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            bindingState = true;
            FragmentRoomEntryBinding binding = getBinding();
            binding.headline.setText(state.getHeadline());
            binding.body.setText(state.getBody());
            binding.roomCodeInputLayout.setError(state.getRoomCodeError());
            if (binding.roomCodeInputLayout.getEditText() != null
                    && !state.getRoomCode().contentEquals(binding.roomCodeInputLayout.getEditText().getText())) {
                binding.roomCodeInputLayout.getEditText().setText(state.getRoomCode());
                binding.roomCodeInputLayout.getEditText().setSelection(binding.roomCodeInputLayout.getEditText().getText().length());
            }
            binding.entryModeGroup.check(state.isCreateMode() ? R.id.create_mode_button : R.id.join_mode_button);
            binding.roomCodeInputLayout.setVisibility(state.isCreateMode() ? View.GONE : View.VISIBLE);
            binding.primaryButton.setText(state.getPrimaryLabel());
            binding.primaryButton.setEnabled(state.isPrimaryEnabled());
            binding.entryModeGroup.setEnabled(!state.isLoading());
            bindingState = false;
        });
    }
}
