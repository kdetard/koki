package io.github.kdetard.koki.ui;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.content.Intent;
import android.os.Bundle;

import com.jakewharton.rxbinding4.view.RxView;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.github.kdetard.koki.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.openAppAuthLoginBtn))
                .doOnNext(v -> {
                    final Intent appAuthLoginIntent = new Intent(this, AppAuthLoginActivity.class);
                    startActivity(appAuthLoginIntent);
                })
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.openRestLoginBtn))
                .doOnNext(v -> {
                    final Intent restLoginIntent = new Intent(this, RestLoginActivity.class);
                    startActivity(restLoginIntent);
                })
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.openSignupBtn))
                .doOnNext(v -> {
                    final Intent signupIntent = new Intent(this, RestSignupActivity.class);
                    startActivity(signupIntent);
                })
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))
                .subscribe();
    }
}