package io.github.kdetard.koki.feature.onboard;

import static autodispose2.AutoDispose.autoDisposable;

import android.content.res.Configuration;
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

public class OnboardController extends BaseController implements OnConfigurationListener {
    private static final int LandscapeInsetType = WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.navigationBars();
    private static final int PortraitInsetType = WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime();
    private static final int OutOfBoundInsetType = 10; /* WindowInsetsCompat.Type.SIZE + 1 */

    ControllerOnboardBinding binding;

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

    private static void setPortraitInsets(View actionLayout) {
        final Drawable background = actionLayout.getBackground();

        actionLayout.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            final int statusBarInsets = Objects.requireNonNull(ViewCompat.getRootWindowInsets(v))
                    .getInsets(WindowInsetsCompat.Type.statusBars()).top;

            final boolean isBehindStatusBar = top <= statusBarInsets;
            final int topInsetType = isBehindStatusBar ? WindowInsetsCompat.Type.statusBars() : OutOfBoundInsetType;

            toggleBackgroundTint(v, background, isBehindStatusBar);

            Insetter.builder()
                    .paddingTop(topInsetType, true)
                    .paddingBottom(PortraitInsetType, true)
                    .applyToView(v);
        });
    }

    private static void toggleBackgroundTint(View view, Drawable originalBackground, boolean predicate) {
        if (predicate) {
            view.setBackgroundColor(Objects.requireNonNull(view.getBackgroundTintList()).getDefaultColor());
        } else {
            view.setBackgroundColor(0);
            view.setBackground(originalBackground);
        }
    }

    @Override
    public void onConfigurationChange(@NonNull Configuration newConfig) {
        final boolean isLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE;
        final int orientationInsetType = isLandscape ? LandscapeInsetType : OutOfBoundInsetType;

        Insetter.builder()
                .paddingLeft(orientationInsetType, false)
                .paddingRight(orientationInsetType, false)
                .applyToView(binding.getRoot());
    }
}
