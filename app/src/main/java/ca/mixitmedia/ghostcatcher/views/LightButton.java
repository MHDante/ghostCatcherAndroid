package ca.mixitmedia.ghostcatcher.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import ca.mixitmedia.ghostcatcher.app.Tools.ToolFragment;

/**
 * Created by Dante on 2014-06-13.
 */

public class LightButton extends View {

    public enum State { lit, unlit, disabled, flashing }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    State state;
    static List<LightButton> lights = new ArrayList<>();
    Bitmap glyph, lit, unlit, disabled, flash;
    int height, width;
    Paint paint;

    public LightButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        lights.add(this);
        paint = new Paint();
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LightButton, 0, 0);
        try {
            Drawable _glyph = a.getDrawable(R.styleable.LightButton_glyph);
            if (_glyph != null) glyph = Utils.drawableToBitmap(_glyph);
            lit = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_lit));
            unlit = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_unlit));
            disabled = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_disabled));
            flash = Utils.drawableToBitmap(context.getResources().getDrawable(R.drawable.button_alarm));
        } finally {
            a.recycle();
        }
    }

    public static void RefreshAll(){
        for(LightButton l : lights){
            l.invalidate();
            l.refreshDrawableState();
        }
    }

    public void setGlyph(Bitmap glyph) {
        this.glyph = glyph;
        invalidate();
        requestLayout();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.height = h;
        this.width = w;
        ToolFragment t;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (state) {
            case disabled:
                drawCenteredBitmap(canvas, lit);
                break;
            case lit:
                drawCenteredBitmap(canvas, lit);
                break;
            case unlit:
                drawCenteredBitmap(canvas, lit);
                break;
            case flashing:
                drawCenteredBitmap(canvas, lit);
                break;
        }


     //      if (Owner.isSelected()) {
     //          if (Owner.hasNotification() && ((System.currentTimeMillis() / 500) % 2 != 0))
     //              drawCenteredBitmap(canvas, unlit);
     //
     //      } else {
     //          if (Owner.hasNotification() && ((System.currentTimeMillis() / 500) % 2 != 0))
     //              drawCenteredBitmap(canvas, lit);
     //          drawCenteredBitmap(canvas, unlit);
     //      }
     //      if (glyph != null)
     //          drawCenteredBitmap(canvas, glyph);
     //  } else {
     //      drawCenteredBitmap(canvas, disabled);
     //  }
    }

    private void drawCenteredBitmap(Canvas canvas, Bitmap bitmap) {
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), canvas.getClipBounds(), paint);
 }
}
