package io.github.kdetard.koki.utils;

import androidx.core.view.WindowInsetsCompat;

public class InsetUtils {
    public static final int LandscapeInsetType = WindowInsetsCompat.Type.displayCutout() | WindowInsetsCompat.Type.navigationBars();
    public static final int PortraitInsetType = WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime();
    public static final int OutOfBoundInsetType = 10; /* WindowInsetsCompat.Type.SIZE + 1 */
}
