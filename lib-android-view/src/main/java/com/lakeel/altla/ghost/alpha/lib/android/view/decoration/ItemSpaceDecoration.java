package com.lakeel.altla.ghost.alpha.lib.android.view.decoration;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.lakeel.altla.ghost.alpha.lib.android.view.R;


public class ItemSpaceDecoration extends RecyclerView.ItemDecoration {

    public enum Orientation {
        VERTICAL, HORIZONTAL
    }

    private int space;

    @NonNull
    private final Orientation orientation;

    public static ItemSpaceDecoration createDefaultDecoration(@NonNull Context context, @NonNull Orientation orientation) {
        int spacingInPixels = context.getResources().getDimensionPixelSize(R.dimen.recycler_view_margin);
        return new ItemSpaceDecoration(spacingInPixels, orientation);
    }

    public ItemSpaceDecoration(@IntRange(from = 0) int space, @NonNull Orientation orientation) {
        this.space = space;
        this.orientation = orientation;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        switch (orientation) {
            case VERTICAL:
                outRect.top = space;
                outRect.bottom = space;
                break;
            case HORIZONTAL:
                outRect.left = space;
                outRect.right = space;
                break;
        }
    }
}
