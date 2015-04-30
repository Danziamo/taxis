package taxi.city.citytaxidriver;

/**
 * Created by Daniyar on 4/30/2015.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class CustomButton extends Button {

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.helveticFont);
        String fontFamily = null;
        final int n = a.getIndexCount();
        for (int i = 0; i < n; ++i) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.helveticFont_android_fontFamily) {
                fontFamily = a.getString(attr);
            }
            a.recycle();
        }
        if (!isInEditMode()) {
            try {
                Typeface tf = Typeface.createFromAsset(
                        getContext().getAssets(), fontFamily);
                setTypeface(tf);
            } catch (Exception e) {
            }
        }
    }
}