package io.github.kdetard.koki.feature.settings;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.maxkeppeler.sheets.core.SheetStyle;
import com.maxkeppeler.sheets.input.InputSheet;
import com.maxkeppeler.sheets.input.type.InputRadioButtons;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.kdetard.koki.R;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.feature.locale.LocaleEvent;
import kotlin.Unit;
import timber.log.Timber;

public class SettingsLanguageController extends BaseController {
    public SettingsLanguageController() { super(R.layout.controller_settings_dialog); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        new InputSheet().show(Objects.requireNonNull(getActivity()), null, sheet -> {
            sheet.style(SheetStyle.DIALOG);
            sheet.setShowsDialog(true);
            sheet.title(R.string.select_app_language);
            sheet.with(new InputRadioButtons("en-US", i -> {
                i.options(Arrays.stream(LocaleEvent.values())
                        .map(l -> l.getStringResId(Objects.requireNonNull(getApplicationContext()))).collect(Collectors.toList()));
                String currentLocaleName;
                if (!AppCompatDelegate.getApplicationLocales().isEmpty()) {
                    // Fetches the current Application Locale from the list
                    var maybeLocale = AppCompatDelegate.getApplicationLocales().get(0);
                    if (maybeLocale != null)
                        currentLocaleName = maybeLocale.toLanguageTag();
                    else {
                        currentLocaleName = null;
                    }
                } else {
                    // Fetches the default System Locale
                    currentLocaleName = Locale.getDefault().toLanguageTag();
                }
                Timber.d("lang: %s", currentLocaleName);
                i.selected(Arrays.stream(LocaleEvent.values()).filter(e -> e.getLocaleResId(Objects.requireNonNull(getApplicationContext())).equals(currentLocaleName))
                        .map(LocaleEvent::ordinal).findFirst().orElse(LocaleEvent.SYSTEM.ordinal()));
                i.changeListener(this::onLanguageChanged);
                return null;
            }));
            sheet.onCancel(this::cancelLanguage);
            sheet.onPositive(R.string.cancel, this::cancelLanguage);
            sheet.onNegative("", () -> null);
            return null;
        });
    }

    private Unit onLanguageChanged(Integer integer) {
        Arrays.stream(LocaleEvent.values()).skip(integer).findFirst().ifPresent(localeEvent -> {
            var appLocale = LocaleListCompat.forLanguageTags(localeEvent.getLocaleResId(Objects.requireNonNull(getApplicationContext())));
            AppCompatDelegate.setApplicationLocales(appLocale);
        });
        return null;
    }

    private Unit cancelLanguage() {
        cancelLanguage(null);
        return null;
    }

    private Unit cancelLanguage(Bundle bundle) {
        getRouter().popController(this);
        return null;
    }
}
