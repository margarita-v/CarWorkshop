package com.dsr_practice.car_workshop.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;

import com.dsr_practice.car_workshop.R;

class IconsUtils {

    static Drawable getIcon(Context context, int drawableId, int colorId) {
        Drawable result = ContextCompat.getDrawable(context, drawableId);
        DrawableCompat.setTint(result, ContextCompat.getColor(context, colorId));
        return result;
    }

    static int getResource(Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, typedValue, true);
        return typedValue.resourceId;
    }
}
