package io.github.kdetard.koki.view;

import android.content.Context;
import android.util.DisplayMetrics;

public class ViewUtils {
    public static int toDp(Context context, int px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (px * (((float)metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int toPx(Context context, int dp) {
        var metrics = context.getResources().getDisplayMetrics();
        return (int) (dp / (((float)metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static String setColorAlpha(int percentage) {
        var decValue = ((double)percentage) / 100 * 255;
        var rawHexColor = "#000000".replace("#", "");
        var str = new StringBuilder(rawHexColor);
        if (Integer.toHexString((int)decValue).length() == 1)
            str.insert(
                0,
                "#0" + Integer.toHexString((int) decValue)
            );
        else
            str.insert(0, "#" + Integer.toHexString((int)decValue));
        return str.toString();
    }
}
