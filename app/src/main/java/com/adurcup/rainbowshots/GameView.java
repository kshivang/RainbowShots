package com.adurcup.rainbowshots;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

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

    private Drop[][] drops = new Drop[10][10];
    // Maximum drops would be 3, one more than value assigned
    // as 0 included
    private int maxDrops = 2;

    Bitmap bitmapBlueDrop, bitmapYellowDrop, bitmapPinkDrop, bitmapGreenDrop;

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

        bitmapBlueDrop = BitmapFactory
                .decodeResource(this.getResources(), R.drawable.blue_drops);
        bitmapYellowDrop = BitmapFactory
                .decodeResource(this.getResources(), R.drawable.yellow_drop);
        bitmapPinkDrop = BitmapFactory
                .decodeResource(this.getResources(), R.drawable.pink_drop);
        bitmapGreenDrop = BitmapFactory
                .decodeResource(this.getResources(), R.drawable.green_drop);


        // Scale the bitmap to the correct size
        // We need to do this because Android automatically
        // scales bitmaps based on screen density
        int frameCount = 9;
        bitmapBlueDrop = Bitmap.createScaledBitmap(bitmapBlueDrop,
                frameWidth * frameCount,
                frameHeight,
                false);
        bitmapYellowDrop = Bitmap.createScaledBitmap(bitmapYellowDrop,
                frameWidth * frameCount,
                frameHeight,
                false);
        bitmapPinkDrop = Bitmap.createScaledBitmap(bitmapPinkDrop,
                frameWidth * frameCount,
                frameHeight,
                false);
        bitmapGreenDrop = Bitmap.createScaledBitmap(bitmapGreenDrop,
                frameWidth * frameCount,
                frameHeight,
                false);

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

        for (int i = 0; i < drops.length; i++) {
            for (int j = 0; j < drops[0].length; j++) {
                drops[i][j] = new Drop(screenX, screenY, i);
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
//            timeThisFrame = 17;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
        }
    }

    int[] nextDrop = {0, 0, 0, 0};
    int[] activeDropsCount = {0, 0, 0, 0};
    int[] previousDrop = {0, 0, 0, 0}, bottomDrop = {0, 0, 0, 0};
    int randomGap = 2, randColor = 0;
    Boolean levelUp = false;
    float level = 1;

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
                if (drop.getStatus()) {
                    if (drop.getImpactPointY() > screenY - buttons[i].getButtonHeight()) {
                        if (drop.getColor() == i) {
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

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorBlueDrop));

            canvas.drawRect(buttons[0].getRect(), paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorYellowDrop));

            canvas.drawRect(buttons[1].getRect(), paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPinkDrop));

            canvas.drawRect(buttons[2].getRect(), paint);

            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorGreenDrop));

            canvas.drawRect(buttons[3].getRect(), paint);

            // Drawing drops color
            for (int i = 0; i < 4; i++) {
                for (Drop drop : drops[i]) {
                    if (drop.getStatus()) {
                        switch (drop.getColor()) {
                            case 0:
                                paint.setColor(ContextCompat
                                        .getColor(getContext(), R.color.colorBlueDrop));
                                canvas.drawBitmap(bitmapBlueDrop,
                                        frameToDraw, drop.getRecF(), paint);
                                break;
                            case 1:
                                paint.setColor(ContextCompat
                                        .getColor(getContext(), R.color.colorYellowDrop));
                                canvas.drawBitmap(bitmapYellowDrop,
                                        frameToDraw, drop.getRecF(), paint);
                                break;
                            case 2:
                                paint.setColor(ContextCompat
                                        .getColor(getContext(), R.color.colorPinkDrop));
                                canvas.drawBitmap(bitmapPinkDrop,
                                        frameToDraw, drop.getRecF(), paint);
                                break;
                            default:
                                paint.setColor(ContextCompat
                                        .getColor(getContext(), R.color.colorGreenDrop));
                                canvas.drawBitmap(bitmapGreenDrop,
                                        frameToDraw, drop.getRecF(), paint);
                        }

//                        canvas.drawRect(drop.getRecF(), paint);
//
//                        canvas.drawCircle(drop.getRecF().centerX(),
//                                drop.getRecF().centerY(), screenX / 10, paint);
//                        paint.setColor(Color.WHITE);
//                        canvas.drawCircle(drop.getRecF().centerX(),
//                                drop.getRecF().centerY() + 2 * screenX / 30, screenX / 30, paint);
//                        paint.setColor(Color.BLACK);
//                        canvas.drawCircle(drop.getRecF().centerX(),
//                               drop.getRecF().centerY() + 9 * screenX / 120, screenX / 60, paint);


                    }
                }
            }
            //background separating lines
            paint.setColor(Color.GRAY);
            paint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

            canvas.drawLine((screenX / 4) - 1, 0, (screenX / 4) + 1, screenY, paint);
            canvas.drawLine((2 * screenX / 4) - 1, 0, (2 * screenX / 4) + 1, screenY, paint);
            canvas.drawLine((3 * screenX / 4) - 1, 0, (3 * screenX / 4) + 1, screenY, paint);
            canvas.drawLine(0, screenY / 2 - 1, 3 * screenX / 8 , screenY / 2 + 1, paint);
            canvas.drawLine(5 * screenX / 8, screenY / 2 - 1, screenX , screenY / 2 + 1, paint);

            paint.setTextSize(screenX / 30);
            paint.setColor(Color.BLACK);
            canvas.drawText("Fill bars up to this",
                    3 * screenX / 8, screenY / 2 + screenX / 60 , paint);

            //Outer most white circle
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 8) - 13, paint);
            canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 8) - 13, paint);

            //inner colored circle to show as a fine ring
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorBlueDrop));
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
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorBlueDrop));
            canvas.drawCircle(screenX / 8, screenY - (screenX / 8), (screenX / 8) - 99, paint);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorYellowDrop));
            canvas.drawCircle(screenX / 8 * 3, screenY - (screenX / 8), (screenX / 8) - 99, paint);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorPinkDrop));
            canvas.drawCircle(screenX / 8 * 5, screenY - (screenX / 8), (screenX / 8) - 99, paint);
            paint.setColor(ContextCompat.getColor(getContext(), R.color.colorGreenDrop));
            canvas.drawCircle(screenX / 8 * 7, screenY - (screenX / 8), (screenX / 8) - 99, paint);

            paint.setTextSize(screenX / 10);

            canvas.drawText("Level:" + (int) level, screenX / 20, screenY / 20, paint);

            if (levelUp) {
                paint.setTextSize(screenX / 8);
                canvas.drawText("Level Cleared!", screenX / 20, screenY / 2, paint);
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

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            case MotionEvent.ACTION_DOWN:
                paused = false;
                levelUp = false;
                float x, y;

                x = motionEvent.getX();
                y = motionEvent.getY();

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
                        }
                    }
                }
                break;
            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:

                // paused = true;
                break;
        }
        return true;
    }
}
