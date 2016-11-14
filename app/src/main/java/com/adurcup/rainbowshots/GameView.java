package com.adurcup.rainbowshots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

import static com.adurcup.rainbowshots.R.color.colorBlueDrop;

public class GameView extends SurfaceView implements Runnable {

    private Thread gameThread = null;

    private SurfaceHolder ourHolder;

    private volatile boolean playing;

    private boolean paused = true;

    private Paint paint;

    private long fps;

    private int screenX;

    private int screenY;

    private Button[] buttons = new Button[4];

    private Drop[][] drops = new Drop[4][11];
    // Maximum drops would be 3, one more than value assigned
    // as 0 included
    private int maxDrops = 10;


    Rect frameToDraw;

    public GameView (Context context) {
        super(context);

        Display display = ((GameActivity)context).getWindowManager()
                .getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        ourHolder = getHolder();
        paint = new Paint();

        screenX = size.x;
        screenY = size.y;

        int frameHeight = screenX / 5;
        int frameWidth = screenX / 5;


        frameToDraw = new Rect(
                0,
                0,
                frameWidth,
                frameHeight);

        prepareLevel();
    }

    private void prepareLevel() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button(screenX, screenY, i);
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

        for (int i = 0; i < drops.length; i++) {
            for (int j = 0; j < drops[0].length; j++) {
                drops[i][j] = new Drop(screenX, i);
            }
        }
    }


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
            }
        }
    }

    int[] nextDrop = {0, 0, 0, 0};
    int[] activeDropsCount = {0, 0, 0, 0};
    int[] previousDrop = {0, 0, 0, 0}, bottomDrop = {0, 0, 0, 0};
    int randomGap = 2, randColor = 0;
    int randomCloudGap[] = new int[10];

    Boolean levelUp = false;
    float level = 1;
    Boolean begin = true;

    boolean[] variable = {true, false, true, false, true, false, false, true, false, false};
    public void update() {
        // updating all drops position according to frame
        for (int i = 0; i < 4; i++) {
            for (Drop drop : drops[i]) {
                if (drop.getStatus()) {
                    drop.update(fps, getContext());
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
                if (drops[i][nextDrop[i]].shoot(drops[0][0].DOWN, randColor, level, getContext())) {
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
                        buttons[i].isTopThresholdReached(screenY);
                    } else {
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

        if (buttons[0].getButtonHeight() >= screenY / 2
                && buttons[1].getButtonHeight() >= screenY / 2
                && buttons[2].getButtonHeight() >= screenY / 2
                && buttons[3].getButtonHeight() >= screenY / 2) {
            paused = true;
            levelUp = true;
            level++;
            buttons[0].reset(screenX, screenY, 0);
            buttons[1].reset(screenX, screenY, 1);
            buttons[2].reset(screenX, screenY, 2);
            buttons[3].reset(screenX, screenY, 3);
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

            // Drawing buttons

            paint.setColor(ContextCompat.getColor(getContext(), colorBlueDrop));

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

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorYellowDrop));

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

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPinkDrop));

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

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorGreenDrop));

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

            // Drawing drops color
            for (int i = 0; i < 4; i++) {
                for (Drop drop : drops[i]) {
                    if (drop.getStatus()) {
                        paint.setColor(ContextCompat
                                .getColor(getContext(), drop.getColor()));

                        Drawable drawable = drop.getDrawable();
                        drawable.setBounds(drop.getRecF());
                        drawable.draw(canvas);
                    }
                }
            }
            //background separating lines
            paint.setColor(Color.GRAY);
