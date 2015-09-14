package me.suszczewicz.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import me.suszczewicz.compass.R;

public class CompassView extends View {

    private final static float MAX_ANGLE_CHANGE_STEP = 2.0f;
    private final static float DEFAULT_ANGLE_CHANGE_STEP_FACTOR = 0.05f;
    private final static float MIN_ANGLE_DIFF_TO_REDRAW = 1.5F;
    private final static float GUIDE_ARROW_DISTANCE_FACTOR = 0.35f;

    private Bitmap discBitmap;
    private Bitmap needleBitmap;
    private Bitmap guideArrowBitmap;

    private int minWidthHeight;
    private boolean guideArrowEnabled;

    private float needleAngle;
    private float needleAngleTarget;

    private float guideArrowAngle;
    private float guideArrowAngleTarget;

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);

        loadBitmaps();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        minWidthHeight = Math.min(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isInEditMode())
            return;

        calculateAngle();

        drawDisc(canvas);
        drawNeedle(canvas);
        drawGuideArrow(canvas);

        requestRedrawIfNeeded();
    }

    public void setNeedleAngle(Float angle) {
        this.needleAngleTarget = angle;

        requestRedrawIfNeeded();
    }

    public void setGuideArrowAngle(float angle) {
        this.guideArrowAngleTarget = angle;

        requestRedrawIfNeeded();
    }

    public void setGuideArrowEnabled(boolean arrowEnabled) {
        if (guideArrowEnabled != arrowEnabled) {
            guideArrowEnabled = arrowEnabled;

            invalidate();
        }
    }

    private void drawNeedle(Canvas canvas) {
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.rotate(needleAngle, minWidthHeight / 2, minWidthHeight / 2);
        canvas.translate(minWidthHeight / 2 - needleBitmap.getWidth() / 2, minWidthHeight / 2 - needleBitmap.getHeight() / 2);
        canvas.drawBitmap(needleBitmap, 0, 0, null);
        canvas.restore();
    }

    private void drawDisc(Canvas canvas) {
        canvas.drawBitmap(discBitmap, 0, 0, null);
    }

    private void calculateAngle() {
        needleAngle += calculateStepWithAnimation(needleAngle, needleAngleTarget);
        guideArrowAngle += calculateStepWithAnimation(guideArrowAngle, needleAngle + guideArrowAngleTarget);
    }

    private void drawGuideArrow(Canvas canvas) {
        if (guideArrowEnabled) {
            float x = (float) (Math.cos(guideArrowAngle * Math.PI / 180 - Math.PI / 2) * minWidthHeight * GUIDE_ARROW_DISTANCE_FACTOR + minWidthHeight / 2);
            float y = (float) (Math.sin(guideArrowAngle * Math.PI / 180 - Math.PI / 2) * minWidthHeight * GUIDE_ARROW_DISTANCE_FACTOR + minWidthHeight / 2);

            canvas.save(Canvas.MATRIX_SAVE_FLAG);
            canvas.translate(x, y);
            canvas.rotate(guideArrowAngle, 0, 0);
            canvas.drawBitmap(guideArrowBitmap, -1 * guideArrowBitmap.getWidth() / 2, -1 * guideArrowBitmap.getHeight() / 2, null);
            canvas.restore();
        }
    }

    private void requestRedrawIfNeeded() {
        if (needToBeRedraw())
            invalidate();
    }

    private boolean needToBeRedraw() {
        boolean needleRedraw = Math.abs(needleAngle - needleAngleTarget) > MIN_ANGLE_DIFF_TO_REDRAW;
        boolean arrowRedraw = Math.abs(guideArrowAngle - guideArrowAngleTarget) > MIN_ANGLE_DIFF_TO_REDRAW && guideArrowEnabled;

        return needleRedraw || arrowRedraw;
    }

    private float calculateStepWithAnimation(float current, float target) {
        float clockwiseDistance = (target - current + 360) % 360;
        float minDistance = Math.min(clockwiseDistance, 360 - clockwiseDistance);

        float step = Math.min(MAX_ANGLE_CHANGE_STEP, minDistance * DEFAULT_ANGLE_CHANGE_STEP_FACTOR);

        return clockwiseDistance < 180 ? step : -1 * step;
    }

    private void loadBitmaps() {
        Resources res = getResources();

        discBitmap = BitmapFactory.decodeResource(res, R.drawable.disc);
        needleBitmap = BitmapFactory.decodeResource(res, R.drawable.needle);
        guideArrowBitmap = BitmapFactory.decodeResource(res, R.drawable.guide_arrow);
    }

}
