package com.pioneer.desktoppetorhumanmobile;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

public class DesktopPetView extends View {
    interface CompanionListener {
        void onTouched();

        void onDragged();
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();
    private CompanionListener listener;
    private boolean humanMode;
    private float centerX;
    private float centerY;
    private float dragOffsetX;
    private float dragOffsetY;
    private boolean dragging;
    private int happiness = 68;
    private int energy = 72;
    private int fullness = 58;
    private long startTime = System.currentTimeMillis();
    private String bubble = "Tiny friend online.";
    private long bubbleUntil = System.currentTimeMillis() + 2600;

    public DesktopPetView(Context context) {
        super(context);
        setFocusable(true);
    }

    public void setCompanionListener(CompanionListener listener) {
        this.listener = listener;
    }

    public void setHumanMode(boolean humanMode) {
        this.humanMode = humanMode;
        invalidate();
    }

    public void setStats(int happiness, int energy, int fullness) {
        this.happiness = happiness;
        this.energy = energy;
        this.fullness = fullness;
        invalidate();
    }

    public void say(String text) {
        bubble = text;
        bubbleUntil = System.currentTimeMillis() + 2600;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        centerX = width * 0.5f;
        centerY = height * 0.6f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = System.currentTimeMillis();
        float t = (now - startTime) / 1000f;
        drawRoom(canvas, t);
        drawCompanion(canvas, t);
        if (now < bubbleUntil) {
            drawBubble(canvas);
        }
        postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float radius = companionRadius();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (distance(event.getX(), event.getY(), centerX, centerY) <= radius * 1.2f) {
                    dragging = true;
                    dragOffsetX = centerX - event.getX();
                    dragOffsetY = centerY - event.getY();
                    say(humanMode ? "Hey." : "Boop.");
                    if (listener != null) {
                        listener.onTouched();
                    }
                    return true;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    centerX = clamp(event.getX() + dragOffsetX, radius, getWidth() - radius);
                    centerY = clamp(event.getY() + dragOffsetY, radius * 1.2f, getHeight() - radius * 0.8f);
                    if (listener != null) {
                        listener.onDragged();
                    }
                    invalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging = false;
                return true;
            default:
                break;
        }
        return true;
    }

    private void drawRoom(Canvas canvas, float t) {
        int width = getWidth();
        int height = getHeight();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(229, 239, 232));
        canvas.drawRoundRect(0, 0, width, height, dp(24), dp(24), paint);

        paint.setColor(Color.rgb(248, 246, 241));
        rect.set(0, height * 0.58f, width, height);
        canvas.drawRect(rect, paint);

        paint.setColor(Color.rgb(47, 111, 115));
        rect.set(width * 0.08f, height * 0.12f, width * 0.3f, height * 0.2f);
        canvas.drawRoundRect(rect, dp(8), dp(8), paint);

        paint.setColor(Color.rgb(244, 196, 106));
        float sunX = width * 0.78f;
        float sunY = height * 0.18f + (float) Math.sin(t) * dp(3);
        canvas.drawCircle(sunX, sunY, dp(24), paint);

        paint.setColor(Color.rgb(90, 131, 128));
        rect.set(width * 0.12f, height * 0.7f, width * 0.88f, height * 0.75f);
        canvas.drawRoundRect(rect, dp(10), dp(10), paint);
    }

    private void drawCompanion(Canvas canvas, float t) {
        float radius = companionRadius();
        float bob = (float) Math.sin(t * (energy < 30 ? 2.0f : 3.2f)) * dp(7);
        float x = centerX;
        float y = centerY + bob;
        int bodyColor = humanMode ? Color.rgb(92, 119, 170) : Color.rgb(244, 196, 106);
        int faceColor = humanMode ? Color.rgb(246, 208, 171) : Color.rgb(255, 247, 225);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(45, 0, 0, 0));
        rect.set(x - radius * 0.75f, y + radius * 0.9f, x + radius * 0.75f, y + radius * 1.12f);
        canvas.drawOval(rect, paint);

