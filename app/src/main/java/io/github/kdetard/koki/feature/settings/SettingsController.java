package io.github.kdetard.koki.feature.settings;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Objects;

import io.github.kdetard.koki.feature.base.BasePreferenceController;

public abstract class SettingsController extends BasePreferenceController {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        var screen = getPreferenceManager().createPreferenceScreen(getThemedContext());
        setPreferenceScreen(screen);
    }

    public String getTitle() {
        var title = getPreferenceScreen().getTitle();
        if (title == null) {
            return null;
        }
        return title.toString();
    }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);
        getListView().setLayoutManager(new LinearLayoutManager(view.getContext()));
    }

    private Context getThemedContext() {
        var tv = new TypedValue();
        Objects.requireNonNull(getActivity()).getTheme().resolveAttribute(androidx.preference.conductor.R.attr.preferenceTheme, tv, true);
        return new ContextThemeWrapper(getActivity(), tv.resourceId);
    }
}
