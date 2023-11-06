package io.github.kdetard.koki.ui.screens.root;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tencent.mmkv.MMKV;

import dagger.hilt.android.AndroidEntryPoint;
import io.github.kdetard.koki.R;

@AndroidEntryPoint
public class RootFragment extends Fragment {
    public RootFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_root, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final NavHostFragment navHostFragment = (NavHostFragment) getChildFragmentManager()
                .findFragmentById(R.id.rootFragment_actionLayout);

        if (MMKV.defaultMMKV().getString("accessToken", "").isEmpty() && navHostFragment != null) {
            final NavDestination dest = navHostFragment.getNavController().getCurrentDestination();
            final NavController controller = navHostFragment.getNavController();
            if (dest != null && !dest.equals(controller.findDestination(R.id.action_mainFragment_to_onboard_graph))) {
                navHostFragment.getNavController().navigate(R.id.action_mainFragment_to_onboard_graph);
            }
        }
    }
}
