package io.github.kdetard.koki.feature.settings;

import static autodispose2.AutoDispose.autoDisposable;

import android.view.View;

import androidx.datastore.rxjava3.RxDataStore;

import com.maxkeppeler.sheets.core.SheetStyle;
import com.maxkeppeler.sheets.info.InfoSheet;
import com.squareup.moshi.JsonAdapter;
import com.tencent.mmkv.MMKV;

import java.util.Objects;

import dagger.hilt.EntryPoint;
import dagger.hilt.InstallIn;
import dagger.hilt.android.EntryPointAccessors;
import dagger.hilt.components.SingletonComponent;
import io.github.kdetard.koki.R;
import io.github.kdetard.koki.Settings;
import io.github.kdetard.koki.di.NetworkModule;
import io.github.kdetard.koki.feature.base.BaseController;
import io.github.kdetard.koki.keycloak.KeycloakApiService;
import io.github.kdetard.koki.keycloak.RxRestKeycloak;
import io.github.kdetard.koki.keycloak.models.KeycloakConfig;
import io.github.kdetard.koki.keycloak.models.KeycloakToken;
import io.reactivex.rxjava3.core.Single;
import kotlin.Unit;

public class SettingsLogoutController extends BaseController {
    @EntryPoint
    @InstallIn(SingletonComponent.class)
    interface SettingsLogoutEntryPoint {
        RxDataStore<Settings> settings();
        KeycloakApiService keycloakApiService();
        JsonAdapter<KeycloakToken> keycloakTokenJsonAdapter();
    }

    SettingsLogoutEntryPoint entryPoint;
    KeycloakConfig mKeycloakConfig;

    public SettingsLogoutController() { super(R.layout.controller_settings_dialog); }

    @Override
    public void onViewCreated(View view) {
        super.onViewCreated(view);

        entryPoint = EntryPointAccessors.fromApplication(Objects.requireNonNull(getApplicationContext()), SettingsLogoutEntryPoint.class);

        mKeycloakConfig = KeycloakConfig.getDefaultConfig(getApplicationContext());

        new InfoSheet().show(Objects.requireNonNull(getActivity()), null, sheet -> {
            sheet.style(SheetStyle.DIALOG);
            sheet.setShowsDialog(true);
            sheet.title(R.string.logout);
            sheet.content(R.string.confirm_logout);
            sheet.onCancel(this::cancelLogout);
            sheet.onNegative(R.string.decline_logout, this::cancelLogout);
            sheet.onPositive(R.string.accept_logout, this::confirmLogout);
            return null;
        });
    }

    private Unit cancelLogout() {
        getRouter().popController(this);
        return null;
    }

    private Unit confirmLogout() {
        entryPoint.settings()
                .data()
                .firstOrError()
                .map(Settings::getKeycloakTokenJson)
                .map(entryPoint.keycloakTokenJsonAdapter()::fromJson)
                .map(KeycloakToken::refreshToken)
                .flatMapCompletable(refreshToken ->
                        RxRestKeycloak.endSession(entryPoint.keycloakApiService(), mKeycloakConfig, refreshToken))
                .andThen(entryPoint.settings().updateDataAsync(settings ->
                        Single.just(settings.toBuilder().clearKeycloakTokenJson().build())))
                .doFinally(() -> MMKV.mmkvWithID(NetworkModule.COOKIE_STORE_NAME).clearAll())
                .to(autoDisposable(getScopeProvider()))
                .subscribe();
        return null;
    }
}
