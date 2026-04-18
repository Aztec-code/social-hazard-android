package com.socialhazard.app.ui.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.socialhazard.app.R;
import com.socialhazard.app.SocialHazardApplication;
import com.socialhazard.app.databinding.ActivityMainBinding;
import com.socialhazard.app.model.GameScreen;
import com.socialhazard.app.model.GameSessionState;
import com.socialhazard.app.repository.GameRepository;
import com.socialhazard.app.util.BackgroundMusicController;

public final class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private GameRepository repository;
    private BackgroundMusicController backgroundMusicController;
    private NavController navController;
    private String lastErrorMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets(binding.getRoot());

        if (getApplication() instanceof SocialHazardApplication) {
            repository = ((SocialHazardApplication) getApplication()).getGameRepository();
        }
        backgroundMusicController = new BackgroundMusicController(this);

        binding.getRoot().post(() -> {
            navController = resolveNavController();
            if (navController != null) {
                navController.addOnDestinationChangedListener(
                        (controller, destination, arguments) -> {
                            updateStatusBarForDestination();
                            updateMusicForDestination();
                        }
                );
                updateStatusBarForDestination();
                updateMusicForDestination();
            }
        });

        if (repository != null) {
            repository.observeNavigationEvents().observe(this, event -> {
                GameScreen screen = event == null ? null : event.getContentIfNotHandled();
                if (screen != null) {
                    navigateTo(screen);
                }
            });
            repository.observeSessionState().observe(this, this::showConnectionFeedback);
            repository.observeSettingsContent().observe(this, settings ->
                    backgroundMusicController.setMusicEnabled(settings == null || settings.isSoundEnabled())
            );
        }

        if (savedInstanceState == null) {
            binding.getRoot().post(this::updateStatusBarForDestination);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        backgroundMusicController.onStart();
        if (repository != null) {
            repository.resumeSession();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (repository != null) {
            repository.resumeSession();
        }
        updateStatusBarForDestination();
        updateMusicForDestination();
    }

    @Override
    protected void onStop() {
        backgroundMusicController.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        backgroundMusicController.release();
        super.onDestroy();
    }

    private void navigateTo(GameScreen screen) {
        NavController controller = requireNavController();
        if (controller == null) {
            return;
        }
        int destinationId;
        switch (screen) {
            case HOME:
                destinationId = R.id.homeFragment;
                break;
            case LOBBY:
                destinationId = R.id.lobbyFragment;
                break;
            case GAME:
                destinationId = R.id.gameFragment;
                break;
            case JUDGE:
                destinationId = R.id.judgeFragment;
                break;
            case ROUND_RESULT:
                destinationId = R.id.roundResultFragment;
                break;
            case FINAL_SCOREBOARD:
                destinationId = R.id.finalScoreboardFragment;
                break;
            default:
                destinationId = R.id.homeFragment;
                break;
        }
        if (controller.getCurrentDestination() != null && controller.getCurrentDestination().getId() == destinationId) {
            return;
        }

        NavOptions.Builder options = new NavOptions.Builder().setLaunchSingleTop(true);
        options.setEnterAnim(R.anim.nav_enter_forward);
        options.setExitAnim(R.anim.nav_exit_forward);
        options.setPopEnterAnim(R.anim.nav_enter_back);
        options.setPopExitAnim(R.anim.nav_exit_back);
        if (screen == GameScreen.HOME) {
            options.setPopUpTo(R.id.homeFragment, false);
        }
        controller.navigate(destinationId, null, options.build());
    }

    private void updateStatusBarForDestination() {
        NavController controller = requireNavController();
        if (controller != null && controller.getCurrentDestination() != null) {
            binding.screenScrim.setAlpha(
                    controller.getCurrentDestination().getId() == R.id.splashFragment ? 0.28f : 0.14f
            );
        }
    }

    private void updateMusicForDestination() {
        NavController controller = requireNavController();
        if (controller == null || controller.getCurrentDestination() == null) {
            return;
        }
        backgroundMusicController.updateForDestination(controller.getCurrentDestination().getId());
    }

    @Nullable
    private NavController requireNavController() {
        if (navController == null) {
            navController = resolveNavController();
        }
        return navController;
    }

    @Nullable
    private NavController resolveNavController() {
        androidx.fragment.app.Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (fragment instanceof NavHostFragment) {
            return ((NavHostFragment) fragment).getNavController();
        }
        return null;
    }

    private void applyWindowInsets(@NonNull View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });
    }

    private void showConnectionFeedback(GameSessionState state) {
        if (state == null || state.getErrorMessage() == null || state.getErrorMessage().isBlank()) {
            lastErrorMessage = null;
            return;
        }
        if (state.getErrorMessage().equals(lastErrorMessage)) {
            return;
        }
        lastErrorMessage = state.getErrorMessage();
        Snackbar.make(binding.getRoot(), state.getErrorMessage(), Snackbar.LENGTH_SHORT).show();
    }
}
