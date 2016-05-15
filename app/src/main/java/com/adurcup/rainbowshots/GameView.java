package com.adurcup.rainbowshots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by kshivang on 15/05/16.
 */
public class GameView extends SurfaceView implements Runnable {

    private Context context;

    private Thread gameThread = null;

    private SurfaceHolder ourHolder;

    private volatile boolean playing;

    private boolean paused = true;

    private Canvas canvas;
    private Paint paint;

    private long fps;

    private long timeThisFrame;

    private int screenX;
    private int screenY;

    private Button[] buttons = new Button[4];

    private Drop[] drops = new Drop[10];
    private int maxDrops = 8;

    public GameView(Context context, int x, int y) {
        super(context);

        this.context = context;

        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        prepareLevel();

    }

    private void prepareLevel() {

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Button(screenX, screenY, i);
        }

        for (int i = 0; i < drops.length; i++) {
            drops[i] = new Drop(screenX, screenY, 2);
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

            //timeThisFrame = System.currentTimeMillis() - startFrameTime;
            timeThisFrame = (long) 33.33;
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
        }
    }

    int nextDrop = 0;
    int rand = 2, randColor = 0;

    public void update() {
        // Move the paddle if required
        for (int i = 0; i < drops.length; i++) {
            if (drops[i].getStatus()) {
                drops[i].update(fps);
            }
        }

        Random generator = new Random();

        //currentDrop = nextDrop - 1;
        //if drop is 0 ,i.e. beginning of game, or drop is at random position
        //trigger new drop
        if (nextDrop == 0 || (drops[nextDrop - 1].getImpactPointY() > rand)) {
            if (drops[nextDrop].shoot(drops[0].DOWN, randColor)) {
                randColor = generator.nextInt(4);
                //rand vary from screen / 4 to screenY / 5
                rand = generator.nextInt((screenY / 5)) + screenY / 4;
                nextDrop++;
                //       currentDrop = nextDrop;

                if (nextDrop == maxDrops) {
                    nextDrop = 1;
                }
            }
        }

        for (int i = 0; i < drops.length; i++) {
            if (drops[i].getImpactPointY() > screenY - buttons[2].getButtonHeight()) {
                if (drops[i].getStatus()) {
                    if (drops[i].getColor() == 2) {
                        buttons[2].isTopThresholdReached(screenY);
                    } else {
                        buttons[2].isBottomThresholdReached(screenY);
                    }
                }
                drops[i].setInactive();
            }
        }
        // Check for ball colliding with a brick
    }

    // Draw the newly updated scene


    public void draw() {
        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(Color.rgb(255, 255, 255));

            // Choose the brush color for drawing
            paint.setColor(Color.rgb(255, 0, 114));
            // Draw the paddle
            canvas.drawRect(buttons[0].getRect(), paint);

            paint.setColor(Color.rgb(43, 233, 68));

            canvas.drawRect(buttons[1].getRect(), paint);

            paint.setColor(Color.rgb(0, 188, 254));

            canvas.drawRect(buttons[2].getRect(), paint);

            paint.setColor(Color.rgb(255, 215, 0));

            canvas.drawRect(buttons[3].getRect(), paint);
            // Change the brush color for drawing

            for (int i = 0; i < drops.length; i++) {
                if (drops[i].getStatus()) {
                    switch (drops[i].getColor()) {
                        case 0:
                            paint.setColor(Color.rgb(255, 0, 114));
                            break;
                        case 1:
                            paint.setColor(Color.rgb(43, 233, 68));
                            break;
                        case 2:
                            paint.setColor(Color.rgb(0, 188, 254));
                            break;
                        default:
                            paint.setColor(Color.rgb(255, 215, 0));
                    }
                    canvas.drawRect(drops[i].getRect(), paint);
                }
            }

            paint.setColor(Color.GRAY);
            canvas.drawRect((screenX/2)-1,0,(screenX/2)+1,screenY, paint);

            paint.setColor(Color.GRAY);
            canvas.drawRect((screenX/4)-1,0,(screenX/4)+1,screenY, paint);

            paint.setColor(Color.GRAY);
            canvas.drawRect((3*screenX/4)-1,0,(3*screenX/4)+1,screenY, paint);


            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX/8, screenY-(screenX/8),(screenX/8)-13,paint);
            paint.setColor(Color.rgb(255, 0, 114));
            canvas.drawCircle(screenX/8, screenY-(screenX/8),(screenX/8)-17,paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX/8, screenY-(screenX/8),(screenX/8)-70,paint);
            paint.setColor(Color.rgb(255, 0, 114));
            canvas.drawCircle(screenX/8, screenY-(screenX/8),(screenX/8)-99,paint);


            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX/8*3, screenY-(screenX/8),(screenX/8)-13,paint);
            paint.setColor(Color.rgb(43, 233, 68));
            canvas.drawCircle(screenX/8*3, screenY-(screenX/8),(screenX/8)-17,paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX/8*3, screenY-(screenX/8),(screenX/8)-70,paint);
            paint.setColor(Color.rgb(43, 233, 68));
            canvas.drawCircle(screenX/8*3, screenY-(screenX/8),(screenX/8)-99,paint);


            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX/8*5, screenY-(screenX/8),(screenX/8)-13,paint);
            paint.setColor(Color.rgb(0, 188, 254));
            canvas.drawCircle(screenX/8*5, screenY-(screenX/8),(screenX/8)-17,paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX/8*5, screenY-(screenX/8),(screenX/8)-70,paint);
            paint.setColor(Color.rgb(0, 188, 254));
            canvas.drawCircle(screenX/8*5, screenY-(screenX/8),(screenX/8)-99,paint);


            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX/8*7, screenY-(screenX/8),(screenX/8)-13,paint);
            paint.setColor(Color.rgb(255, 215, 0));
            canvas.drawCircle(screenX/8*7, screenY-(screenX/8),(screenX/8)-17,paint);
            paint.setColor(Color.WHITE);
            canvas.drawCircle(screenX/8*7, screenY-(screenX/8),(screenX/8)-70,paint);
            paint.setColor(Color.rgb(255, 215, 0));
            canvas.drawCircle(screenX/8*7, screenY-(screenX/8),(screenX/8)-99,paint);

            paint.setTextSize(40);
            canvas.drawText("FPS:" + fps, 20, 70, paint);
            canvas.drawText("Rand:" + rand, 250, 70, paint);
            canvas.drawText("NextDrop:" + nextDrop, 500, 70, paint);

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


                if (!paused) {
                    drops[nextDrop].setInactive();
                }

                paused = false;

                if (motionEvent.getY() > screenY - screenY / 8) {
                    if (motionEvent.getX() > screenX / 2) {

                    } else {
                    }
                }

                if (motionEvent.getY() < screenY - screenY / 8) {
                    // Shots fired
                }

                break;


            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:

                //paused = true;
                if (motionEvent.getY() > screenY - screenY / 10) {
                }
                break;
        }
        return true;
    }
}
