package dev.chuahou.juicebar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.Log;
import android.view.View;

/** View to be drawn by BarService's thread. */
public class BarView extends View {
    private float battLevel = 0.0f;

    private ShapeDrawable drawable;

    /** Tag for logging. */
    private static final String TAG = "juicebar.BarView";

    public void setBattLevel(float battLevel) {
        Log.i(TAG, "Setting battery level to " + battLevel);

        // Only update and redraw if battery level has changed.
        if (this.battLevel != battLevel) {
            this.battLevel = battLevel;
            postInvalidate();
        }
    }

    public BarView(Context context) {
        super(context);
        drawable = new ShapeDrawable(new RectShape());
        drawable.getPaint().setColor(0xff00ff00);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "Redrawing");
        super.onDraw(canvas);
        int width = (int) (canvas.getWidth() * battLevel);
        int height = 8;
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
    }
}
