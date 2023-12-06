package io.github.kdetard.koki.feature.base;

import android.content.Intent;

public class RxActivity {
    public static class ActivityResult {
        public final int requestCode;
        public final int resultCode;
        public final Intent data;

        public ActivityResult(final int requestCode, final int resultCode, final Intent data) {
            this.requestCode = requestCode;
            this.resultCode = resultCode;
            this.data = data;
        }
    }
}