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
import ca.mixitmedia.ghostcatcher.Utils;

/**
 * Created by Dante on 2014-06-13.
 */

public class ToolLightButton extends View {

    public ToolFragment toolFragment;
    Bitmap glyph, lit, unlit, disabled;
    int height, width;
    Paint paint;

    public void setGlyph(Bitmap glyph) {
        this.glyph = glyph;
        invalidate();
        requestLayout();
    }

    public ToolLightButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,R.styleable.ToolLightButton,0, 0);
        try {
            Drawable _glyph = a.getDrawable(R.styleable.ToolLightButton_glyph);
            if (_glyph != null) glyph = Utils.drawableToBitmap(_glyph);
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
        if (toolFragment.isEnabled()) {
            if (toolFragment.isResumed()) {
                if(toolFragment.hasNotification() && ((System.currentTimeMillis() / 500) % 2 !=0))
                    drawCenteredBitmap(canvas, unlit);
                drawCenteredBitmap(canvas, lit);
            } else {
                if(toolFragment.hasNotification() && ((System.currentTimeMillis() / 500) % 2 !=0))
                    drawCenteredBitmap(canvas, lit);
                drawCenteredBitmap(canvas, unlit);
            }
            if (glyph != null)
                drawCenteredBitmap(canvas, glyph);
        } else {
            drawCenteredBitmap(canvas, disabled);
        }
    }

    private void drawCenteredBitmap(Canvas canvas, Bitmap bitmap) {
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), canvas.getClipBounds(), paint);
        //canvas.drawBitmap(bitmap, canvas.getWidth() - bitmap.getWidth()/2, canvas.getHeight() - bitmap.getHeight()/2, paint);
    }
}
