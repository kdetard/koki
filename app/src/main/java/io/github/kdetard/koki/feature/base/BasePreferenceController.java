package io.github.kdetard.koki.feature.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceController;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Objects;

import autodispose2.lifecycle.LifecycleScopeProvider;
import dev.chrisbanes.insetter.Insetter;
import io.github.kdetard.koki.conductor.ControllerEvent;
import io.github.kdetard.koki.conductor.ControllerScopeProvider;
import io.github.kdetard.koki.conductor.ControllerUtils;
import io.github.kdetard.koki.conductor.OnConfigurationChangeListener;
import io.github.kdetard.koki.inset.InsetUtils;

public abstract class BasePreferenceController extends PreferenceController implements ActivityLayoutProvider, OnConfigurationChangeListener {
    public BasePreferenceController() {
        this(null);
    }

    public BasePreferenceController(@Nullable Bundle args) {
        super(args);
        ControllerUtils.watchForLeaks(this);
        addLifecycleListener(new LifecycleListener() {
            @Override
            public void postCreateView(@NonNull Controller controller, @NonNull View view) {
                super.postCreateView(controller, view);
                onViewCreated(view);
            }
        });
    }

    public LifecycleScopeProvider<ControllerEvent> getScopeProvider() { return ControllerScopeProvider.from(this); }

    public View getRoot() {
        final var provider = ((ActivityLayoutProvider)getActivity());
        if (provider != null) {
            return provider.getRoot();
        }
        return null;
    }

    public Toolbar getToolbar() {
        final var provider = ((ActivityLayoutProvider)getActivity());
        if (provider != null) {
            return provider.getToolbar();
        }
        return null;
    }

    public ExpandedAppBarLayout getAppBarLayout() {
        final var provider = ((ActivityLayoutProvider)getActivity());
        if (provider != null) {
            return provider.getAppBarLayout();
        }
        return null;
    }

    public NavigationBarView getNavBar() {
        final var provider = ((ActivityLayoutProvider)getActivity());
        if (provider != null) {
            return provider.getNavBar();
        }
        return null;
    }

    protected String title = null;

    public void onViewCreated(View view) {}

    @Override
    public void onChangeStarted(@NonNull ControllerChangeHandler changeHandler, @NonNull ControllerChangeType changeType) {
        super.onChangeStarted(changeHandler, changeType);

        if (changeType.isEnter) {
            if (getToolbar() != null) {
                configureMenu(getToolbar());
            }
        }
    }

    public FragmentManager getSupportFragmentManager() {
        return ((FragmentActivity) Objects.requireNonNull(getActivity())).getSupportFragmentManager();
    }

    public void configureToolbar(Toolbar toolbar) {
        if (title == null) {
            return;
        }

        var parentController = getParentController();
        while (parentController != null) {
            if (parentController instanceof BasePreferenceController && ((BasePreferenceController) parentController).title != null) {
                return;
            }
            parentController = parentController.getParentController();
        }

        toolbar.setTitle(title);
    }

    public void configureMenu(Toolbar toolbar) {
        toolbar.getMenu().clear();
    }

    @Override
    public void onConfigurationChange(@NonNull Configuration newConfig) {
        final boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        final int orientationInsetType = isLandscape ? InsetUtils.LandscapeInsetType : InsetUtils.OutOfBoundInsetType;

        Insetter.builder()
                .paddingLeft(orientationInsetType, false)
                .paddingRight(orientationInsetType, false)
                .applyToView(getRoot());
    }
}
