package io.github.kdetard.koki.feature.base;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.ControllerChangeHandler;
import com.bluelinelabs.conductor.ControllerChangeType;
import com.google.android.material.navigation.NavigationBarView;

import autodispose2.lifecycle.LifecycleScopeProvider;
import dev.chrisbanes.insetter.Insetter;
import io.github.kdetard.koki.utils.InsetUtils;

public abstract class BaseController extends Controller implements NavigationProvider, OnConfigurationListener {
    private final int layoutRes;

    public BaseController(int layoutRes) {
        this(layoutRes, null);
    }

    public BaseController(int layoutRes, @Nullable Bundle args) {
        super(args);
        this.layoutRes = layoutRes;
        ControllerUtils.watchForLeaks(this);
        addLifecycleListener(new LifecycleListener() {
            @Override
            public void postCreateView(@NonNull Controller controller, @NonNull View view) {
                super.postCreateView(controller, view);
                onViewCreated(view);
            }
        });
    }

    public int getLayoutRes() {
        return layoutRes;
    }

    public LifecycleScopeProvider<ControllerEvent> getScopeProvider() { return ControllerScopeProvider.from(this); }

    public View getRoot() {
        final var provider = ((NavigationProvider)getActivity());
        if (provider != null) {
            return provider.getRoot();
        }
        return null;
    }

    public Toolbar getToolbar() {
        final var provider = ((NavigationProvider)getActivity());
        if (provider != null) {
            return provider.getToolbar();
        }
        return null;
    }

    public ExpandedAppBarLayout getAppBarLayout() {
        final var provider = ((NavigationProvider)getActivity());
        if (provider != null) {
            return provider.getAppBarLayout();
        }
        return null;
    }

    public NavigationBarView getNavBar() {
        final var provider = ((NavigationProvider)getActivity());
        if (provider != null) {
            return provider.getNavBar();
        }
        return null;
    }

    protected String title = null;

    @NonNull
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @NonNull ViewGroup container,
                             Bundle savedViewState) {
        return inflater.inflate(layoutRes, container, false);
    }

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

    public void configureToolbar(Toolbar toolbar) {
        if (title == null) {
            return;
        }

        var parentController = getParentController();
        while (parentController != null) {
            if (parentController instanceof BaseController && ((BaseController) parentController).title != null) {
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
