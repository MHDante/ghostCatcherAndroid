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

import java.util.ArrayList;
import java.util.List;

import ca.mixitmedia.ghostcatcher.Utils;
import ca.mixitmedia.ghostcatcher.app.R;

/**
 * Created by Dante on 2014-06-13
 */

public class LightButton extends View {

    public void setGlyphID(int glyphID) {
       setGlyph(Utils.drawableToBitmap(context.getResources().getDrawable(glyphID)));
    }

    private int mAspectRatioWidth;
    private int mAspectRatioHeight;

    public enum State { lit, unlit, disabled, flashing }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        this.invalidate();
        this.refreshDrawableState();
    }

    State state = State.disabled;
    static List<LightButton> lights = new ArrayList<>();
    Bitmap glyph, lit, unlit, disabled, flash;
    int height, width;
    Paint paint, glyphPaint;
    Context context;
    Rect drawRect,glyphRect = new Rect(0,0,0,0);

    public LightButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        lights.add(this);
        paint = new Paint();
        glyphPaint = new Paint();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LightButton, 0, 0);
        try {
            Drawable _glyph = a.getDrawable(R.styleable.LightButton_glyph);
            if (_glyph != null) glyph = Utils.drawableToBitmap(_glyph);
            lit = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_lit));
            unlit = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_unlit));
            disabled = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_disabled));
            flash = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_alarm));
            drawRect = new Rect(0, 0, disabled.getWidth(), disabled.getHeight());
            mAspectRatioWidth = a.getInt(R.styleable.LightButton_aspectRatioWidth, 1);
            mAspectRatioHeight = a.getInt(R.styleable.LightButton_aspectRatioHeight, 1);
        } finally {
            a.recycle();
        }


    }

    public void setGlyph(Bitmap glyph) {
        this.glyph = glyph;
        glyphRect = new Rect(0, 0, glyph.getWidth(), glyph.getHeight());
        invalidate();
        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.height = h;
        this.width = w;
    }

    Rect clipBounds = new Rect();
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(clipBounds);

        switch (state) {
            case disabled:
                canvas.drawBitmap(disabled, drawRect, clipBounds, paint);
                break;

            case lit:
                canvas.drawBitmap(lit, drawRect, clipBounds, paint);
                if (glyph!=null)canvas.drawBitmap(glyph, glyphRect, clipBounds, glyphPaint);
                break;

            case unlit:
                canvas.drawBitmap(unlit, drawRect, clipBounds, paint);
                if (glyph!=null)canvas.drawBitmap(glyph, glyphRect, clipBounds, glyphPaint);
                break;

            case flashing:
                if((System.currentTimeMillis()/500)%2 ==0) {
                    canvas.drawBitmap(unlit, drawRect, clipBounds, paint);
                    if (glyph != null) canvas.drawBitmap(glyph, glyphRect, clipBounds, glyphPaint);
                }
                else {
                    canvas.drawBitmap(lit, drawRect, clipBounds, paint);
                    if (glyph != null ) canvas.drawBitmap(glyph, glyphRect, clipBounds, glyphPaint);
                }
                postInvalidateDelayed(500);
                break;
        }
    }

    @Override
    protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec)  {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int originalHeight = MeasureSpec.getSize(heightMeasureSpec);

        int calculatedHeight = originalWidth * mAspectRatioHeight / mAspectRatioWidth;

        int finalWidth, finalHeight;

        if (calculatedHeight > originalHeight)  {
            finalWidth = originalHeight * mAspectRatioWidth / mAspectRatioHeight;
            finalHeight = originalHeight;
        }
        else {
            finalWidth = originalWidth;
            finalHeight = calculatedHeight;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }



    private void drawCenteredBitmap(Canvas canvas, Bitmap bitmap) {
        if (bitmap != null) {
	        Rect r = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	        canvas.drawBitmap(bitmap, r, canvas.getClipBounds(), paint);
        }
	}
}