        if (humanMode) {
            drawHuman(canvas, x, y, radius, bodyColor, faceColor);
        } else {
            drawPet(canvas, x, y, radius, bodyColor, faceColor);
        }
    }

    private void drawPet(Canvas canvas, float x, float y, float radius, int bodyColor, int faceColor) {
        paint.setColor(bodyColor);
        path.reset();
        path.moveTo(x - radius * 0.58f, y - radius * 0.25f);
        path.lineTo(x - radius * 0.85f, y - radius * 0.82f);
        path.lineTo(x - radius * 0.2f, y - radius * 0.55f);
        path.lineTo(x + radius * 0.2f, y - radius * 0.55f);
        path.lineTo(x + radius * 0.85f, y - radius * 0.82f);
        path.lineTo(x + radius * 0.58f, y - radius * 0.25f);
        path.close();
        canvas.drawPath(path, paint);

        rect.set(x - radius, y - radius * 0.7f, x + radius, y + radius * 0.95f);
        canvas.drawRoundRect(rect, radius * 0.42f, radius * 0.42f, paint);

        paint.setColor(faceColor);
        rect.set(x - radius * 0.66f, y - radius * 0.42f, x + radius * 0.66f, y + radius * 0.52f);
        canvas.drawRoundRect(rect, radius * 0.34f, radius * 0.34f, paint);
        drawFace(canvas, x, y, radius);

        paint.setColor(Color.rgb(47, 111, 115));
        rect.set(x + radius * 0.68f, y + radius * 0.1f, x + radius * 1.25f, y + radius * 0.38f);
        canvas.drawRoundRect(rect, radius * 0.18f, radius * 0.18f, paint);
    }

    private void drawHuman(Canvas canvas, float x, float y, float radius, int bodyColor, int faceColor) {
        paint.setColor(bodyColor);
        rect.set(x - radius * 0.58f, y + radius * 0.18f, x + radius * 0.58f, y + radius * 1.05f);
        canvas.drawRoundRect(rect, radius * 0.24f, radius * 0.24f, paint);

        paint.setColor(faceColor);
        rect.set(x - radius * 0.58f, y - radius * 0.66f, x + radius * 0.58f, y + radius * 0.34f);
        canvas.drawOval(rect, paint);

        paint.setColor(Color.rgb(31, 41, 51));
        rect.set(x - radius * 0.62f, y - radius * 0.75f, x + radius * 0.62f, y - radius * 0.14f);
        canvas.drawArc(rect, 180, 180, true, paint);
        drawFace(canvas, x, y - radius * 0.05f, radius * 0.9f);

        paint.setStrokeWidth(dp(7));
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(x - radius * 0.62f, y + radius * 0.34f, x - radius * 0.95f, y + radius * 0.74f, paint);
        canvas.drawLine(x + radius * 0.62f, y + radius * 0.34f, x + radius * 0.95f, y + radius * 0.74f, paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawFace(Canvas canvas, float x, float y, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(31, 41, 51));
        float eyeY = y - radius * 0.05f;
        canvas.drawCircle(x - radius * 0.28f, eyeY, radius * 0.055f, paint);
        canvas.drawCircle(x + radius * 0.28f, eyeY, radius * 0.055f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        float smile = happiness > 70 ? radius * 0.18f : radius * 0.06f;
        rect.set(x - radius * 0.22f, y + radius * 0.12f - smile, x + radius * 0.22f, y + radius * 0.18f + smile);
        canvas.drawArc(rect, 15, 150, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawBubble(Canvas canvas) {
        float radius = companionRadius();
        float bubbleWidth = Math.min(getWidth() - dp(36), Math.max(dp(150), paint.measureText(bubble) + dp(42)));
        float left = clamp(centerX - bubbleWidth * 0.5f, dp(12), getWidth() - bubbleWidth - dp(12));
        float top = Math.max(dp(14), centerY - radius * 1.65f);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        rect.set(left, top, left + bubbleWidth, top + dp(48));
        canvas.drawRoundRect(rect, dp(18), dp(18), paint);

        paint.setColor(Color.rgb(31, 41, 51));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dp(15));
        canvas.drawText(bubble, left + bubbleWidth * 0.5f, top + dp(31), paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private float companionRadius() {
        return Math.min(getWidth(), getHeight()) * 0.18f;
    }

    private float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}

