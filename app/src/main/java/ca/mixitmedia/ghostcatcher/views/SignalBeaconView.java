package ca.mixitmedia.ghostcatcher.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * Created by Nathalie on 2014-07-06.
 */
public class SignalBeaconView extends SurfaceView {

    Paint waveOne, waveTwo,waveThree;
    float graphX, graphY;
    WaveFunction waveFunction;

    public SignalBeaconView(Context context, AttributeSet attr){
        super(context, attr);

        waveOne = new Paint();
        waveTwo = new Paint();
        waveThree = new Paint();

    }

    @Override
    protected void onDraw(Canvas canvas){
        drawWavePoints(canvas);

    }

    public void drawWavePoints(Canvas canvas){
        canvas.drawColor(Color.BLACK);
        float[] waveOnePointsArray = new float[(this.getWidth()*2)];

        for(int i=0; i < this.getWidth(); i++){
            waveOnePointsArray[i*2]=i;
            waveOnePointsArray[(i*2)+1] = waveFunction.getGraphY(i);
        }

        canvas.drawPoints(waveOnePointsArray, waveOne);
    }

    public interface WaveFunction{
        public float getGraphY(float graphX);
    }

    public void setWaveFunction(WaveFunction waveFunction){
        this.waveFunction = waveFunction;
    }


}
