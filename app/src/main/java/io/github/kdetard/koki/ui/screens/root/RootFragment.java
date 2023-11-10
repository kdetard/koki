package io.github.kdetard.koki.ui.screens.root;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.mmkv.MMKV;

import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.ui.base.BaseFragment;
import io.github.kdetard.koki.ui.screens.main.MainFragmentDirections;

@AndroidEntryPoint
public class RootFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_root, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.rootFragment_actionLayout);

        if (navHostFragment == null) {
            return;
        }

        final NavController controller = navHostFragment.getNavController();

        if (MMKV.defaultMMKV().getString("accessToken", "").isEmpty()) {
            final NavDestination dest = navHostFragment.getNavController().getCurrentDestination();
            if (dest != null && dest.equals(controller.findDestination(R.id.mainFragment))) {
                controller.navigate(MainFragmentDirections.actionGlobalOnboardFragment());
            }
        }
    }
}
