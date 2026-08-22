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
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

public class OverlayPetView extends View {
    static final int MODE_COMPACT = 0;
    static final int MODE_MENU = 1;
    static final int MODE_PICKER = 2;

    interface Host {
        void moveBy(float dx, float dy);

        void setOverlayMode(int mode);

        void setSizeLevel(int level);

        void setStayOnTop(boolean stayOnTop);

        boolean isStayOnTop();

        void resetToBottomRight();

        void closeOverlay();
    }

    static final String[] CHARACTER_IDS = {
            "fengge", "fengxiong", "gugugaga", "laowangmao",
            "miaobaibai", "miaobubu", "miaoniang", "tuge",
            "xiaotu", "xiaoying", "yamei", "yumei"
    };
    private static final String[] CHARACTER_NAMES = {
            "鋒哥", "鋒兄", "咕咕嘎嘎", "老王貓",
            "喵白白", "喵布布", "喵娘", "兔哥",
            "小兔", "小影", "雅美", "玉美"
    };
    private static final String[] MENU_LABELS = {"角色", "模式", "睡一下", "取消置頂", "回到右下角", "關閉"};
    private static final boolean[] PRIMARY_IS_HUMAN = {
            true, true, false, false,
            false, false, true, true,
            true, true, true, true
    };
    private static final String[] PET_MODE_NAMES = {
            "黑貓模式", "柴犬模式", "企鵝模式", "招財貓模式",
            "白貓模式", "三花模式", "貓咪模式", "兔子模式",
            "兔子模式", "黑貓模式", "紅貓模式", "海豚模式"
    };
    private static final long CHEER_DURATION_MS = 3000;
    private static final long NAP_DURATION_MS = 3000;
    private static final int FORM_HUMAN = 0;
    private static final int FORM_PET = 1;
    static final int SIZE_LEVEL_COUNT = 5;
    static final int DEFAULT_SIZE_LEVEL = SIZE_LEVEL_COUNT - 1;
    static final int[] COMPACT_DP = {168, 210, 260, 324, 400};
    private static final int PICKER_COLS = 3;
    private static final int PICKER_ROWS = 4;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();
    private final Bitmap[][] idleBitmaps = new Bitmap[2][CHARACTER_IDS.length];
    private final Bitmap[][] happyBitmaps = new Bitmap[2][CHARACTER_IDS.length];
    private final Bitmap[][] sleepBitmaps = new Bitmap[2][CHARACTER_IDS.length];
    private final boolean[] humanForm = new boolean[CHARACTER_IDS.length];
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private final Runnable longPressRunnable = new Runnable() {
        @Override
        public void run() {
            if (pressing && !dragging) {
                pickerVisible = false;
                menuVisible = true;
                longPressTriggered = true;
                if (host != null) {
                    host.setOverlayMode(MODE_MENU);
                }
                say("選單");
            }
        }
    };
    private final Runnable idleTalkRunnable = new Runnable() {
        @Override
        public void run() {
            maybeIdleTalk();
            handler.postDelayed(this, 8500);
        }
    };

    private Host host;
    private int characterIndex;
    private boolean menuVisible;
    private boolean pickerVisible;
    private boolean pressing;
    private boolean dragging;
    private boolean pinching;
    private boolean scaledThisGesture;
    private boolean longPressTriggered;
    private int sizeLevel = DEFAULT_SIZE_LEVEL;
    private int pinchStartLevel;
    private float pinchStartSpan;
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
    private long cheerUntil;
    private long napUntil;
    private int idleTick;
    private boolean closing;

