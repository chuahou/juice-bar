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

    /** Single sub-bar, representing one level. */
    private class SubBar {
        private ShapeDrawable rect;
        private float maxPercentage; // % of battery corresponding to this sub-bar.
        private static final int height = 8; // Height of sub-bar.
        public SubBar(float maxPercentage, int color) {
            rect = new ShapeDrawable(new RectShape());
            rect.getPaint().setColor(color);
            this.maxPercentage = maxPercentage;
        }
        public void draw(Canvas canvas) {
            float percentage = Math.min(battLevel, maxPercentage);
            Log.i(TAG, "Drawing sub-bar with max at " + maxPercentage + "% at " + percentage + "%");
            rect.setBounds(0, 0, (int) (canvas.getWidth() * percentage), height);
            rect.draw(canvas);
        }
    }

    // Drawn from first element to last. The later in the array, the higher the sub-bar.
    private final SubBar subBars[] = {
        new SubBar(1.0000f, 0xFF0099CC), // 80%
        new SubBar(0.8125f, 0xFFC1E859), // 65%
        new SubBar(0.4375f, 0xFFF0A30A), // 35%
        new SubBar(0.1875f, 0xFFE74C3C), // 15%
        new SubBar(0.0625f, 0xFFFF2D3B), //  5%
    };

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
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "Redrawing");
        super.onDraw(canvas);
        for (SubBar subBar : subBars)
            subBar.draw(canvas);
    }
}
