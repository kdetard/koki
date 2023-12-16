package io.github.kdetard.koki.misc;

import android.content.res.Resources;

public class ResourceUtils {
    public static int toDp(int px) {
        return (int) (px * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    public static float toDp(float px) {
        return (float) (px * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }
}
