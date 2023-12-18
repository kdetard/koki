package io.github.kdetard.koki.misc;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.kdetard.koki.R;

public enum Direction {
    NORTH(R.string.north),
    NORTHEAST(R.string.northeast),
    EAST(R.string.east),
    SOUTHEAST(R.string.southeast),
    SOUTH(R.string.south),
    SOUTHWEST(R.string.southwest),
    WEST(R.string.west),
    NORTHWEST(R.string.northwest),
    ;

    private final int resId;

    Direction(final int resId) {
        this.resId = resId;
    }

    @NonNull
    public String getText(Context context) {
        return context.getString(resId);
    }
}
