package io.github.kdetard.koki.feature.settings;

import static autodispose2.AutoDispose.autoDisposable;

import android.os.Bundle;
import android.view.View;

import androidx.datastore.rxjava3.RxDataStore;

import com.maxkeppeler.sheets.core.SheetStyle;
import com.maxkeppeler.sheets.input.InputSheet;
import com.maxkeppeler.sheets.input.type.InputRadioButtons;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.feature.theme.ThemeEvent;
import io.reactivex.rxjava3.core.Single;
import kotlin.Unit;

public class SettingsThemeController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface SettingsThemeEntryPoint {
        RxDataStore<Settings> settings();
    }

    SettingsThemeEntryPoint entryPoint;

    public SettingsThemeController() { super(R.layout.controller_settings_dialog); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), SettingsThemeEntryPoint.class);

        new InputSheet().show(Objects.requireNonNull(getActivity()), null, sheet -> {
            sheet.style(SheetStyle.DIALOG);
            sheet.setShowsDialog(true);
            sheet.title(R.string.select_app_language);
            sheet.with(new InputRadioButtons("en-US", i -> {
                i.options(Arrays.stream(ThemeEvent.values())
                        .map(e -> e.getText(getApplicationContext())).collect(Collectors.toList()));
                entryPoint.settings().data()
                        .map(Settings::getTheme)
                        .map(t -> Arrays.stream(ThemeEvent.values()).filter(e -> e.getTheme() == t)
                                .map(ThemeEvent::ordinal).findFirst().orElse(ThemeEvent.AUTO.ordinal()))
                        .doOnNext(i::selected)
                        .to(autoDisposable(getScopeProvider()))
                        .subscribe();
                i.changeListener(this::onThemeChanged);
                return null;
            }));
            sheet.onCancel(this::cancelLanguage);
            sheet.onPositive(R.string.cancel, this::cancelLanguage);
            sheet.onNegative("", () -> null);
            return null;
        });
    }

    private Unit onThemeChanged(Integer integer) {
        Arrays.stream(ThemeEvent.values()).skip(integer).findFirst().ifPresent(themeEvent ->
                entryPoint.settings().updateDataAsync(settings ->
                        Single.just(settings.toBuilder().setTheme(themeEvent.getTheme()).build())));
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
