package io.github.ktard.koki.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.github.ktard.koki.R;

public class EmptyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(RESULT_OK, getIntent());
        finish();
    }
}