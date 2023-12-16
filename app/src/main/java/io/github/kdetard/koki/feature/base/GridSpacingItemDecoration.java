package io.github.kdetard.koki.feature.base;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

    private final int columnCount;
    private final boolean includeEdge;
    private final int space;

    public GridSpacingItemDecoration(int columnCount, int preferredSpace, boolean includeEdge) {
        this.columnCount = columnCount;
        this.includeEdge = includeEdge;
        this.space = preferredSpace % 3 == 0 ? preferredSpace : preferredSpace + (3 - preferredSpace % 3);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        var position = parent.getChildAdapterPosition(view);

        if (includeEdge) {
            if (position % columnCount == 0) {
                outRect.left = space;
                outRect.right = space / 3;
            } else if (position % columnCount == columnCount - 1) {
                outRect.right = space;
                outRect.left = space / 3;
            } else {
                outRect.left = space * 2 / 3;
                outRect.right = space * 2 / 3;
            }

            if (position < columnCount) {
                outRect.top = space;
            }

            outRect.bottom = space;

        } else {
            if (position % columnCount == 0) {
                outRect.right = space * 2 / 3;
            } else if (position % columnCount == columnCount - 1) {
                outRect.left = space * 2 / 3;
            } else {
                outRect.left = space / 3;
                outRect.right = space / 3;
            }

            if (position >= columnCount) {
                outRect.top = space;
            }
        }
    }
}
