package com.pioneer.desktoppetorhumanmobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class OverlayPetView extends View {
    interface Host {
        void moveBy(float dx, float dy);

        void setMenuExpanded(boolean expanded);

        void closeOverlay();
    }

    private static final String[] CHARACTER_IDS = {
            "fengge", "fengxiong", "gugugaga", "laowangmao",
            "miaobaibai", "miaobubu", "miaoniang", "tuge",
            "xiaotu", "xiaoying", "yamei", "yumei"
    };
    private static final String[] CHARACTER_NAMES = {
            "鋒哥", "鋒兄", "咕咕嘎嘎", "老王貓",
            "喵白白", "喵布布", "喵娘", "兔哥",
            "小兔", "小影", "雅美", "玉美"
    };
    private static final String[] MENU_LABELS = {"角色", "模式", "餵食", "玩耍", "休息", "關閉"};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();
    private final Bitmap[] idleBitmaps = new Bitmap[CHARACTER_IDS.length];
    private final Bitmap[] happyBitmaps = new Bitmap[CHARACTER_IDS.length];
    private final Bitmap[] sleepBitmaps = new Bitmap[CHARACTER_IDS.length];
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (pressing && !dragging) {
                menuVisible = true;
                longPressTriggered = true;
                if (host != null) {
                    host.setMenuExpanded(true);
                }
                say("選單");
            }
        }
    };

    private Host host;
    private boolean humanMode = true;
    private int characterIndex;
    private boolean menuVisible;
    private boolean pressing;
    private boolean dragging;
    private boolean longPressTriggered;
    private float lastRawX;
    private float lastRawY;
    private float downX;
    private float downY;
    private int happiness = 76;
    private int energy = 66;
    private int fullness = 63;
    private int touchSlop;
    private String bubble = "";
    private long bubbleUntil;

    public OverlayPetView(Context context) {
        super(context);
        setWillNotDraw(false);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        for (int i = 0; i < CHARACTER_IDS.length; i++) {
            idleBitmaps[i] = loadBitmap(CHARACTER_IDS[i] + "_idle");
            happyBitmaps[i] = loadBitmap(CHARACTER_IDS[i] + "_happy");
            sleepBitmaps[i] = loadBitmap(CHARACTER_IDS[i] + "_sleep");
        }
    }

    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (menuVisible) {
            drawMenu(canvas);
        }
        drawPet(canvas);
        if (System.currentTimeMillis() < bubbleUntil) {
            drawBubble(canvas);
        }
        postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                lastRawX = event.getRawX();
                lastRawY = event.getRawY();
                longPressTriggered = false;
                if (menuVisible) {
                    int item = menuItemAt(downX, downY);
                    if (item >= 0) {
                        handleMenuAction(item);
                        return true;
                    }
                    hideMenu();
                }
                if (isInsidePet(downX, downY)) {
                    pressing = true;
                    handler.postDelayed(longPressRunnable, ViewConfiguration.getLongPressTimeout());
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (pressing && !longPressTriggered
                        && distance(event.getX(), event.getY(), downX, downY) > touchSlop) {
                    handler.removeCallbacks(longPressRunnable);
                    dragging = true;
                    hideMenu();
                }
                if (dragging && host != null) {
                    float dx = event.getRawX() - lastRawX;
                    float dy = event.getRawY() - lastRawY;
                    host.moveBy(dx, dy);
                    lastRawX = event.getRawX();
                    lastRawY = event.getRawY();
                    updateStats(1, -1, -1);
                }
                return true;
            case MotionEvent.ACTION_UP:
                handler.removeCallbacks(longPressRunnable);
                if (pressing && !dragging && !longPressTriggered) {
                    updateStats(3, -1, -1);
                    say(humanMode ? "嗨。" : "啾。");
                }
                pressing = false;
                dragging = false;
                return true;
            case MotionEvent.ACTION_CANCEL:
                handler.removeCallbacks(longPressRunnable);
                pressing = false;
                dragging = false;
                return true;
            default:
                return true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(longPressRunnable);
        super.onDetachedFromWindow();
    }

    private void handleMenuAction(int index) {
        switch (index) {
            case 0:
                characterIndex = (characterIndex + 1) % CHARACTER_IDS.length;
                humanMode = true;
                happiness = 82;
                energy = Math.max(energy, 48);
                say(CHARACTER_NAMES[characterIndex] + " " + (characterIndex + 1) + "/12");
                break;
            case 1:
                humanMode = !humanMode;
                say(humanMode ? "人形模式" : "企鵝模式");
                break;
            case 2:
                updateStats(8, -2, 20);
                say("已餵食");
                break;
            case 3:
                updateStats(18, -14, -6);
                say("一起玩");
                break;
            case 4:
                updateStats(4, 24, -2);
                say("休息中");
                break;
            case 5:
                if (host != null) {
                    host.closeOverlay();
                }
                break;
            default:
                break;
        }
        hideMenu();
    }

    private void drawPet(Canvas canvas) {
        Bitmap bitmap = currentBitmap();
        float size = petSize();
        float left = getWidth() - size - dp(12);
        float top = getHeight() - size - dp(8);
        paint.setColor(Color.argb(100, 0, 0, 0));
        rect.set(left + size * 0.22f, top + size * 0.84f, left + size * 0.86f, top + size * 0.96f);
        canvas.drawOval(rect, paint);
        paint.setAlpha(255);
        if (humanMode && bitmap != null) {
            rect.set(left, top, left + size, top + size);
            canvas.drawBitmap(bitmap, null, rect, paint);
        } else {
            drawFallbackPet(canvas, left, top, size);
        }
    }

    private void drawFallbackPet(Canvas canvas, float left, float top, float size) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(29, 31, 36));
        rect.set(left + size * 0.18f, top + size * 0.14f, left + size * 0.82f, top + size * 0.88f);
        canvas.drawOval(rect, paint);
        paint.setColor(Color.rgb(246, 246, 239));
        rect.set(left + size * 0.32f, top + size * 0.43f, left + size * 0.68f, top + size * 0.78f);
        canvas.drawOval(rect, paint);
        paint.setColor(Color.rgb(249, 190, 44));
        path.reset();
        path.moveTo(left + size * 0.44f, top + size * 0.35f);
        path.lineTo(left + size * 0.6f, top + size * 0.42f);
        path.lineTo(left + size * 0.44f, top + size * 0.49f);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawMenu(Canvas canvas) {
        float width = dp(204);
        float itemHeight = menuItemHeight();
        float height = itemHeight * MENU_LABELS.length + dp(14);
        float left = getWidth() - width - dp(16);
        float top = dp(8);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, top, 0, top + height,
                Color.argb(240, 45, 46, 52), Color.argb(240, 20, 21, 25), Shader.TileMode.CLAMP));
        rect.set(left, top, left + width, top + height);
        canvas.drawRoundRect(rect, dp(22), dp(22), paint);
        paint.setShader(null);

        for (int i = 0; i < MENU_LABELS.length; i++) {
            float rowTop = top + dp(7) + i * itemHeight;
            if (i > 0) {
                paint.setColor(Color.argb(58, 255, 255, 255));
                canvas.drawRect(left + dp(52), rowTop, left + width - dp(14), rowTop + dp(1), paint);
            }
            drawMenuGlyph(canvas, left + dp(28), rowTop + itemHeight * 0.5f, i);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextSize(dp(15));
            canvas.drawText(MENU_LABELS[i], left + dp(56), rowTop + itemHeight * 0.62f, paint);
            paint.setTypeface(Typeface.DEFAULT);
        }
    }

    private void drawMenuGlyph(Canvas canvas, float x, float y, int index) {
        int color = index == 0 ? Color.rgb(81, 156, 255)
                : index == 1 ? Color.rgb(255, 193, 87)
                : index == 2 ? Color.rgb(84, 224, 139)
                : index == 3 ? Color.rgb(178, 145, 255)
                : index == 4 ? Color.rgb(120, 205, 255)
                : Color.rgb(255, 100, 100);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(x, y, dp(14), paint);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(13));
        canvas.drawText(index == 5 ? "X" : MENU_LABELS[index].substring(0, 1), x, y + dp(5), paint);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawBubble(Canvas canvas) {
        float size = petSize();
        float bubbleWidth = Math.max(dp(74), paint.measureText(bubble) + dp(34));
        float left = getWidth() - size - dp(36);
        float top = getHeight() - size - dp(44);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(236, 255, 255, 255));
        rect.set(left, top, left + bubbleWidth, top + dp(38));
        canvas.drawRoundRect(rect, dp(17), dp(17), paint);
        paint.setColor(Color.rgb(28, 31, 36));
        paint.setTextSize(dp(14));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(bubble, left + bubbleWidth * 0.5f, top + dp(25), paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private int menuItemAt(float x, float y) {
        float width = dp(204);
        float itemHeight = menuItemHeight();
        float height = itemHeight * MENU_LABELS.length + dp(14);
        float left = getWidth() - width - dp(16);
        float top = dp(8);
        if (x < left || x > left + width || y < top || y > top + height) {
            return -1;
        }
        int index = (int) ((y - top - dp(7)) / itemHeight);
        return index >= 0 && index < MENU_LABELS.length ? index : -1;
    }

    private boolean isInsidePet(float x, float y) {
        float size = petSize();
        float left = getWidth() - size - dp(12);
        float top = getHeight() - size - dp(8);
        return x >= left && x <= left + size && y >= top && y <= top + size;
    }

    private void hideMenu() {
        if (menuVisible) {
            menuVisible = false;
            if (host != null) {
                host.setMenuExpanded(false);
            }
            invalidate();
        }
    }

    private void say(String text) {
        bubble = text;
        bubbleUntil = System.currentTimeMillis() + 1800;
        invalidate();
    }

    private void updateStats(int happinessDelta, int energyDelta, int fullnessDelta) {
        happiness = clampInt(happiness + happinessDelta);
        energy = clampInt(energy + energyDelta);
        fullness = clampInt(fullness + fullnessDelta);
        invalidate();
    }

    private Bitmap currentBitmap() {
        if (energy < 34 && sleepBitmaps[characterIndex] != null) {
            return sleepBitmaps[characterIndex];
        }
        if (happiness > 78 && happyBitmaps[characterIndex] != null) {
            return happyBitmaps[characterIndex];
        }
        return idleBitmaps[characterIndex];
    }

    private Bitmap loadBitmap(String name) {
        int id = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
        return id == 0 ? null : BitmapFactory.decodeResource(getResources(), id);
    }

    private float petSize() {
        return Math.min(getWidth(), getHeight()) * 0.9f;
    }

    private float menuItemHeight() {
        return dp(46);
    }

    private float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private int clampInt(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
