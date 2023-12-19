package io.github.kdetard.koki.feature.settings;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.preference.Preference;

import com.bluelinelabs.conductor.Controller;
import com.bluelinelabs.conductor.RouterTransaction;
import com.bluelinelabs.conductor.changehandler.FadeChangeHandler;

import java.util.Objects;

import io.github.kdetard.koki.R;

public class SettingsMainController extends SettingsController {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
        getPreferenceManager().setOnPreferenceTreeClickListener(this::handlePreference);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getToolbar().setTitle(R.string.settings);
    }

    public boolean handlePreference(Preference pref) {
        var fragment = pref.getFragment();
        Controller controller = null;

        if (Objects.equals(fragment, SettingsAccountController.class.getName())) {
            controller = new SettingsAccountController();
        } else if (Objects.equals(fragment, SettingsThemeController.class.getName())) {
            controller = new SettingsThemeController();
        } else if (Objects.equals(fragment, SettingsLanguageController.class.getName())) {
            controller = new SettingsLanguageController();
        } else if (Objects.equals(fragment, SettingsLogoutController.class.getName())) {
            controller = new SettingsLogoutController();
        } else if (Objects.equals(fragment, SettingsAboutController.class.getName())) {
            controller = new SettingsAboutController();
        }

        if (controller != null) {
            getRouter().pushController(
                    RouterTransaction.with(controller)
                            .popChangeHandler(new FadeChangeHandler())
                            .pushChangeHandler(new FadeChangeHandler(!(controller instanceof SettingsLogoutController || controller instanceof SettingsLanguageController)))
            );
        }

        return true;
    }
}
