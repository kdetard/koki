package io.github.kdetard.koki.feature.onboard;

import static autodispose2.AutoDispose.autoDisposable;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bluelinelabs.conductor.Router;
import com.bluelinelabs.conductor.Router.PopRootControllerMode;
import com.bluelinelabs.conductor.RouterTransaction;
import com.jakewharton.rxbinding4.view.RxView;

import java.util.Objects;

import dev.chrisbanes.insetter.Insetter;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.databinding.ControllerOnboardBinding;
import io.github.kdetard.koki.feature.auth.AuthController;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.feature.base.OnConfigurationListener;
import io.github.kdetard.koki.utils.InsetUtils;

public class OnboardController extends BaseController implements OnConfigurationListener {
    ControllerOnboardBinding binding;
    Drawable actionLayoutBackground;
    View.OnLayoutChangeListener actionLayoutChangeListener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
        final int statusBarInsets = Objects.requireNonNull(ViewCompat.getRootWindowInsets(v))
                .getInsets(WindowInsetsCompat.Type.statusBars()).top;

        final boolean isBehindStatusBar = top <= statusBarInsets;
        final int topInsetType = isBehindStatusBar ? WindowInsetsCompat.Type.statusBars() : InsetUtils.OutOfBoundInsetType;

        toggleBackgroundTint(v, actionLayoutBackground, isBehindStatusBar);

        Insetter.builder()
                .paddingTop(topInsetType, true)
                .paddingBottom(InsetUtils.PortraitInsetType, true)
                .applyToView(v);
    };

    public OnboardController() { super(R.layout.controller_onboard); }

    @Override
    public void onViewCreated(View view) {
        binding = ControllerOnboardBinding.bind(view);

        Router childRouter = getChildRouter(binding.onboardControllerActionLayoutContainerView)
                .setPopRootControllerMode(PopRootControllerMode.POP_ROOT_CONTROLLER_BUT_NOT_VIEW);

        if (!childRouter.hasRootController()) {
            childRouter.setRoot(RouterTransaction.with(new AuthController()));
        }

        RxView
                .clicks(binding.onboardControllerAppBarLayoutLangPickerBtn)
                .doOnNext(v -> Toast.makeText(getApplicationContext(), "Function not implemented", Toast.LENGTH_SHORT).show())
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        Insetter.builder()
                .paddingTop(WindowInsetsCompat.Type.systemBars(), true)
                .applyToView(binding.onboardControllerAppBarLayout);

        setPortraitInsets(binding.onboardControllerActionLayout);
    }

    private void setPortraitInsets(View actionLayout) {
        actionLayoutBackground = actionLayout.getBackground();
        actionLayout.addOnLayoutChangeListener(actionLayoutChangeListener);
    }

    @Override
    protected void onDestroyView(@NonNull View view) {
        super.onDestroyView(view);
        binding.onboardControllerActionLayout.removeOnLayoutChangeListener(actionLayoutChangeListener);
    }

    private static void toggleBackgroundTint(View view, Drawable originalBackground, boolean predicate) {
        if (predicate) {
            view.setBackgroundColor(Objects.requireNonNull(view.getBackgroundTintList()).getDefaultColor());
        } else {
            view.setBackgroundColor(0);
            view.setBackground(originalBackground);
        }
    }
}
