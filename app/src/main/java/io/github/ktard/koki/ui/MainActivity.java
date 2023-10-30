package io.github.ktard.koki.ui;

import static autodispose2.AutoDispose.autoDisposable;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.jakewharton.rxbinding4.view.RxView;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.github.ktard.koki.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.openCustomTabLoginBtn))

                .doOnNext(v -> {
                    var customTabLoginIntent = new Intent(this, CustomTabLoginActivity.class);
                    startActivity(customTabLoginIntent);
                })

                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))

                .subscribe();

        RxView
                //Capture Login Button Click:
                .clicks(findViewById(R.id.openDefaultLoginBtn))

                .doOnNext(v -> {
                    var defaultLoginIntent = new Intent(this, LoginActivity.class);
                    startActivity(defaultLoginIntent);
                })

                .to(autoDisposable(AndroidLifecycleScopeProvider.from(this)))

                .subscribe();
    }
}