    public OverlayPetView(Context context) {
        super(context);
        setWillNotDraw(false);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                pinching = true;
                scaledThisGesture = true;
                pressing = false;
                dragging = false;
                handler.removeCallbacks(longPressRunnable);
                hideMenu();
                pinchStartSpan = detector.getCurrentSpan();
                pinchStartLevel = sizeLevel;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (pinchStartSpan <= 1f) {
                    return true;
                }
                float ratio = detector.getCurrentSpan() / pinchStartSpan;
                int delta = Math.round((float) (Math.log(ratio) / Math.log(1.23)));
                int next = Math.max(0, Math.min(SIZE_LEVEL_COUNT - 1, pinchStartLevel + delta));
                applySizeLevel(next);
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                pinching = false;
            }
        });
        scaleDetector.setQuickScaleEnabled(false);
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent event) {
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent event) {
                if (shouldIgnoreTap(event)) {
                    return false;
                }
                greet();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent event) {
                if (shouldIgnoreTap(event)) {
                    return false;
                }
                cheer();
                return true;
            }
        });
        gestureDetector.setIsLongpressEnabled(false);
        for (int i = 0; i < CHARACTER_IDS.length; i++) {
            Bitmap idle = loadBitmap(CHARACTER_IDS[i] + "_idle");
            Bitmap happy = loadBitmap(CHARACTER_IDS[i] + "_happy");
            Bitmap sleep = loadBitmap(CHARACTER_IDS[i] + "_sleep");
            if (PRIMARY_IS_HUMAN[i]) {
                idleBitmaps[FORM_HUMAN][i] = idle;
                happyBitmaps[FORM_HUMAN][i] = happy;
                sleepBitmaps[FORM_HUMAN][i] = sleep;
                idleBitmaps[FORM_PET][i] = loadBitmap(CHARACTER_IDS[i] + "_pet_idle");
                happyBitmaps[FORM_PET][i] = firstBitmap(
                        loadBitmap(CHARACTER_IDS[i] + "_pet_happy"), idleBitmaps[FORM_PET][i]);
                sleepBitmaps[FORM_PET][i] = firstBitmap(
                        loadBitmap(CHARACTER_IDS[i] + "_pet_sleep"), idleBitmaps[FORM_PET][i]);
                humanForm[i] = true;
            } else {
                idleBitmaps[FORM_PET][i] = idle;
                happyBitmaps[FORM_PET][i] = happy;
                sleepBitmaps[FORM_PET][i] = sleep;
                idleBitmaps[FORM_HUMAN][i] = loadBitmap(CHARACTER_IDS[i] + "_human_idle");
                happyBitmaps[FORM_HUMAN][i] = firstBitmap(
                        loadBitmap(CHARACTER_IDS[i] + "_human_happy"), idleBitmaps[FORM_HUMAN][i]);
                sleepBitmaps[FORM_HUMAN][i] = firstBitmap(
                        loadBitmap(CHARACTER_IDS[i] + "_human_sleep"), idleBitmaps[FORM_HUMAN][i]);
                humanForm[i] = false;
            }
        }
        int saved = OverlayPetService.savedCharacterIndex(context);
        characterIndex = saved >= 0 && saved < CHARACTER_IDS.length ? saved : 0;
        sizeLevel = OverlayPetService.savedSizeLevel(context);
    }

    public void setHost(Host host) {
        this.host = host;
    }

    void applyCompensationRotation(int clockwiseDegrees) {
        int degrees = ((clockwiseDegrees % 360) + 360) % 360;
        if (getWidth() > 0 && getHeight() > 0) {
            setPivotX(getWidth() / 2f);
            setPivotY(getHeight() / 2f);
        }
        if (Math.abs(getRotation() - degrees) > 0.5f) {
            setRotation(degrees);
        }
    }

    void collapsePanels() {
        hideMenu();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setPivotX(w / 2f);
        setPivotY(h / 2f);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.removeCallbacks(idleTalkRunnable);
        handler.postDelayed(idleTalkRunnable, 8500);
        say(CharacterTalk.greeting(characterIndex), 3200);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        idleTick++;
        if (menuVisible) {
            drawMenu(canvas);
        }
        drawPet(canvas);
        if (pickerVisible) {
            drawPicker(canvas);
        } else if (System.currentTimeMillis() < bubbleUntil) {
            drawBubble(canvas);
        }
        postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        int action = event.getActionMasked();
        if (pinching || scaleDetector.isInProgress() || (scaledThisGesture && action != MotionEvent.ACTION_DOWN)) {
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                pinching = false;
                scaledThisGesture = false;
                pressing = false;
                dragging = false;
                handler.removeCallbacks(longPressRunnable);
            }
            return true;
        }
        gestureDetector.onTouchEvent(event);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                lastRawX = event.getRawX();
                lastRawY = event.getRawY();
                longPressTriggered = false;
                scaledThisGesture = false;
                pinching = false;
                if (pickerVisible) {
                    int character = characterAt(downX, downY);
                    if (character >= 0) {
                        selectCharacter(character);
                        return true;
                    }
                    if (isInsidePickerPanel(downX, downY)) {
                        showMenu();
                        return true;
                    }
                    hideMenu();
                    return true;
                }
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
                if (event.getPointerCount() >= 2) {
                    return true;
                }
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
        handler.removeCallbacks(idleTalkRunnable);
        super.onDetachedFromWindow();
    }

    private void handleMenuAction(int index) {
        switch (index) {
            case 0:
                showPicker();
                return;
            case 1:
                humanForm[characterIndex] = !humanForm[characterIndex];
                say(modeLabel());
                break;
            case 2:
                nap();
                break;
            case 3:
                if (host != null) {
                    boolean stayOnTop = !host.isStayOnTop();
                    host.setStayOnTop(stayOnTop);
                    say(stayOnTop ? "保持置頂" : "已取消置頂");
                }
                break;
            case 4:
                hideMenu();
                if (host != null) {
                    host.resetToBottomRight();
                }
                say("回到右下角");
                return;
            case 5:
                beginFarewell();
                return;
            default:
                break;
        }
        hideMenu();
    }

    private void selectCharacter(int index) {
        characterIndex = index;
        OverlayPetService.saveCharacterIndex(getContext(), characterIndex);
        happiness = 82;
        energy = Math.max(energy, 48);
        say(CharacterTalk.greeting(characterIndex), 3200);
        hideMenu();
    }

    private void showPicker() {
        menuVisible = false;
        pickerVisible = true;
        if (host != null) {
            host.setOverlayMode(MODE_PICKER);
        }
        invalidate();
    }

    private void showMenu() {
        pickerVisible = false;
        menuVisible = true;
        if (host != null) {
            host.setOverlayMode(MODE_MENU);
        }
        invalidate();
    }

    private boolean shouldIgnoreTap(MotionEvent event) {
        return dragging || longPressTriggered || scaledThisGesture || menuVisible || pickerVisible
                || !isInsidePet(event.getX(), event.getY());
    }

    private void greet() {
        updateStats(3, -1, -1);
        say(CharacterTalk.idle(characterIndex), 2400);
    }

    private void cheer() {
        handler.removeCallbacks(longPressRunnable);
        pressing = false;
        napUntil = 0;
        cheerUntil = System.currentTimeMillis() + CHEER_DURATION_MS;
        updateStats(12, -4, -2);
        say(CharacterTalk.cheer(characterIndex), 2400);
    }

    private float cheerHop() {
        long now = System.currentTimeMillis();
        if (now >= cheerUntil) {
            return 0;
        }
        float elapsed = (CHEER_DURATION_MS - (cheerUntil - now)) / 1000f;
        if (elapsed >= 1.2f) {
            return 0;
        }
        return Math.abs((float) Math.sin(elapsed * Math.PI * 3.5)) * dp(16) * Math.max(0f, 1f - elapsed / 1.2f);
    }

    private void nap() {
        cheerUntil = 0;
        napUntil = System.currentTimeMillis() + NAP_DURATION_MS;
        say(CharacterTalk.sleep(characterIndex), 2400);
    }

    private void beginFarewell() {
        if (closing) {
            return;
        }
        closing = true;
        hideMenu();
        say(CharacterTalk.farewell(characterIndex), 2400);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (host != null) {
                    host.closeOverlay();
                }
            }
        }, 1800);
    }

    boolean speakAboutApp(String packageName, String appName, boolean opened) {
        if (closing || menuVisible || pickerVisible || getVisibility() != VISIBLE) {
            return false;
        }
        String key = CHARACTER_IDS[characterIndex];
        String kind = AppTalk.kindOf(packageName);
        String line = opened ? AppTalk.forOpen(key, appName, kind) : AppTalk.forClose(key, appName, kind);
        say(line, 3400);
        return true;
    }

    private void maybeIdleTalk() {
        if (closing || menuVisible || pickerVisible || getVisibility() != VISIBLE) {
            return;
        }
        if (System.currentTimeMillis() < bubbleUntil) {
            return;
        }
        say(CharacterTalk.idle(characterIndex), 2400);
    }

    private void applySizeLevel(int level) {
        if (level == sizeLevel) {
            return;
        }
        sizeLevel = level;
        if (host != null) {
            host.setSizeLevel(sizeLevel);
        }
        say("大小 " + (sizeLevel + 1) + "/" + SIZE_LEVEL_COUNT);
        invalidate();
    }

    private void drawPet(Canvas canvas) {
        Bitmap bitmap = currentBitmap();
        float size = petSize();
        float left = getWidth() - size - dp(12);
        float top = getHeight() - size - dp(8) - cheerHop();
        float breathe = 1f + (float) Math.sin(idleTick / 9.0) * 0.018f;
        float tilt = (float) Math.sin(idleTick / 15.0) * 2.3f;
        if (dragging) {
            tilt *= 2f;
        }
        canvas.save();
        float cx = left + size * 0.5f;
        float cy = top + size * 0.5f;
        canvas.translate(cx, cy);
        canvas.rotate(tilt);
        canvas.scale(breathe, 1f / breathe);
        canvas.translate(-cx, -cy);
        paint.setColor(Color.argb(100, 0, 0, 0));
        rect.set(left + size * 0.22f, top + size * 0.84f, left + size * 0.86f, top + size * 0.96f);
        canvas.drawOval(rect, paint);
        paint.setAlpha(255);
        if (bitmap != null) {
            rect.set(left, top, left + size, top + size);
            canvas.drawBitmap(bitmap, null, rect, paint);
        } else {
            drawFallbackPet(canvas, left, top, size);
        }
        canvas.restore();
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
            canvas.drawText(menuLabel(i), left + dp(56), rowTop + itemHeight * 0.62f, paint);
            paint.setTypeface(Typeface.DEFAULT);
        }
    }

    private void drawMenuGlyph(Canvas canvas, float x, float y, int index) {
        int color = index == 0 ? Color.rgb(81, 156, 255)
                : index == 1 ? Color.rgb(255, 193, 87)
                : index == 2 ? Color.rgb(164, 148, 255)
                : index == 3 ? Color.rgb(80, 196, 186)
                : index == 4 ? Color.rgb(255, 170, 80)
                : Color.rgb(255, 100, 100);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(x, y, dp(14), paint);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(13));
        String glyph = index == MENU_LABELS.length - 1 ? "X" : menuLabel(index).substring(0, 1);
        canvas.drawText(glyph, x, y + dp(5), paint);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawPicker(Canvas canvas) {
        float panelLeft = pickerLeft();
        float panelTop = pickerTop();
        float panelWidth = pickerPanelWidth();
        float gap = pickerGap();
        float innerLeft = pickerInnerLeft();
        float innerTop = pickerInnerTop();
        float cellW = pickerCellWidth();
        float cellH = pickerCellHeight();
        float thumb = pickerThumbSize();
        float panelHeight = pickerPanelHeight();

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, panelTop, 0, panelTop + panelHeight,
                Color.argb(244, 45, 46, 52), Color.argb(244, 20, 21, 25), Shader.TileMode.CLAMP));
        rect.set(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight);
        canvas.drawRoundRect(rect, dp(22), dp(22), paint);
        paint.setShader(null);

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(15));
        canvas.drawText("選擇角色", panelLeft + panelWidth * 0.5f, panelTop + dp(24), paint);
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextSize(dp(11));
        paint.setColor(Color.argb(180, 200, 205, 215));
        canvas.drawText("點選頭像切換  ·  點標題返回", panelLeft + panelWidth * 0.5f, panelTop + dp(40), paint);

        for (int i = 0; i < CHARACTER_IDS.length; i++) {
            int col = i % PICKER_COLS;
            int row = i / PICKER_COLS;
            float cellLeft = innerLeft + col * (cellW + gap);
            float cellTop = innerTop + row * (cellH + gap);
            boolean selected = i == characterIndex;
            if (selected) {
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(Color.argb(90, 81, 156, 255));
                rect.set(cellLeft, cellTop, cellLeft + cellW, cellTop + cellH);
                canvas.drawRoundRect(rect, dp(16), dp(16), paint);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(dp(2));
                paint.setColor(Color.rgb(120, 180, 255));
                canvas.drawRoundRect(rect, dp(16), dp(16), paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(0);
            }
            paint.setAlpha(255);
            float thumbLeft = cellLeft + (cellW - thumb) * 0.5f;
            float thumbTop = cellTop + dp(4);
            rect.set(thumbLeft, thumbTop, thumbLeft + thumb, thumbTop + thumb);
            Bitmap bitmap = rosterBitmap(i);
            if (bitmap != null) {
                canvas.drawBitmap(bitmap, null, rect, paint);
            }
            paint.setColor(selected ? Color.rgb(168, 206, 255) : Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTypeface(selected ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            paint.setTextSize(dp(11));
            canvas.drawText(CHARACTER_NAMES[i], cellLeft + cellW * 0.5f, thumbTop + thumb + dp(14), paint);
        }
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setAlpha(255);
    }

    private void drawBubble(Canvas canvas) {
        if (bubble == null || bubble.length() == 0) {
            return;
        }
        float size = petSize();
        paint.setTextSize(dp(14));
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.LEFT);
        TextPaint textPaint = new TextPaint(paint);
        textPaint.setColor(Color.rgb(28, 31, 36));
        int maxWidth = dp(168);
        StaticLayout layout = StaticLayout.Builder
                .obtain(bubble, 0, bubble.length(), textPaint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0, 1f)
                .build();
        float textWidth = 0;
        for (int i = 0; i < layout.getLineCount(); i++) {
            textWidth = Math.max(textWidth, layout.getLineWidth(i));
        }
        float bubbleWidth = Math.max(dp(74), textWidth + dp(34));
        float bubbleHeight = layout.getHeight() + dp(20);
        float left = getWidth() - size - dp(36);
        if (left + bubbleWidth > getWidth() - dp(8)) {
            left = getWidth() - bubbleWidth - dp(8);
        }
        if (left < dp(8)) {
            left = dp(8);
        }
        float top = getHeight() - size - bubbleHeight - dp(10) - cheerHop();
        if (top < dp(8)) {
            top = dp(8);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(236, 255, 255, 255));
        rect.set(left, top, left + bubbleWidth, top + bubbleHeight);
        canvas.drawRoundRect(rect, dp(17), dp(17), paint);
        canvas.save();
        canvas.translate(left + (bubbleWidth - maxWidth) * 0.5f, top + dp(10));
        layout.draw(canvas);
        canvas.restore();
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

    private int characterAt(float x, float y) {
        float gap = pickerGap();
        float innerLeft = pickerInnerLeft();
        float innerTop = pickerInnerTop();
        float cellW = pickerCellWidth();
        float cellH = pickerCellHeight();
        if (x < innerLeft || y < innerTop) {
            return -1;
        }
        int col = (int) ((x - innerLeft) / (cellW + gap));
        int row = (int) ((y - innerTop) / (cellH + gap));
        if (col < 0 || col >= PICKER_COLS || row < 0 || row >= PICKER_ROWS) {
            return -1;
        }
        float cellLeft = innerLeft + col * (cellW + gap);
        float cellTop = innerTop + row * (cellH + gap);
        if (x > cellLeft + cellW || y > cellTop + cellH) {
            return -1;
        }
        int index = row * PICKER_COLS + col;
        return index < CHARACTER_IDS.length ? index : -1;
    }

    private boolean isInsidePickerPanel(float x, float y) {
        float left = pickerLeft();
        float top = pickerTop();
        return x >= left && x <= left + pickerPanelWidth()
                && y >= top && y <= top + pickerPanelHeight();
    }

    private boolean isInsidePet(float x, float y) {
        float size = petSize();
        float left = getWidth() - size - dp(12);
        float top = getHeight() - size - dp(8);
        return x >= left && x <= left + size && y >= top && y <= top + size;
    }

    private void hideMenu() {
        if (!menuVisible && !pickerVisible) {
            return;
        }
        menuVisible = false;
        pickerVisible = false;
        if (host != null) {
            host.setOverlayMode(MODE_COMPACT);
        }
        invalidate();
    }

    private void say(String text) {
        say(text, 1800);
    }

    private void say(String text, long durationMs) {
        bubble = text;
        bubbleUntil = System.currentTimeMillis() + durationMs;
        invalidate();
    }

    private void updateStats(int happinessDelta, int energyDelta, int fullnessDelta) {
        happiness = clampInt(happiness + happinessDelta);
        energy = clampInt(energy + energyDelta);
        fullness = clampInt(fullness + fullnessDelta);
        invalidate();
    }

    private Bitmap currentBitmap() {
        int form = humanForm[characterIndex] ? FORM_HUMAN : FORM_PET;
        int i = characterIndex;
        if (System.currentTimeMillis() < cheerUntil && happyBitmaps[form][i] != null) {
            return happyBitmaps[form][i];
        }
        if (System.currentTimeMillis() < napUntil && sleepBitmaps[form][i] != null) {
            return sleepBitmaps[form][i];
        }
        if (energy < 34 && sleepBitmaps[form][i] != null) {
            return sleepBitmaps[form][i];
        }
        if (happiness > 78 && happyBitmaps[form][i] != null) {
            return happyBitmaps[form][i];
        }
        if (idleBitmaps[form][i] != null) {
            return idleBitmaps[form][i];
        }
        return rosterBitmap(i);
    }

    private Bitmap rosterBitmap(int index) {
        return PRIMARY_IS_HUMAN[index]
                ? idleBitmaps[FORM_HUMAN][index]
                : idleBitmaps[FORM_PET][index];
    }

    private String modeLabel() {
        return humanForm[characterIndex] ? "人形模式" : PET_MODE_NAMES[characterIndex];
    }

    private Bitmap firstBitmap(Bitmap preferred, Bitmap fallback) {
        return preferred != null ? preferred : fallback;
    }

    private Bitmap loadBitmap(String name) {
        int id = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
        return id == 0 ? null : BitmapFactory.decodeResource(getResources(), id);
    }

    private float petSize() {
        return Math.min(getWidth(), getHeight()) * 0.9f;
    }

    private String menuLabel(int index) {
        if (index == 3) {
            boolean stayOnTop = host == null || host.isStayOnTop();
            return stayOnTop ? "取消置頂" : "保持置頂";
        }
        return MENU_LABELS[index];
    }

    private float menuItemHeight() {
        return dp(46);
    }

    private float pickerLeft() {
        return dp(8);
    }

    private float pickerTop() {
        return dp(8);
    }

    private float pickerPanelWidth() {
        return getWidth() - dp(16);
    }

    private float pickerTitleHeight() {
        return dp(48);
    }

    private float pickerGap() {
        return dp(6);
    }

    private float pickerInnerLeft() {
        return pickerLeft() + dp(10);
    }

    private float pickerInnerTop() {
        return pickerTop() + pickerTitleHeight();
    }

    private float pickerCellWidth() {
        return (pickerPanelWidth() - dp(20) - pickerGap() * (PICKER_COLS - 1)) / PICKER_COLS;
    }

    private float pickerThumbSize() {
        return pickerCellWidth() * 0.78f;
    }

    private float pickerCellHeight() {
        return pickerThumbSize() + dp(22);
    }

    private float pickerPanelHeight() {
        return pickerTitleHeight()
                + pickerCellHeight() * PICKER_ROWS
                + pickerGap() * (PICKER_ROWS - 1)
                + dp(12);
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
