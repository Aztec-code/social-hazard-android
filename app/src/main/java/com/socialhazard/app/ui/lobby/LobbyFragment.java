package com.socialhazard.app.ui.lobby;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.socialhazard.app.R;
import com.socialhazard.app.databinding.FragmentLobbyBinding;
import com.socialhazard.app.ui.adapter.PlayerAdapter;
import com.socialhazard.app.ui.base.BaseFragment;
import com.socialhazard.app.viewmodel.LobbyViewModel;
import com.socialhazard.app.viewmodel.ViewModelFactory;

public final class LobbyFragment extends BaseFragment<FragmentLobbyBinding> {

    private final PlayerAdapter playerAdapter = new PlayerAdapter();

    @Override
    protected FragmentLobbyBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentLobbyBinding.inflate(inflater, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LobbyViewModel viewModel = new ViewModelProvider(this, ViewModelFactory.from(this)).get(LobbyViewModel.class);
        configureToolbar(getBinding().toolbar, true);
        getBinding().toolbar.setNavigationOnClickListener(v -> viewModel.leaveRoom());

        getBinding().playerList.setLayoutManager(new LinearLayoutManager(requireContext()));
        getBinding().playerList.setAdapter(playerAdapter);
        getBinding().startMatchButton.setOnClickListener(v -> viewModel.onPrimaryAction());
        getBinding().openSettingsButton.setOnClickListener(v ->
                navController().navigate(R.id.action_lobbyFragment_to_settingsFragment)
        );

        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            FragmentLobbyBinding binding = getBinding();
            binding.roomCodeValue.setText(state.getRoomCode());
            binding.modeChip.setText(state.getModeLabel());
            binding.scoreChip.setText(state.getTargetScoreLabel());
            binding.populationValue.setText(state.getPopulationLabel());
            binding.readinessValue.setText(state.getReadinessLabel());
            binding.body.setText(state.getBody());
            binding.startMatchButton.setText(state.getPrimaryLabel());
            binding.startMatchButton.setEnabled(state.isPrimaryEnabled());
            binding.playerListState.setText(state.getPlayerStateMessage());
            binding.playerListState.setVisibility(state.getPlayerStateMessage() == null ? View.GONE : View.VISIBLE);
            playerAdapter.submitList(state.getPlayers());
            binding.playerList.scheduleLayoutAnimation();
        });
    }
}
