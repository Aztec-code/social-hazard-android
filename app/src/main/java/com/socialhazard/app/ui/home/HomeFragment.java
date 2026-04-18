package com.socialhazard.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentHomeBinding;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.ui.room.RoomEntryFragment;
import com.socialhazard.app.util.AvatarCatalog;
import com.socialhazard.app.viewmodel.HomeViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class HomeFragment extends BaseFragment<FragmentHomeBinding> {

    @Override
    protected FragmentHomeBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentHomeBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(HomeViewModel.class);

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            FragmentHomeBinding binding = getBinding();
            binding.eyebrow.setText(state.getEyebrow());
            binding.title.setText(state.getTitle());
            binding.roomHint.setText(state.getRoomHint());
            binding.avatarImage.setImageResource(AvatarCatalog.drawableFor(state.getAvatarId()));
        });

        getBinding().createRoomButton.setOnClickListener(v ->
                navController().navigate(R.id.action_homeFragment_to_roomEntryFragment, RoomEntryFragment.argsFor(true))
        );
        getBinding().joinRoomButton.setOnClickListener(v ->
                navController().navigate(R.id.action_homeFragment_to_roomEntryFragment, RoomEntryFragment.argsFor(false))
        );
        getBinding().settingsButton.setOnClickListener(v ->
                navController().navigate(R.id.action_homeFragment_to_settingsFragment)
        );
    }
}
