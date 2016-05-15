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
public class GameView extends SurfaceView implements Runnable{

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

    private Drop[][] drops = new Drop[10][4];
    // Maximum drops would be 3, one more than value assigned
    // as 0 included
    private int maxDrops = 2;

    public GameView(Context context, int x, int y) {
        super(context);

        this.context = context;

        ourHolder = getHolder();
        paint = new Paint();

        screenX = x;
        screenY = y;

        prepareLevel();

    }

    private void prepareLevel(){
        for(int i = 0; i < buttons.length; i++){
            buttons[i] = new Button(screenX, screenY, i);
        }

        for(int i = 0; i < drops.length; i++){
            for(int j = 0; j < drops[0].length; j++) {
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
            if(!paused){
                update();
            }

            // Draw the frame
            draw();

            // Calculate the fps this frame
            // We can then use the result to
            // time animations and more.

            timeThisFrame = System.currentTimeMillis() - startFrameTime;
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

    public void update() {
        // updating all drops position according to frame
        for(int i = 0; i < 4; i++){
            for(Drop drop : drops[i]) {
                if (drop.getStatus()) {
                    drop.update(fps);
                }
            }
        }

        Random generator = new Random();

        // fetching previous drop
        for(int i = 0; i < 4; i++) {
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
            if (activeDropsCount[i] == 0 || (drops[i][previousDrop[i]].getImpactPointY() > randomGap)) {
                if (drops[i][nextDrop[i]].shoot(drops[0][0].DOWN, randColor)) {
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
                            buttons[i].isBottomThresholdReached(screenY);
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

    }

    // Draw the newly updated scene
    public void draw() {

        // Make sure our drawing surface is valid or we crash
        if (ourHolder.getSurface().isValid()) {
            // Lock the canvas ready to draw
            canvas = ourHolder.lockCanvas();

            // Draw the background color
            canvas.drawColor(Color.rgb(255, 255, 255));

            // Drawing buttons
            paint.setColor(Color.rgb(255, 0, 114));

            canvas.drawRect(buttons[0].getRect(), paint);

            paint.setColor(Color.rgb(43, 233, 68));

            canvas.drawRect(buttons[1].getRect(), paint);

            paint.setColor(Color.rgb(0, 188, 254));

            canvas.drawRect(buttons[2].getRect(), paint);

            paint.setColor(Color.rgb(255, 215, 0));

            canvas.drawRect(buttons[3].getRect(), paint);

            // Drawing drops color
            for (int i = 0; i < 4; i++) {
                for (Drop drop : drops[i]) {
                    if (drop.getStatus()) {
                        switch (drop.getColor()) {
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
                        canvas.drawRect(drop.getRect(), paint);
                    }
                }
            }

            paint.setTextSize(100);

            canvas.drawText("FPS:"+ fps + "  :"+ timeThisFrame + "", 50, 70, paint);
            // Draw everything to the screen
            ourHolder.unlockCanvasAndPost(canvas);
        }
    }

    public void pause(){
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


                if(!paused) {
                    for (int i = 0; i < 4; i++) {
                        drops[i][bottomDrop[i]].setInactive();
                        activeDropsCount[i]--;
                        if (bottomDrop[i] == maxDrops) {
                            bottomDrop[i] = 0;
                        } else {
                            bottomDrop[i]++;
                        }
                    }
                }

                paused = false;

                if(motionEvent.getY() > screenY - screenY / 8) {
                    if (motionEvent.getX() > screenX / 2) {

                    } else {
                    }
                }

                if(motionEvent.getY() < screenY - screenY / 8) {
                    // Shots fired
                }

                break;


            // Player has removed finger from screen
            case MotionEvent.ACTION_UP:

                //paused = true;
                if(motionEvent.getY() > screenY - screenY / 10) {
                }
                break;
        }
        return true;
    }
}
