package com.adurcup.rainbowshots;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Locale;
import java.util.Random;

import static android.support.v4.content.ContextCompat.getColor;
import static com.adurcup.rainbowshots.R.color.colorYellowDrop;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread = null;

    private SurfaceHolder ourHolder;

    private volatile boolean playing;

    private boolean paused = true;

    private Paint paint;

    private long fps;
    private long score = 0;
    private long bestScore = 0;

    private int screenX;

    private int screenY;

    private Button[] buttons = new Button[4];

    private Drop[][] drops;
    // Maximum drops would be 3, one more than value assigned
    // as 0 included
    private int maxDrops = 3;


    int[] nextDrop = new int[4], activeDropsCount = new int[4],
            previousDrop = new int[4], bottomDrop = new int[4];
    int randomGap = 2, randColor = 0;
    int randomCloudGap[] = new int[10];
    boolean gameOver = false;

    private Boolean levelUp = true;
    private int level = 1;
    private Integer cutOffLevel;

    private boolean[] variable = {true, false, true, false, true, false, false, true, false, false};

    private long maxTime = 0;
    private SharedPreferences sp;
    private final String KEY_BEST = "kb";

    public GameView (Context context) {
        super(context);

        sp = context.getSharedPreferences(BuildConfig.APPLICATION_ID, 0);

        Display display = ((GameActivity)context).getWindowManager()
                .getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        ourHolder = getHolder();
        paint = new Paint();
        Typeface tf =Typeface.createFromAsset(context.getAssets(),"fonts/arial_black.ttf");
        paint.setTypeface(tf);


        screenX = size.x;
        screenY = size.y;

        prepareLevel();
    }

    private void prepareLevel() {

        if (level == 1) {
            bestScore = sp.getLong(KEY_BEST, 0);
            score = 0;
        }

        maxTime = (120 * 1000) + maxTime - totalTimeThisLevel;
        totalTimeThisLevel = 0;
        if (cutOffLevel == null || cutOffLevel < 2 * screenY/3) {
            cutOffLevel = screenY / 6 + (level * screenY / 18);
        }

        for (int i = 0; i < 4; i++) {
            buttons[i] = new Button(screenX, screenY, i);
            nextDrop[i] = 0;
            activeDropsCount[i] =  0;
            previousDrop[i] = 0;
            bottomDrop[i] = 0;
        }

        randomCloudGap[0] = screenX / 8;
        randomCloudGap[1] = screenX / 4 - ((screenX / 15) - 13);
        randomCloudGap[2] = (screenX / 15) - 13;
        randomCloudGap[3] = (screenX / 15) - 30;
        randomCloudGap[4] = screenX / 15 - 20;
        randomCloudGap[5] = screenX / 9;
        randomCloudGap[6] = screenX / 10;
        randomCloudGap[7] = screenX / 12;
        randomCloudGap[8] = screenX / 13;
        randomCloudGap[9] = screenX / 15 - 25;

        if (drops == null) {
            drops = new Drop[4][4];
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                drops[i][j] = new Drop(screenX, i);
            }
        }
    }

    long totalTimeThisLevel = 0;
    @Override
    public void run() {
        while (playing) {

            // Capture the current time in milliseconds in startFrameTime
            long startFrameTime = System.currentTimeMillis();

            // Update the frame
            // Update the frame
            if (!paused) {
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.

            long timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            } else fps = 35;
            if (!paused) {
                totalTimeThisLevel += timeThisFrame;
                if (totalTimeThisLevel >= maxTime) {
                    gameOver = true;
                    paused = true;
                    if (bestScore < score) {
                        SharedPreferences.Editor spEditor = sp.edit();
                        spEditor.putLong(KEY_BEST, score);
                        spEditor.apply();
                    }
                }
            }
        }
    }

    public void update() {
        // updating all drops position according to frame
        for (int i = 0; i < 4; i++) {
            for (Drop drop : drops[i]) {
                if (drop.getStatus()) {
                    drop.update(fps);
                }
            }
        }

        Random generator = new Random();

        // fetching previous drop
        for (int i = 0; i < 4; i++) {
            if (nextDrop[i] == 0) {
                previousDrop[i] = maxDrops;
            } else {
                previousDrop[i] = nextDrop[i] - 1;
            }
        }

        for (int i = 0; i < 10; i ++) {
            int gap = generator.nextInt(screenX / 400);
            if (variable[i]) {
                if (i != 0)
                    randomCloudGap[i] = randomCloudGap[i] + gap;
                else
                    randomCloudGap[i] = randomCloudGap[i] + gap;
                if (randomCloudGap[i] > screenX / 4 - ((screenX / 15) - 13)) {
                    variable[i] = false;
                    randomCloudGap[i] = randomCloudGap[i] - gap;
                }
            } else {
                if (i != 0)
                    randomCloudGap[i] = randomCloudGap[i] - gap;
                else
                    randomCloudGap[i] = randomCloudGap[i] - gap;
                if (randomCloudGap[i] < ((screenX / 15) - 13)) {
                    variable[i] = true;
                    randomCloudGap[i] = randomCloudGap[i] + gap;
                }
            }
        }

        // generate or position next drop from the beginning screen
        // if first Run or previous drop reach the previous value of
        // value of previous drop reach previous value of randomGap

        for (int i = 0; i < 4; i++) {


            if (activeDropsCount[i] == 0 ||
                    (drops[i][previousDrop[i]].getImpactPointY() > randomGap)) {
                if (drops[i][nextDrop[i]].shoot(drops[0][0].DOWN, randColor, level)) {
                    activeDropsCount[i]++;
                    randColor = generator.nextInt(4);
                    // randomGap between consecutive drops
                    // vary from screen / 4 to screenY / 5
                    randomGap = generator.nextInt((screenY / 5)) + screenY / 4;
                    nextDrop[i]++;
                    // Maximum of 3 drops would be created in any frame
                    if (nextDrop[i] > maxDrops) {
                        nextDrop[i] = 0;
                    }
                }
            }
        }

        // inactivate drop when it hit button
        for (int i = 0; i < 4; i++) {
            for (Drop drop : drops[i]) {
                if (drop.getStatus() &&
                        (drop.getImpactPointY() > screenY - buttons[i].getButtonHeight())) {
                    if (drop.getColorState() == i) {
                        dropBurst(false);
                        buttons[i].isTopThresholdReached(screenY, cutOffLevel);
                    } else {
                        dropBurst(true);
                        buttons[i].isBottomThresholdReached(screenY, screenX);
                    }
                    drop.setInactive();
                    activeDropsCount[i]--;
                    if (bottomDrop[i] == maxDrops) {
                        bottomDrop[i] = 0;
                    } else {
                        bottomDrop[i]++;
                    }
                }
            }
        }

        if (buttons[0].getButtonHeight() >= cutOffLevel
                && buttons[1].getButtonHeight() >= cutOffLevel
                && buttons[2].getButtonHeight() >= cutOffLevel
                && buttons[3].getButtonHeight() >= cutOffLevel) {
            paused = true;
            levelUp = true;
            level++;
            score += level * score;
            prepareLevel();
        }
    }

    // Draw the newly updated scene
    public void draw() {

        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw

            Canvas canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(Color.WHITE);



            // Drawing drops color
            for (int i = 0; i < 4; i++) {
                for (Drop drop : drops[i]) {
                    if (drop.getStatus()) {
                        paint.setColor(
                                getColor(getContext(), drop.getColor()));

                        canvas.drawCircle(drop.getRecF().centerX(), drop.getRecF().centerY(),
                                screenX / 18, paint);

                        paint.setColor(Color.WHITE);
                        if (drop.getNotActiveState() >= 0) {
                            canvas.drawCircle(drop.getRecF().centerX(), drop.getRecF().centerY(),
                                    (9 - drop.getNotActiveState()) *(screenX / 162) ,
                                    paint);
                        }
                    }
                }
            }

            // Drawing buttons
            paint.setColor(getColor(getContext(), colorYellowDrop));

            canvas.drawRect(buttons[0].getRect(), paint);
            canvas.drawCircle(screenX / 4 - ((screenX / 15 ) - 13),
                    screenY + 13 - buttons[0].getButtonHeight(), (screenX / 15 ) - 13, paint);
            canvas.drawCircle((screenX / 15 ) - 13,
                    screenY + 26 - buttons[0].getButtonHeight(), (screenX / 15 ) - 13, paint);
            for (int i = 0; i < 9; i++) {
                canvas.drawCircle(randomCloudGap[i],
                        screenY - buttons[0].getButtonHeight(),
                        (screenX / (15 + i)) - 13, paint);
            }

            paint.setColor(getColor(getContext(), R.color.colorBlueDrop));

            canvas.drawRect(buttons[1].getRect(), paint);
            canvas.drawCircle(screenX / 2 - ((screenX / 15 ) - 13),
                    screenY + 20 - buttons[1].getButtonHeight(), (screenX / 15 ) - 13, paint);
            canvas.drawCircle(screenX/ 4 + (screenX / 15 ) - 13,
                    screenY + 10- buttons[1].getButtonHeight(), (screenX / 15 ) - 13, paint);
            for (int i = 0; i < 9; i++) {
                canvas.drawCircle(screenX / 4 + randomCloudGap[(i + 3) % 10],
                        screenY - buttons[1].getButtonHeight(),
                        (screenX / (15 + i)) - 13, paint);
            }

            paint.setColor(getColor(getContext(), R.color.colorGreenDrop));

            canvas.drawRect(buttons[2].getRect(), paint);
            canvas.drawCircle(3 * screenX / 4 - ((screenX / 15 ) - 13),
                    screenY + 5 - buttons[2].getButtonHeight(), (screenX / 15 ) - 13, paint);
            canvas.drawCircle(screenX / 2 + (screenX / 15 ) - 13,
                    screenY + 25- buttons[2].getButtonHeight(), (screenX / 15 ) - 13, paint);
            for (int i = 0; i < 9; i++) {
                canvas.drawCircle(screenX / 2 + randomCloudGap[(i + 7) % 10],
                        screenY - buttons[2].getButtonHeight(),
                        (screenX / (15 + i)) - 13, paint);
            }

            paint.setColor(getColor(getContext(), R.color.colorPinkDrop));

            canvas.drawRect(buttons[3].getRect(), paint);
            canvas.drawCircle(screenX  - ((screenX / 15 ) - 13),
                    screenY + 10 - buttons[3].getButtonHeight(), (screenX / 15 ) - 13, paint);
            canvas.drawCircle(3 * screenX / 4 + (screenX / 15 ) - 13,
                    screenY + 20- buttons[3].getButtonHeight(), (screenX / 15 ) - 13, paint);
            for (int i = 0; i < 9; i++) {
                canvas.drawCircle(3 * screenX / 4 + randomCloudGap[(i + 5) % 10],
                        screenY - buttons[3].getButtonHeight(),
                        (screenX / (15 + i)) - 13, paint);
            }

            //background separating lines
            paint.setColor(Color.GRAY);
//            paint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

            canvas.drawLine((screenX / 4) + 1, 0, (screenX / 4) + 1, screenY, paint);
            canvas.drawLine((2 * screenX / 4) + 1, 0, (2 * screenX / 4) + 1, screenY, paint);
            canvas.drawLine((3 * screenX / 4) + 1, 0, (3 * screenX / 4) + 1, screenY, paint);

            paint.setColor(getColor(getContext(), R.color.colorPinkDrop));
            paint.setStrokeWidth(1.4f);

            int winY = (screenY - cutOffLevel - (screenX / 20));

            canvas.drawLine(0, winY, screenX, winY, paint);

            Point a = new Point(0, winY - 20);
            Point b = new Point(0, winY + 20);
            Point c = new Point(38, winY);

            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            path.lineTo(b.x, b.y);
            path.lineTo(c.x, c.y);
            path.lineTo(a.x, a.y);
            path.close();

            canvas.drawPath(path, paint);

            a = new Point(screenX, winY - 20);
            b = new Point(screenX, winY+ 20);
            c = new Point(screenX - 38, winY);

            path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);

            path.moveTo(a.x, a.y);
            path.lineTo(b.x, b.y);
            path.lineTo(c.x, c.y);
            path.lineTo(a.x, a.y);
            path.close();

            canvas.drawPath(path, paint);

            paint.setTextSize(screenX / 35);
            canvas.drawText("WIN THE SHOTS",
                    23 * screenX / 60, winY , paint);

            //Outer most white circle
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 8) - 13, paint);

            //inner colored circle to show as a fine ring
            paint.setColor(getColor(getContext(), colorYellowDrop));
            canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 8) - 17, paint);
            paint.setColor(getColor(getContext(), R.color.colorBlueDrop));
            canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 8) - 17, paint);
            paint.setColor(getColor(getContext(), R.color.colorGreenDrop));
            canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 8) - 17, paint);
            paint.setColor(getColor(getContext(), R.color.colorPinkDrop));
            canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 8) - 17, paint);

            //inner thicker white dome
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 16), paint);
            canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 16), paint);
            canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 16), paint);
            canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 16), paint);

            //inner colored to make a hole in the inner ring
            if (!buttonPressed[0]) {
                paint.setColor(getColor(getContext(), colorYellowDrop));
                canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 24), paint);
            }
            if (!buttonPressed[1]) {
                paint.setColor(getColor(getContext(), R.color.colorBlueDrop));
                canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 24), paint);
            }
            if (!buttonPressed[2]) {
                paint.setColor(getColor(getContext(), R.color.colorGreenDrop));
                canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 24), paint);
            }
            if (!buttonPressed[3]) {
                paint.setColor(getColor(getContext(), R.color.colorPinkDrop));
                canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 24), paint);
            }

            paint.setColor(getColor(getContext(), R.color.colorGreenDrop));
            paint.setTextSize(screenX / 18);

            canvas.drawText("Level:" + level, screenX / 40, screenY / 20, paint);

            paint.setColor(getColor(getContext(), R.color.colorBlueDrop));
            if ((int)(totalTimeThisLevel / 1000) < (int)(maxTime / 1000)) {
                canvas.drawText(secToTime((maxTime - totalTimeThisLevel) / 1000),
                        screenX / 2 - screenX / 12, screenY / 13, paint);
            } else {
                canvas.drawText("Time Up", 3 * screenX / 8, screenY / 13, paint);
                canvas.drawText("Game Over!", screenX / 3, screenY / 3, paint);
            }

            paint.setColor(getColor(getContext(), R.color.colorPinkDrop));
            paint.setTextSize(screenX / 20);

            canvas.drawText(String.format(Locale.ENGLISH, "Score:%d", (int) score),
                    screenX - ((5 + noOfDigit(score)) * screenX / 25), screenY / 20, paint);

            paint.setTextSize(screenX / 30);
            if (score > bestScore) {
                canvas.drawText(String.format(Locale.ENGLISH, "Best:%d", (int) score),
                        screenX - ((5 + noOfDigit(score)) * screenX / 40), screenY / 15, paint);
            } else {
                canvas.drawText(String.format(Locale.ENGLISH, "Best:%d", (int) bestScore),
                        screenX - ((5 + noOfDigit(bestScore)) * screenX / 40), screenY / 15, paint);
            }

            paint.setColor(getColor(getContext(), R.color.colorYellowDrop));
            paint.setTextSize(screenX/10);
            if (paused) {
                canvas.drawText("▶", 36 * screenX / 80, screenY / 20, paint);
                paint.setTextSize(screenX / 9);
                if (levelUp || gameOver)
                    canvas.drawText("Start", screenX / 2 - (screenX / 8), screenY / 2, paint);
                else {
                    canvas.drawText("Paused", screenX / 2 - (10 * screenX / 45), screenY / 2, paint);
                }
            }
            else {
                canvas.drawText("\u23F8", 141 * screenX / 320, screenY / 20, paint);
            }


            if (levelUp && level != 1) {
                paint.setTextSize(screenX / 15);
                canvas.drawText("Level Cleared!", screenX / 4, screenY / 3, paint);
                paint.setColor(getColor(getContext(), R.color.colorBlueDrop));
                canvas.drawText("Bonus +" +
                        secToTime((maxTime / 1000) - 120),
                        screenX / 4, 5 * screenY / 12, paint);
            }
            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }


    public void pause() {
        playing = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            Log.e("Error:", "joining thread");
        }
    }

    public void resume() {
        playing = true;
        paused = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    boolean buttonPressed[] = {false, false, false, false, false};

    private void dropBurst(boolean wrongDropBurst) {
        if (wrongDropBurst) {
            if (score - (level * 3) < 0) {
                score = 0;
            } else {
                score -= (level * 3);
            }
        } else {
            score += level * 2;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:

                if (!gameOver) {
                    levelUp = false;
                    float x, y;

                    x = motionEvent.getX();
                    y = motionEvent.getY();

                    paused = y < screenY / 20 && !paused;

                    if (activeDropsCount[0] > 0 && x < screenX / 4) {
                        if (y > screenY - buttons[0].getButtonHeight()) {

                            if (!paused) {
                                buttonPressed[0] = true;
                                dropBurst(drops[0][bottomDrop[0]].getColorState() == 0);
                                drops[0][bottomDrop[0]].setInactive();
                                activeDropsCount[0]--;
                                if (bottomDrop[0] == maxDrops) {
                                    bottomDrop[0] = 0;
                                } else {
                                    bottomDrop[0]++;
                                }
                            }
                        }
                    } else if (activeDropsCount[1] > 0 && x < screenX / 2) {
                        if (y > screenY - buttons[1].getButtonHeight()) {
                            if (!paused) {
                                buttonPressed[1] = true;
                                dropBurst(drops[1][bottomDrop[1]].getColorState() == 1);
                                drops[1][bottomDrop[1]].setInactive();
                                activeDropsCount[1]--;
                                if (bottomDrop[1] == maxDrops) {
                                    bottomDrop[1] = 0;
                                } else {
                                    bottomDrop[1]++;
                                }
                            }
                        }
                    } else if (activeDropsCount[2] > 0 && x < (3 * screenX / 4)) {
                        if (y > screenY - buttons[2].getButtonHeight()) {
                            if (!paused) {
                                buttonPressed[2] = true;
                                dropBurst(drops[2][bottomDrop[2]].getColorState() == 2);
                                drops[2][bottomDrop[2]].setInactive();
                                activeDropsCount[2]--;
                                if (bottomDrop[2] == maxDrops) {
                                    bottomDrop[2] = 0;
                                } else {
                                    bottomDrop[2]++;
                                }
                            }
                        }
                    } else {
                        if (activeDropsCount[3] > 0 && y > screenY - buttons[3].getButtonHeight()) {
                            if (!paused) {
                                buttonPressed[3] = true;
                                dropBurst(drops[3][bottomDrop[3]].getColorState() == 3);
                                drops[3][bottomDrop[3]].setInactive();
                                activeDropsCount[3]--;
                                if (bottomDrop[3] == maxDrops) {
                                    bottomDrop[3] = 0;
                                } else {
                                    bottomDrop[3]++;
                                }
                            }
                        }
                    }
                } else {
                    paused = false;
                    gameOver = false;
                    maxTime = 0;
                    level = 1;
                    prepareLevel();
                }
                break;
            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                if (!gameOver) {
                    buttonPressed[0] = false;
                    buttonPressed[1] = false;
                    buttonPressed[2] = false;
                    buttonPressed[3] = false;
                } else {
                    paused = false;
                    gameOver = false;
                    maxTime = 0;
                    level = 1;
                    prepareLevel();
                }
                // paused = true;
                break;

        }
        return true;
    }

    private int noOfDigit(long number) {
        if (number == 0) {
            return 1;
        }
        int i = 0;
        while (number != 0) {
            number /= 10;
            i++;
        }
        return i;
    }

    private String secToTime(long sec) {
        long hr = sec / 3600;
        String stHr = (Long.toString(hr + 100)).substring(1);
        sec = sec % 3600;
        long min = sec / 60;
        String stMin = (Long.toString(min + 100)).substring(1);
        sec = sec % 60;
        String stSec = (Long.toString(sec + 100)).substring(1);
        if (hr == 0) {
            return (stMin + ":" + stSec);
        }
        return (stHr + ":" + stMin + ":" + stSec);
    }
}
