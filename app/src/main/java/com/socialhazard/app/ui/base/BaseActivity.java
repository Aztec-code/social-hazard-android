package com.socialhazard.app.ui.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewbinding.ViewBinding;

public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {

    private T binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = createBinding();
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (view, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });
        onBindingCreated(binding, savedInstanceState);
    }

    protected abstract T createBinding();

    protected void onBindingCreated(T binding, @Nullable Bundle savedInstanceState) {
    }

    protected final T getBinding() {
        return binding;
    }
}
