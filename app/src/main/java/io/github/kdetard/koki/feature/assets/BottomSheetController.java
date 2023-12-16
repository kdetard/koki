package io.github.kdetard.koki.feature.assets;

import static autodispose2.AutoDispose.autoDisposable;

import android.util.TypedValue;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.jakewharton.rxbinding4.view.RxView;

import java.util.Objects;

import io.github.kdetard.koki.feature.base.BaseController;

public abstract class BottomSheetController extends BaseController {

    public BottomSheetController(int layoutRes) {
        super(layoutRes);
    }
    
    private BottomSheetBehavior<View> behavior;
    boolean isExpanding = false;

    public void onViewCreated(View view, View bottomSheet) {
        super.onViewCreated(view);

        behavior = BottomSheetBehavior.from(bottomSheet);
        RxView.clicks(bottomSheet)
                .doOnNext(u -> {
                    if (behavior.getState() != BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                    } else {
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                })
                .to(autoDisposable(getScopeProvider()))
                .subscribe();

        /*behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });*/

        /*behavior.setFitToContents(false);
        behavior.setHalfExpandedRatio(0.5f);*/

        /*behavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {}

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //Define a new peekHeight when user drag above middle of screen
                if (behavior.getState() == BottomSheetBehavior.STATE_DRAGGING) {
                    if(slideOffset >= 0 && slideOffset <= 0.5f){
                        setPeekHeightBottom();
                    }
                    else{
                        setPeekHeightMiddleScreen();
                    }
                }

                //Settling
                if (behavior.getState() == BottomSheetBehavior.STATE_SETTLING) {
                    //Bottom of bottom of screen > Force to collapse
                    if(slideOffset >= 0 && slideOffset <= 0.25f) {
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    //Top of bottom of screen > Force to expand
                    else if(slideOffset > 0.25f && slideOffset <= 0.5f) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                    //Bottom of top of screen > Force to co
                    else if(slideOffset > 0.5f && slideOffset <= 0.75f) {
                        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                    else if(slideOffset > 0.75f) {
                        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                }
            }
        });*/

        // behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        getNavBar().setOnItemReselectedListener(item -> {
            if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                behavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            } else if (behavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            } else {
                behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });
    }

    public BottomSheetBehavior<View> getBehavior() { return behavior; }

    private void setPeekHeightBottom() {
        behavior.setPeekHeight((int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                32f,
                Objects.requireNonNull(getResources()).getDisplayMetrics()
        ));
    }

    private void setPeekHeightMiddleScreen() {
        var screenHeight = Objects.requireNonNull(getResources()).getDisplayMetrics().heightPixels;
        behavior.setPeekHeight(screenHeight / 2);
    }
}
