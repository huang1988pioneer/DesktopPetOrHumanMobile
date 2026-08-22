package com.pioneer.desktoppetorhumanmobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DesktopPetView extends View {
    interface CompanionListener {
        void onTouched();

        void onDragged();
    }

    private static final String[] APP_LABELS = {
            "檔案", "Wondershare\nFilmora 15", "CapCut", "Corel FastFlick", "Antigravity",
            "威力導演 365", "Grok Bot", "Trae", "MiniMax Design", "PotPlayer 64 bit",
            "資源回收筒", "Bandizip", "SuperGrok", "Chrome", "相簿",
            "Microsoft Edge", "Everything", "Firefox", "阿不點 - Chrome", "DaVinci Resolve",
            "Visual Studio Code", "下載 Promeo", "三星瀏覽器", "KMPlayer 64X", "蠻鋒 - Chrome",
            "Google", "Typeless"
    };

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private final Path path = new Path();
    private final SimpleDateFormat clockFormat = new SimpleDateFormat("HH:mm", Locale.US);
    private final Bitmap petIdleBitmap;
    private final Bitmap petHappyBitmap;
    private final Bitmap petSleepBitmap;
    private CompanionListener listener;
    private boolean humanMode = true;
    private float petX;
    private float petY;
    private float dragOffsetX;
    private float dragOffsetY;
    private boolean dragging;
    private int happiness = 76;
    private int energy = 66;
    private int fullness = 63;
    private long startTime = System.currentTimeMillis();
    private String bubble = "";
    private long bubbleUntil;

    public DesktopPetView(Context context) {
        super(context);
        setFocusable(true);
        petIdleBitmap = loadBitmap("pet_idle");
        petHappyBitmap = loadBitmap("pet_happy");
        petSleepBitmap = loadBitmap("pet_sleep");
    }

    public void setCompanionListener(CompanionListener listener) {
        this.listener = listener;
    }

    public void setStats(int happiness, int energy, int fullness) {
        this.happiness = clampInt(happiness);
        this.energy = clampInt(energy);
        this.fullness = clampInt(fullness);
        invalidate();
    }

    public void say(String text) {
        bubble = text;
        bubbleUntil = System.currentTimeMillis() + 2400;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        petX = width * 0.78f;
        petY = height - dockHeight() - dp(18);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        long now = System.currentTimeMillis();
        float t = (now - startTime) / 1000f;
        drawWallpaper(canvas, t);
        drawStatusBar(canvas);
        drawHomeIcons(canvas);
        drawCompanion(canvas, t);
        if (now < bubbleUntil) {
            drawBubble(canvas);
        }
        drawPageDots(canvas);
        drawDock(canvas);
        postInvalidateOnAnimation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float radius = companionRadius();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                int dockItem = dockItemAt(event.getX(), event.getY());
                if (dockItem >= 0) {
                    handleDockAction(dockItem);
                    return true;
                }
                if (distance(event.getX(), event.getY(), petX, petY - radius * 0.54f) <= radius * 1.34f) {
                    dragging = true;
                    dragOffsetX = petX - event.getX();
                    dragOffsetY = petY - event.getY();
                    updateStats(3, -1, -1);
                    say(humanMode ? "嗨，我在這裡。" : "啾。");
                    if (listener != null) {
                        listener.onTouched();
                    }
                    return true;
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (dragging) {
                    petX = clamp(event.getX() + dragOffsetX, radius * 0.75f, getWidth() - radius * 0.75f);
                    petY = clamp(event.getY() + dragOffsetY, statusTop() + radius * 1.55f,
                            getHeight() - dockHeight() + radius * 0.18f);
                    updateStats(1, -1, -1);
                    if (listener != null) {
                        listener.onDragged();
                    }
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

    private void handleDockAction(int index) {
        switch (index) {
            case 0:
                humanMode = !humanMode;
                say(humanMode ? "人形模式" : "企鵝模式");
                break;
            case 1:
                updateStats(4, -2, 18);
                say("收到點心。");
                break;
            case 2:
                updateStats(16, -12, -5);
                say("拍一張！");
                break;
            case 3:
                updateStats(3, 20, -2);
                say("設定：休息一下。");
                break;
            default:
                break;
        }
        invalidate();
    }

    private void drawWallpaper(Canvas canvas, float t) {
        int width = getWidth();
        int height = getHeight();
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, 0, width, height,
                new int[] {
                        Color.rgb(15, 17, 23),
                        Color.rgb(29, 29, 38),
                        Color.rgb(59, 39, 44),
                        Color.rgb(10, 12, 16)
                },
                new float[] {0f, 0.34f, 0.72f, 1f},
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(null);

        drawMilkyWay(canvas, width, height);
        drawAurora(canvas, width, height, t);
        drawStars(canvas, width, height, t);
        drawTree(canvas, width, height);
        drawForeground(canvas, width, height);
    }

    private void drawMilkyWay(Canvas canvas, int width, int height) {
        paint.setShader(new RadialGradient(width * 0.64f, height * 0.36f, width * 0.42f,
                new int[] {Color.argb(84, 224, 226, 236), Color.argb(28, 180, 166, 158),
                        Color.TRANSPARENT},
                new float[] {0f, 0.42f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(width * 0.64f, height * 0.36f, width * 0.43f, paint);
        paint.setShader(null);
    }

    private void drawAurora(Canvas canvas, int width, int height, float t) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new RadialGradient(width * 0.16f, height * 0.72f + (float) Math.sin(t) * dp(5),
                width * 0.54f,
                new int[] {Color.argb(228, 153, 236, 20), Color.argb(92, 228, 167, 31),
                        Color.TRANSPARENT},
                new float[] {0f, 0.36f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(width * 0.16f, height * 0.72f, width * 0.58f, paint);
        paint.setShader(null);

        paint.setColor(Color.argb(112, 255, 166, 50));
        path.reset();
        path.moveTo(0, height * 0.83f);
        path.cubicTo(width * 0.28f, height * 0.68f, width * 0.62f, height * 0.63f,
                width, height * 0.73f);
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawStars(Canvas canvas, int width, int height, float t) {
        paint.setShader(null);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < 130; i++) {
            float x = ((i * 61) % 1000) / 1000f * width;
            float y = ((i * 37) % 720) / 1000f * height;
            float twinkle = 0.45f + 0.55f * (float) Math.abs(Math.sin(t * 0.6f + i * 0.31f));
            paint.setColor(Color.argb((int) (44 + 142 * twinkle), 226, 234, 246));
            canvas.drawCircle(x, y, (i % 9 == 0) ? dp(1.25f) : dp(0.65f), paint);
        }
    }

    private void drawTree(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.rgb(7, 8, 8));
        float baseX = width * 0.48f;
        float baseY = height * 0.82f;

        paint.setStrokeWidth(dp(17));
        path.reset();
        path.moveTo(baseX, baseY);
        path.cubicTo(width * 0.43f, height * 0.64f, width * 0.48f, height * 0.52f,
                width * 0.39f, height * 0.42f);
        canvas.drawPath(path, paint);

        paint.setStrokeWidth(dp(9));
        drawBranch(canvas, baseX - dp(8), baseY - dp(92), width * 0.24f, height * 0.58f);
        drawBranch(canvas, baseX - dp(3), baseY - dp(128), width * 0.68f, height * 0.62f);
        drawBranch(canvas, width * 0.42f, height * 0.52f, width * 0.67f, height * 0.42f);
        drawBranch(canvas, width * 0.39f, height * 0.47f, width * 0.2f, height * 0.43f);
        drawBranch(canvas, width * 0.49f, height * 0.61f, width * 0.83f, height * 0.54f);

        paint.setStrokeWidth(dp(1.7f));
        paint.setColor(Color.argb(175, 203, 219, 225));
        for (int i = 0; i < 35; i++) {
            float bx = width * (0.18f + (i % 12) * 0.058f);
            float by = height * (0.41f + (i % 8) * 0.035f);
            canvas.drawLine(bx, by, bx + dp((i % 2 == 0) ? 28 : -22),
                    by - dp(18 + (i % 4) * 5), paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawBranch(Canvas canvas, float startX, float startY, float endX, float endY) {
        path.reset();
        path.moveTo(startX, startY);
        path.cubicTo((startX + endX) * 0.55f, startY - dp(35), endX - dp(16),
                endY + dp(16), endX, endY);
        canvas.drawPath(path, paint);
    }

    private void drawForeground(Canvas canvas, int width, int height) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(9, 11, 12));
        path.reset();
        path.moveTo(0, height * 0.81f);
        path.cubicTo(width * 0.22f, height * 0.77f, width * 0.37f, height * 0.86f,
                width * 0.56f, height * 0.78f);
        path.cubicTo(width * 0.76f, height * 0.69f, width * 0.88f, height * 0.82f,
                width, height * 0.73f);
        path.lineTo(width, height);
        path.lineTo(0, height);
        path.close();
        canvas.drawPath(path, paint);

        paint.setColor(Color.rgb(30, 31, 33));
        for (int i = 0; i < 10; i++) {
            float x = width * (0.03f + i * 0.105f);
            float y = height * (0.9f + (i % 3) * 0.018f);
            rect.set(x, y, x + dp(70 + (i % 3) * 18), y + dp(24 + (i % 2) * 12));
            canvas.drawOval(rect, paint);
        }
    }

    private void drawStatusBar(Canvas canvas) {
        float top = statusTop();
        paint.setColor(Color.WHITE);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(24));
        canvas.drawText(clockFormat.format(new Date()), dp(42), top + dp(27), paint);
        paint.setTypeface(Typeface.DEFAULT);

        float right = getWidth() - dp(38);
        drawBattery(canvas, right - dp(38), top + dp(8));
        drawWifi(canvas, right - dp(90), top + dp(8));
        drawSignal(canvas, right - dp(130), top + dp(10));
    }

    private void drawSignal(Canvas canvas, float x, float y) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        for (int i = 0; i < 4; i++) {
            rect.set(x + i * dp(7), y + dp(15 - i * 4), x + dp(5) + i * dp(7), y + dp(20));
            canvas.drawRoundRect(rect, dp(2), dp(2), paint);
        }
    }

    private void drawWifi(Canvas canvas, float x, float y) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(3));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.WHITE);
        rect.set(x, y + dp(2), x + dp(34), y + dp(34));
        canvas.drawArc(rect, 220, 100, false, paint);
        rect.set(x + dp(7), y + dp(10), x + dp(27), y + dp(30));
        canvas.drawArc(rect, 220, 100, false, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(x + dp(17), y + dp(25), dp(3), paint);
        paint.setStrokeCap(Paint.Cap.BUTT);
    }

    private void drawBattery(Canvas canvas, float x, float y) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        rect.set(x, y + dp(1), x + dp(39), y + dp(24));
        canvas.drawRoundRect(rect, dp(7), dp(7), paint);
        rect.set(x + dp(39), y + dp(8), x + dp(43), y + dp(17));
        canvas.drawRoundRect(rect, dp(2), dp(2), paint);
        paint.setColor(Color.rgb(19, 20, 24));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(13));
        canvas.drawText("100", x + dp(20), y + dp(17), paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.DEFAULT);
    }

    private void drawHomeIcons(Canvas canvas) {
        float safeTop = statusTop() + dp(76);
        float horizontal = dp(35);
        float gapX = (getWidth() - horizontal * 2f) / 5f;
        float rowGap = dp(100);
        float iconSize = Math.min(dp(64), gapX * 0.68f);
        int maxRows = 6;
        for (int i = 0; i < APP_LABELS.length; i++) {
            int row = i / 5;
            if (row >= maxRows) {
                break;
            }
            int col = i % 5;
            float centerX = horizontal + gapX * col + gapX * 0.5f;
            float y = safeTop + row * rowGap;
            if (y + iconSize + dp(35) > getHeight() - dockHeight() - dp(104)) {
                break;
            }
            drawHomeIcon(canvas, centerX, y, iconSize, i, APP_LABELS[i]);
        }
    }

    private void drawHomeIcon(Canvas canvas, float centerX, float y, float size, int index, String label) {
        float left = centerX - size * 0.5f;
        int[] colors = {
                Color.rgb(255, 197, 47), Color.rgb(26, 60, 73), Color.rgb(250, 250, 250),
                Color.rgb(225, 54, 43), Color.rgb(248, 248, 248), Color.rgb(3, 218, 238),
                Color.rgb(236, 238, 240), Color.rgb(22, 24, 28), Color.rgb(178, 174, 255),
                Color.rgb(255, 222, 30), Color.rgb(214, 220, 226), Color.rgb(9, 137, 232),
                Color.rgb(244, 245, 247), Color.rgb(66, 133, 244), Color.rgb(250, 250, 250),
                Color.rgb(33, 205, 211), Color.rgb(238, 116, 8), Color.rgb(255, 123, 43),
                Color.rgb(66, 133, 244), Color.rgb(31, 36, 42), Color.rgb(8, 132, 214),
                Color.rgb(0, 119, 255), Color.rgb(77, 102, 255), Color.rgb(116, 52, 222)
        };

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(72, 0, 0, 0));
        rect.set(left, y + dp(4), left + size, y + size + dp(8));
        canvas.drawRoundRect(rect, size * 0.24f, size * 0.24f, paint);

        paint.setColor(colors[index % colors.length]);
        rect.set(left, y, left + size, y + size);
        canvas.drawRoundRect(rect, size * 0.22f, size * 0.22f, paint);
        drawGlyph(canvas, left, y, size, index);

        if (index == 18 || index == 24) {
            paint.setColor(Color.rgb(15, 124, 95));
            canvas.drawCircle(left + size * 0.82f, y + size * 0.14f, size * 0.22f, paint);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(size * 0.2f);
            canvas.drawText(index == 18 ? "黃" : "鋒", left + size * 0.82f, y + size * 0.21f, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        }

        drawMultilineLabel(canvas, label, centerX, y + size + dp(18));
    }

    private void drawGlyph(Canvas canvas, float x, float y, float size, int index) {
        paint.setStyle(Paint.Style.FILL);
        switch (index % 10) {
            case 0:
                paint.setColor(Color.rgb(255, 222, 98));
                rect.set(x + size * 0.08f, y + size * 0.25f, x + size * 0.92f, y + size * 0.75f);
                canvas.drawRoundRect(rect, size * 0.08f, size * 0.08f, paint);
                paint.setColor(Color.rgb(255, 186, 47));
                rect.set(x + size * 0.12f, y + size * 0.18f, x + size * 0.5f, y + size * 0.38f);
                canvas.drawRoundRect(rect, size * 0.05f, size * 0.05f, paint);
                break;
            case 1:
                paint.setColor(Color.WHITE);
                path.reset();
                path.moveTo(x + size * 0.32f, y + size * 0.14f);
                path.lineTo(x + size * 0.64f, y + size * 0.34f);
                path.lineTo(x + size * 0.38f, y + size * 0.72f);
                path.lineTo(x + size * 0.2f, y + size * 0.54f);
                path.lineTo(x + size * 0.46f, y + size * 0.32f);
                path.close();
                canvas.drawPath(path, paint);
                paint.setColor(Color.rgb(74, 219, 202));
                path.offset(size * 0.18f, size * 0.12f);
                canvas.drawPath(path, paint);
                break;
            case 2:
                paint.setColor(Color.rgb(12, 12, 14));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(size * 0.09f);
                canvas.drawLine(x + size * 0.22f, y + size * 0.32f, x + size * 0.78f, y + size * 0.68f, paint);
                canvas.drawLine(x + size * 0.78f, y + size * 0.32f, x + size * 0.22f, y + size * 0.68f, paint);
                paint.setStyle(Paint.Style.FILL);
                break;
            case 3:
                paint.setColor(Color.rgb(255, 65, 45));
                path.reset();
                path.moveTo(x + size * 0.3f, y + size * 0.22f);
                path.lineTo(x + size * 0.76f, y + size * 0.5f);
                path.lineTo(x + size * 0.3f, y + size * 0.78f);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case 4:
                paint.setShader(new LinearGradient(x, y, x + size, y + size,
                        Color.rgb(247, 62, 68), Color.rgb(49, 123, 240), Shader.TileMode.CLAMP));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(size * 0.1f);
                rect.set(x + size * 0.25f, y + size * 0.2f, x + size * 0.75f, y + size * 0.82f);
                canvas.drawArc(rect, 200, 280, false, paint);
                paint.setShader(null);
                paint.setStyle(Paint.Style.FILL);
                break;
            case 5:
                paint.setColor(Color.rgb(0, 235, 255));
                rect.set(x + size * 0.15f, y + size * 0.22f, x + size * 0.7f, y + size * 0.66f);
                canvas.drawRoundRect(rect, size * 0.04f, size * 0.04f, paint);
                paint.setColor(Color.rgb(0, 0, 0));
                path.reset();
                path.moveTo(x + size * 0.32f, y + size * 0.3f);
                path.lineTo(x + size * 0.62f, y + size * 0.48f);
                path.lineTo(x + size * 0.32f, y + size * 0.64f);
                path.close();
                canvas.drawPath(path, paint);
                break;
            case 6:
                paint.setColor(Color.rgb(32, 34, 38));
                canvas.drawCircle(x + size * 0.42f, y + size * 0.46f, size * 0.11f, paint);
                canvas.drawCircle(x + size * 0.65f, y + size * 0.42f, size * 0.11f, paint);
                break;
            case 7:
                paint.setColor(Color.rgb(26, 234, 127));
                rect.set(x + size * 0.18f, y + size * 0.25f, x + size * 0.82f, y + size * 0.68f);
                canvas.drawRect(rect, paint);
                paint.setColor(Color.rgb(21, 25, 27));
                rect.set(x + size * 0.3f, y + size * 0.35f, x + size * 0.7f, y + size * 0.58f);
                canvas.drawRect(rect, paint);
                break;
            case 8:
                paint.setColor(Color.rgb(12, 13, 16));
                canvas.drawCircle(x + size * 0.34f, y + size * 0.48f, size * 0.17f, paint);
                canvas.drawCircle(x + size * 0.65f, y + size * 0.48f, size * 0.17f, paint);
                paint.setColor(Color.WHITE);
                canvas.drawCircle(x + size * 0.38f, y + size * 0.44f, size * 0.06f, paint);
                canvas.drawCircle(x + size * 0.61f, y + size * 0.44f, size * 0.06f, paint);
                break;
            default:
                paint.setColor(Color.WHITE);
                path.reset();
                path.moveTo(x + size * 0.36f, y + size * 0.25f);
                path.lineTo(x + size * 0.72f, y + size * 0.5f);
                path.lineTo(x + size * 0.36f, y + size * 0.75f);
                path.close();
                canvas.drawPath(path, paint);
                break;
        }
    }

    private void drawMultilineLabel(Canvas canvas, String label, float centerX, float y) {
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(12));
        paint.setShadowLayer(dp(3), 0, dp(1), Color.argb(180, 0, 0, 0));
        String[] lines = label.split("\\n");
        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], centerX, y + i * dp(15), paint);
        }
        paint.clearShadowLayer();
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawCompanion(Canvas canvas, float t) {
        float radius = companionRadius();
        float bob = (float) Math.sin(t * (energy < 30 ? 2.0f : 3.1f)) * dp(5);
        float x = petX;
        float y = petY + bob;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(122, 0, 0, 0));
        rect.set(x - radius * 0.68f, y + radius * 0.36f, x + radius * 0.68f, y + radius * 0.52f);
        canvas.drawOval(rect, paint);

        Bitmap bitmap = currentPetBitmap();
        if (humanMode && bitmap != null) {
            drawPetBitmap(canvas, bitmap, x, y, radius);
            return;
        }

        if (humanMode) {
            drawPenguinHuman(canvas, x, y, radius);
        } else {
            drawPenguin(canvas, x, y, radius);
        }
    }

    private Bitmap loadBitmap(String name) {
        int id = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
        return id == 0 ? null : BitmapFactory.decodeResource(getResources(), id);
    }

    private Bitmap currentPetBitmap() {
        if (energy < 34 && petSleepBitmap != null) {
            return petSleepBitmap;
        }
        if (happiness > 78 && petHappyBitmap != null) {
            return petHappyBitmap;
        }
        return petIdleBitmap;
    }

    private void drawPetBitmap(Canvas canvas, Bitmap bitmap, float x, float y, float radius) {
        float targetHeight = radius * 2.45f;
        float targetWidth = targetHeight * bitmap.getWidth() / bitmap.getHeight();
        float left = x - targetWidth * 0.5f;
        float top = y - targetHeight * 0.96f;
        rect.set(left, top, left + targetWidth, top + targetHeight);
        paint.setAlpha(255);
        canvas.drawBitmap(bitmap, null, rect, paint);
    }

    private void drawPenguinHuman(Canvas canvas, float x, float y, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(30, 31, 36));
        rect.set(x - radius * 0.72f, y - radius * 1.28f, x + radius * 0.72f, y + radius * 0.38f);
        canvas.drawRoundRect(rect, radius * 0.55f, radius * 0.55f, paint);
        paint.setColor(Color.rgb(246, 246, 239));
        rect.set(x - radius * 0.43f, y - radius * 0.45f, x + radius * 0.43f, y + radius * 0.24f);
        canvas.drawOval(rect, paint);
        paint.setColor(Color.rgb(243, 207, 179));
        rect.set(x - radius * 0.52f, y - radius * 1.02f, x + radius * 0.52f, y - radius * 0.18f);
        canvas.drawOval(rect, paint);
        paint.setColor(Color.rgb(24, 24, 28));
        rect.set(x - radius * 0.55f, y - radius * 1.07f, x + radius * 0.55f, y - radius * 0.45f);
        canvas.drawArc(rect, 185, 170, true, paint);
        paint.setColor(Color.rgb(249, 190, 44));
        path.reset();
        path.moveTo(x - radius * 0.12f, y - radius * 1.18f);
        path.lineTo(x + radius * 0.15f, y - radius * 1.05f);
        path.lineTo(x - radius * 0.08f, y - radius * 0.9f);
        path.close();
        canvas.drawPath(path, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x - radius * 0.32f, y - radius * 1.14f, radius * 0.09f, paint);
        canvas.drawCircle(x + radius * 0.32f, y - radius * 1.14f, radius * 0.09f, paint);
        paint.setColor(Color.rgb(30, 31, 36));
        canvas.drawCircle(x - radius * 0.32f, y - radius * 1.14f, radius * 0.045f, paint);
        canvas.drawCircle(x + radius * 0.32f, y - radius * 1.14f, radius * 0.045f, paint);
        drawAnimeFace(canvas, x, y - radius * 0.62f, radius);
        drawFlippersAndFeet(canvas, x, y, radius);
    }

    private void drawPenguin(Canvas canvas, float x, float y, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(29, 31, 36));
        rect.set(x - radius * 0.66f, y - radius * 1.08f, x + radius * 0.66f, y + radius * 0.38f);
        canvas.drawOval(rect, paint);
        paint.setColor(Color.rgb(246, 246, 239));
        rect.set(x - radius * 0.42f, y - radius * 0.55f, x + radius * 0.42f, y + radius * 0.24f);
        canvas.drawOval(rect, paint);
        drawAnimeFace(canvas, x, y - radius * 0.55f, radius * 0.92f);
        paint.setColor(Color.rgb(249, 190, 44));
        path.reset();
        path.moveTo(x - radius * 0.13f, y - radius * 0.78f);
        path.lineTo(x + radius * 0.16f, y - radius * 0.7f);
        path.lineTo(x - radius * 0.13f, y - radius * 0.62f);
        path.close();
        canvas.drawPath(path, paint);
        drawFlippersAndFeet(canvas, x, y, radius);
    }

    private void drawAnimeFace(Canvas canvas, float x, float y, float radius) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(35, 39, 46));
        canvas.drawCircle(x - radius * 0.23f, y, radius * 0.065f, paint);
        canvas.drawCircle(x + radius * 0.23f, y, radius * 0.065f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x - radius * 0.25f, y - radius * 0.025f, radius * 0.022f, paint);
        canvas.drawCircle(x + radius * 0.21f, y - radius * 0.025f, radius * 0.022f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(dp(2.2f));
        paint.setColor(Color.rgb(136, 65, 63));
        float mouthHeight = happiness > 72 ? radius * 0.16f : radius * 0.07f;
        rect.set(x - radius * 0.13f, y + radius * 0.08f - mouthHeight,
                x + radius * 0.13f, y + radius * 0.14f + mouthHeight);
        canvas.drawArc(rect, 20, 140, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setColor(Color.argb(130, 255, 126, 133));
        canvas.drawCircle(x - radius * 0.39f, y + radius * 0.06f, radius * 0.055f, paint);
        canvas.drawCircle(x + radius * 0.39f, y + radius * 0.06f, radius * 0.055f, paint);
    }

    private void drawFlippersAndFeet(Canvas canvas, float x, float y, float radius) {
        paint.setColor(Color.rgb(23, 24, 29));
        path.reset();
        path.moveTo(x - radius * 0.58f, y - radius * 0.42f);
        path.quadTo(x - radius * 1.08f, y - radius * 0.08f, x - radius * 0.74f, y + radius * 0.12f);
        path.quadTo(x - radius * 0.52f, y - radius * 0.02f, x - radius * 0.58f, y - radius * 0.42f);
        canvas.drawPath(path, paint);
        path.reset();
        path.moveTo(x + radius * 0.58f, y - radius * 0.42f);
        path.quadTo(x + radius * 1.08f, y - radius * 0.08f, x + radius * 0.74f, y + radius * 0.12f);
        path.quadTo(x + radius * 0.52f, y - radius * 0.02f, x + radius * 0.58f, y - radius * 0.42f);
        canvas.drawPath(path, paint);
        paint.setColor(Color.rgb(236, 158, 40));
        rect.set(x - radius * 0.46f, y + radius * 0.22f, x - radius * 0.1f, y + radius * 0.42f);
        canvas.drawOval(rect, paint);
        rect.set(x + radius * 0.1f, y + radius * 0.22f, x + radius * 0.46f, y + radius * 0.42f);
        canvas.drawOval(rect, paint);
    }

    private void drawBubble(Canvas canvas) {
        float radius = companionRadius();
        paint.setTextSize(dp(14));
        float bubbleWidth = Math.min(dp(238), Math.max(dp(132), paint.measureText(bubble) + dp(34)));
        float left = clamp(petX - bubbleWidth * 0.68f, dp(10), getWidth() - bubbleWidth - dp(10));
        float top = clamp(petY - radius * 1.96f, statusTop() + dp(50), getHeight() - dockHeight() - dp(116));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(238, 255, 255, 255));
        rect.set(left, top, left + bubbleWidth, top + dp(40));
        canvas.drawRoundRect(rect, dp(17), dp(17), paint);
        paint.setColor(Color.rgb(28, 31, 36));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(bubble, left + bubbleWidth * 0.5f, top + dp(26), paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawPageDots(Canvas canvas) {
        float y = getHeight() - dockHeight() - dp(27);
        float cx = getWidth() * 0.5f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 255, 255, 255));
        canvas.drawCircle(cx - dp(22), y, dp(5), paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, y, dp(6), paint);
        paint.setColor(Color.argb(90, 255, 255, 255));
        canvas.drawCircle(cx + dp(22), y, dp(5), paint);
    }

    private void drawDock(Canvas canvas) {
        float margin = dp(18);
        float top = getHeight() - dockHeight() + dp(13);
        float bottom = getHeight() - dp(17);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0, top, 0, bottom,
                Color.argb(218, 74, 75, 76), Color.argb(218, 35, 36, 38), Shader.TileMode.CLAMP));
        rect.set(margin, top, getWidth() - margin, bottom);
        canvas.drawRoundRect(rect, dp(34), dp(34), paint);
        paint.setShader(null);

        String[] labels = {"電話", "訊息", "相機", "設定"};
        for (int i = 0; i < 4; i++) {
            float center = dockItemCenter(i);
            drawDockIcon(canvas, center, top + dp(27), i, labels[i]);
        }
    }

    private void drawDockIcon(Canvas canvas, float centerX, float y, int index, String label) {
        float size = dp(58);
        float left = centerX - size * 0.5f;
        int color = index < 2 ? Color.rgb(49, 232, 82) : Color.rgb(238, 239, 241);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        rect.set(left, y, left + size, y + size);
        canvas.drawRoundRect(rect, size * 0.23f, size * 0.23f, paint);

        paint.setColor(index < 2 ? Color.WHITE : Color.rgb(43, 45, 49));
        if (index == 0) {
            paint.setStrokeWidth(dp(8));
            paint.setStrokeCap(Paint.Cap.ROUND);
            canvas.drawLine(left + size * 0.33f, y + size * 0.28f, left + size * 0.67f, y + size * 0.72f, paint);
            paint.setStrokeCap(Paint.Cap.BUTT);
        } else if (index == 1) {
            canvas.drawOval(left + size * 0.2f, y + size * 0.22f, left + size * 0.8f, y + size * 0.72f, paint);
            paint.setColor(color);
            path.reset();
            path.moveTo(left + size * 0.38f, y + size * 0.66f);
            path.lineTo(left + size * 0.25f, y + size * 0.84f);
            path.lineTo(left + size * 0.54f, y + size * 0.68f);
            path.close();
            canvas.drawPath(path, paint);
        } else if (index == 2) {
            rect.set(left + size * 0.2f, y + size * 0.3f, left + size * 0.8f, y + size * 0.72f);
            canvas.drawRoundRect(rect, size * 0.08f, size * 0.08f, paint);
            paint.setColor(Color.rgb(238, 239, 241));
            canvas.drawCircle(left + size * 0.5f, y + size * 0.51f, size * 0.15f, paint);
            paint.setColor(Color.rgb(43, 45, 49));
            canvas.drawCircle(left + size * 0.5f, y + size * 0.51f, size * 0.11f, paint);
        } else {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(dp(4));
            canvas.drawCircle(left + size * 0.5f, y + size * 0.5f, size * 0.22f, paint);
            for (int i = 0; i < 8; i++) {
                double a = Math.PI * 2 * i / 8.0;
                float sx = left + size * 0.5f + (float) Math.cos(a) * size * 0.29f;
                float sy = y + size * 0.5f + (float) Math.sin(a) * size * 0.29f;
                float ex = left + size * 0.5f + (float) Math.cos(a) * size * 0.38f;
                float ey = y + size * 0.5f + (float) Math.sin(a) * size * 0.38f;
                canvas.drawLine(sx, sy, ex, ey, paint);
            }
            paint.setStyle(Paint.Style.FILL);
        }

        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(dp(12));
        paint.setShadowLayer(dp(3), 0, dp(1), Color.argb(160, 0, 0, 0));
        canvas.drawText(label, centerX, y + size + dp(19), paint);
        paint.clearShadowLayer();
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private int dockItemAt(float x, float y) {
        float top = getHeight() - dockHeight() + dp(13);
        if (y < top || y > getHeight() - dp(17)) {
            return -1;
        }
        for (int i = 0; i < 4; i++) {
            float center = dockItemCenter(i);
            if (Math.abs(x - center) <= dp(38)) {
                return i;
            }
        }
        return -1;
    }

    private float dockItemCenter(int index) {
        float left = dp(50);
        float right = getWidth() - dp(50);
        return left + (right - left) * (index + 0.5f) / 4f;
    }

    private void updateStats(int happinessDelta, int energyDelta, int fullnessDelta) {
        happiness = clampInt(happiness + happinessDelta);
        energy = clampInt(energy + energyDelta);
        fullness = clampInt(fullness + fullnessDelta);
        invalidate();
    }

    private float statusTop() {
        return dp(22);
    }

    private float dockHeight() {
        return dp(132);
    }

    private float companionRadius() {
        return Math.min(getWidth(), getHeight()) * 0.155f;
    }

    private float distance(float ax, float ay, float bx, float by) {
        float dx = ax - bx;
        float dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clampInt(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
