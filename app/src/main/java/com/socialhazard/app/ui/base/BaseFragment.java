package com.socialhazard.app.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.viewbinding.ViewBinding;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.transition.MaterialFadeThrough;

public abstract class BaseFragment<T extends ViewBinding> extends Fragment {

    private T binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MaterialFadeThrough enterTransition = new MaterialFadeThrough();
        enterTransition.setDuration(240L);
        setEnterTransition(enterTransition);
        setReenterTransition(enterTransition);

        MaterialFadeThrough exitTransition = new MaterialFadeThrough();
        exitTransition.setDuration(200L);
        setExitTransition(exitTransition);
        setReturnTransition(exitTransition);
    }

    @Nullable
    @Override
    public final View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = createBinding(inflater, container);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    protected abstract T createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container);

    protected final T getBinding() {
        if (binding == null) {
            throw new IllegalStateException("Binding accessed outside of view lifecycle.");
        }
        return binding;
    }

    protected final NavController navController() {
        return NavHostFragment.findNavController(this);
    }

    protected final void configureToolbar(@NonNull MaterialToolbar toolbar, boolean showBack) {
        if (showBack) {
            toolbar.setNavigationOnClickListener(view -> navController().navigateUp());
        } else {
            toolbar.setNavigationIcon(null);
        }
    }
}
