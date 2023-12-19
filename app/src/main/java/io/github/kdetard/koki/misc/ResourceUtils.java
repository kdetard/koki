package io.github.kdetard.koki.misc;

import android.content.res.Resources;

public class ResourceUtils {
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int toDp(int px) {
        return (int) (px * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    public static float toDp(float px) {
        return px * Resources.getSystem().getDisplayMetrics().density + 0.5f;
    }
}
