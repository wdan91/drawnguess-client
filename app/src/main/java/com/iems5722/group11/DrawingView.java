package com.iems5722.group11;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
/**
 * Created by WD-MAC on 16/4/15.
 */
public class DrawingView extends View
{
    private boolean isDrawer;


    //drawing path
    private Path drawPath;
    //drawing and canvas paint
    private Paint drawPaint, canvasPaint;
    //initial color
    private int paintColor = 0xFF660000;
    //canvas
    private Canvas drawCanvas;
    //canvas bitmap
    private Bitmap canvasBitmap;

    private float brushSize;

    private boolean erase;


    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setupDrawing();
    }


    private void setupDrawing(){
//get drawing area setup for interaction
        brushSize = getResources().getInteger(R.integer.init_size);
        drawPath = new Path();
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(brushSize);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    /*@Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isDrawer) {
            float touchX = event.getX();
            float touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    drawPath.moveTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    drawPath.lineTo(touchX, touchY);
                    break;
                case MotionEvent.ACTION_UP:
                    drawCanvas.drawPath(drawPath, drawPaint);
                    drawPath.reset();
                    break;
                default:
                    return false;
            }
            invalidate();
        }
        return true;
    }*/
    public void setColor(String newColor){
        invalidate();
        paintColor = Color.parseColor(newColor);
        //brushSetting.setPaintColor(Color.parseColor(newColor));
        drawPaint.setColor(paintColor);
//set color
    }

    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        // brushSetting.setBrushSize(pixelAmount);
        brushSize = pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
        // drawPaint.setStrokeWidth(brushSetting.getBrushSize());
    }


    public void setErase(boolean isErase){
        erase = isErase;
        // brushSetting.setErase(isErase);
        if(erase) drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        else drawPaint.setXfermode(null);
//set erase true or false
    }

    public int getViewWidth(){
        return this.getWidth();
    }
    public int getViewHeight(){
        return this.getHeight();
    }

    public void setDrawer(boolean drawer){
        this.isDrawer = drawer;
        return;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
//force a 4:3 aspect ratio
        int height = Math.round(width * 1f);
        setMeasuredDimension(width, height);
    }

    public boolean setPath(int action, float touchX, float touchY) {
        // if (isDrawer) {
        // float touchX = event.getX();
        //float touchY = event.getY();
        Log.d("DrawingView","brushsize: "+drawPaint.getStrokeWidth());
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPath.reset();
                break;
            default:
                return false;
        }
        invalidate();
        //  }
        return true;
    }

    public Paint getDrawPaint(){
        return drawPaint;
    }
    public boolean getErase(){
        return erase;
    }
}
