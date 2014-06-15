package ca.mixitmedia.ghostcatcher.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import ca.mixitmedia.ghostcatcher.app.R;
import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;
import ca.mixitmedia.ghostcatcher.utils.Utils;

/**
 * Created by Dante on 2014-06-13.
 */
public class ToolLightButton extends View {

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        invalidate();
        requestLayout();
    }

    boolean selected;

    public boolean isEnabled() {
        return enabled;

    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        invalidate();
        requestLayout();
    }

    public Bitmap getSrc() {
        return src;
    }

    public void setSrc(Bitmap src) {
        this.src = src;

        invalidate();
        requestLayout();
    }

    public ToolFragment getToolFragment() {
        return toolFragment;
    }

    public void setToolFragment(ToolFragment toolFragment) {
        this.toolFragment = toolFragment;
    }

    ToolFragment toolFragment;
    boolean enabled;
    Bitmap src, lit, unlit, disabled;
    int height, width;
    Paint paint;


    public ToolLightButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ToolLightButton,
                0, 0);

        try {
            selected = a.getBoolean(R.styleable.ToolLightButton_selected, false);
            enabled = a.getBoolean(R.styleable.ToolLightButton_enabled, false);
            Drawable glyph = a.getDrawable(R.styleable.ToolLightButton_glyph);
            if (glyph != null) src = Utils.drawableToBitmap(glyph);
            lit = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_lit));
            unlit = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_unlit));
            disabled = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_disabled));

        } finally {
            a.recycle();
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.height = h;
        this.width = w;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (enabled) {
            if (selected) {
                drawCenteredBitmap(canvas, lit);
            } else {
                drawCenteredBitmap(canvas, unlit);
            }
            if (src != null)
                drawCenteredBitmap(canvas, src);
        } else {
            drawCenteredBitmap(canvas, disabled);
        }


    }

    private void drawCenteredBitmap(Canvas canvas, Bitmap bitmap) {
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), canvas.getClipBounds(), paint);
        //canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth()/2, canvas.getHeight() - bitmap.getHeight()/2, paint);
    }
}