//            paint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

            canvas.drawLine((screenX / 4) - 1, 0, (screenX / 4) + 1, screenY, paint);
            canvas.drawLine((2 * screenX / 4) - 1, 0, (2 * screenX / 4) + 1, screenY, paint);
            canvas.drawLine((3 * screenX / 4) - 1, 0, (3 * screenX / 4) + 1, screenY, paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPinkDrop));
            canvas.drawLine(0, screenY / 3 - 1, 3 * screenX / 8 , screenY / 3 + 1, paint);
            canvas.drawLine(5 * screenX / 8, screenY / 3 - 1, screenX , screenY / 3 + 1, paint);

            paint.setTextSize(screenX / 30);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorBlueDrop));
            canvas.drawText("WIN THE SHOTS",
                    3 * screenX / 8, screenY / 3 + screenX / 60 , paint);

            //Outer most white circle
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 8) - 13, paint);

            //inner colored circle to show as a fine ring
            paint.setColor(ContextCompat.getColor(getContext(), colorBlueDrop));
            canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 8) - 17, paint);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorYellowDrop));
            canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 8) - 17, paint);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPinkDrop));
            canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 8) - 17, paint);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorGreenDrop));
            canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 8) - 17, paint);

            //inner thicker white dome
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 8) - 70, paint);
            canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 8) - 70, paint);
            canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 8) - 70, paint);
            canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 8) - 70, paint);

            //inner colored to make a hole in the inner ring
            if (!buttonPressed[0]) {
                paint.setColor(ContextCompat.getColor(getContext(), colorBlueDrop));
                canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 8) - 99, paint);
            }
            if (!buttonPressed[1]) {
                paint.setColor(ContextCompat.getColor(getContext(), R.color.colorYellowDrop));
                canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 8) - 99, paint);
            }
            if (!buttonPressed[2]) {
                paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPinkDrop));
                canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 8) - 99, paint);
            }
            if (!buttonPressed[3]) {
                paint.setColor(ContextCompat.getColor(getContext(), R.color.colorGreenDrop));
                canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 8) - 99, paint);
            }

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorGreenDrop));
            paint.setTextSize(screenX / 10);

            canvas.drawText("Level:" + (int) level, screenX / 20, screenY / 20, paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPinkDrop));
            paint.setTextSize(screenX/10);
            if (paused) {
                canvas.drawText("▶", 36 * screenX / 80, screenY / 20, paint);
                paint.setTextSize(screenX / 9);
                if (!begin)
                    canvas.drawText("Paused", screenX / 2 - (screenX / 6), screenY / 2, paint);
                else
                    canvas.drawText("Start", screenX / 2 - (screenX / 7), screenY / 2, paint);
            }
            else {
                canvas.drawText("\u23F8", 141 * screenX / 320, screenY / 20, paint);
            }


            if (levelUp) {
                begin = true;
                paint.setTextSize(screenX / 8);
                canvas.drawText("Level Cleared!", screenX / 20, screenY / 3, paint);
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
        gameThread = new Thread(this);
        gameThread.start();
    }

    boolean buttonPressed[] = {false, false, false, false, false};

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                begin = false;
                levelUp = false;
                float x, y;

                x = motionEvent.getX();
                y = motionEvent.getY();

                paused = y < screenY / 20 && !paused;

                if (activeDropsCount[0] > 0 && x < screenX / 4) {
                    if (y > screenY - buttons[0].getButtonHeight()) {

                        if (!paused) {
                            drops[0][bottomDrop[0]].setInactive();
                            activeDropsCount[0]--;
                            if (bottomDrop[0] == maxDrops) {
                                bottomDrop[0] = 0;
                            } else {
                                bottomDrop[0]++;
                            }
                            buttonPressed[0] = true;
                        }
                    }
                } else if (activeDropsCount[1] > 0 && x < screenX / 2) {
                    if (y > screenY - buttons[1].getButtonHeight()) {
                        if (!paused) {
                            drops[1][bottomDrop[1]].setInactive();
                            activeDropsCount[1]--;
                            if (bottomDrop[1] == maxDrops) {
                                bottomDrop[1] = 0;
                            } else {
                                bottomDrop[1]++;
                            }

                            buttonPressed[1] = true;
                        }
                    }
                } else if (activeDropsCount[2] > 0 && x < (3 * screenX / 4)) {
                    if (y > screenY - buttons[2].getButtonHeight()) {
                        if (!paused) {
                            drops[2][bottomDrop[2]].setInactive();
                            activeDropsCount[2]--;
                            if (bottomDrop[2] == maxDrops) {
                                bottomDrop[2] = 0;
                            } else {
                                bottomDrop[2]++;
                            }
                            buttonPressed[2] = true;
                        }
                    }
                } else {
                    if (activeDropsCount[3] > 0 && y > screenY - buttons[3].getButtonHeight()) {
                        if (!paused) {
                            drops[3][bottomDrop[3]].setInactive();
                            activeDropsCount[3]--;
                            if (bottomDrop[3] == maxDrops) {
                                bottomDrop[3] = 0;
                            } else {
                                bottomDrop[3]++;
                            }
                            buttonPressed[3] = true;
                        }
                    }
                }
                break;
            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:
                buttonPressed[0] = false;
                buttonPressed[1] = false;
                buttonPressed[2] = false;
                buttonPressed[3] = false;
                // paused = true;
                break;
        }
        return true;
    }
}
