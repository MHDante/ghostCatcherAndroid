package ca.mixitmedia.ghostcatcher.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Nathalie on 2014-07-06
 */
public class SignalBeaconView extends SurfaceView { //TODO: this needs a lot of polish.
    Paint waveOne, waveTwo, waveThree;

    WaveFunction waveFunction;
    SurfaceThread surfaceThread;
    int width, height;

    public SignalBeaconView(Context context, AttributeSet attr) {
        super(context, attr);

        waveOne = new Paint();
        waveOne.setColor(Color.GREEN);
        waveOne.setStrokeWidth(7);

        waveTwo = new Paint();
        waveTwo.setColor(Color.MAGENTA);
        waveTwo.setStrokeWidth(7);

        waveThree = new Paint();
        waveThree.setColor(Color.YELLOW);
        waveThree.setStrokeWidth(7);



        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceThread = new SurfaceThread(getHolder(), SignalBeaconView.this);
                surfaceThread.setEnable(true);
                surfaceThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Boolean hasJoined = false;
                surfaceThread.setEnable(false);

                while (!hasJoined) {
                    try {
                        surfaceThread.join();
                        hasJoined = true;
                    } catch (java.lang.InterruptedException e) {
                    }
                }
            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawWavePoints(canvas);

    }

    public void drawWavePoints(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        float[] waveOnePointsArray = new float[width * 2];
        float[] waveTwoPointsArray = new float[width * 2];
        float[] waveThreePointsArray = new float[width * 2];

        for (int i = 0; i < this.getWidth(); i++) {
            waveOnePointsArray[i * 2] = i;
            waveTwoPointsArray[i * 2] = i;
            waveThreePointsArray[i * 2] = i;

            if (waveFunction == null) {
                waveOnePointsArray[(i * 2) + 1] = 1;
                waveTwoPointsArray[(i * 2) + 1] = 1;
                waveThreePointsArray[(i * 2) + 1] = 1;
            } else {
                waveOnePointsArray[(i * 2) + 1] = waveFunction.getGraphYWaveOne(i, ((float) height / 2));
                waveTwoPointsArray[(i * 2) + 1] = waveFunction.getGraphYWaveTwo(i, ((float) height / 2));
                waveThreePointsArray[(i * 2) + 1] = waveFunction.getGraphYWaveThree(i, ((float) height / 2));
            }
        }

        canvas.drawPoints(waveOnePointsArray, waveOne);
        canvas.drawPoints(waveTwoPointsArray, waveTwo);
        canvas.drawPoints(waveThreePointsArray, waveThree);
    }

    public void setWaveFunction(WaveFunction waveFunction) {
        this.waveFunction = waveFunction;
    }

    public interface WaveFunction {
        public float getGraphYWaveOne(float graphX, float amplitude);

        public float getGraphYWaveTwo(float graphX, float amplitude);

        public float getGraphYWaveThree(float graphX, float amplitude);
    }

}

class SurfaceThread extends Thread {
    SurfaceHolder surfaceHolder;
    SignalBeaconView beaconView;
    Boolean enable = false;


    public SurfaceThread(SurfaceHolder holder, SignalBeaconView bView) {
        surfaceHolder = holder;
        beaconView = bView;

    }

    public void setEnable(Boolean enable) {
        this.enable = enable;
    }

    @Override
    public void run() {
        Canvas canvas = null;

        while (enable) {
            try {
                canvas = surfaceHolder.lockCanvas();
                if (canvas != null) {
                    synchronized (surfaceHolder) {
                        beaconView.drawWavePoints(canvas);
                    }
                }

            } finally {
                if (canvas != null)
                    surfaceHolder.unlockCanvasAndPost(canvas);
            }

        }
    }
}

