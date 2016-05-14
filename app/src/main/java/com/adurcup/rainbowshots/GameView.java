package com.adurcup.rainbowshots;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

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

    private Button middleButton;

    private Drop[] drops = new Drop[10];
    private int maxDrops = 3;

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
        middleButton = new Button(screenX, screenY);

        for(int i = 0; i < drops.length; i++){
            drops[i] = new Drop(screenX, screenY);
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
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
        }
    }

    int nextDrop = 0;
    public void update() {
        // Move the paddle if required
        for(int i = 0; i < drops.length; i++){
            if(drops[i].getStatus()) {
                drops[i].update(fps);
            }
        }


        if(nextDrop == 0) {
            if (drops[nextDrop].shoot(screenX / 2, screenY / 2, drops[0].DOWN)) {

                nextDrop++;

                /*if (nextDrop == maxDrops) {
                    // This stops the firing of another bullet until one completes its journey
                    // Because if bullet 0 is still active shoot returns false.
                    nextDrop = 0;
                }*/
            }
        }


        for(int i = 0; i < drops.length; i++){

            if(drops[i].getImpactPointY() > screenY){
                drops[i].setInactive();
                nextDrop = 0;
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
            canvas.drawColor(Color.argb(255,  26, 128, 182));

            // Choose the brush color for drawing
            paint.setColor(Color.argb(255,  255, 255, 255));

            // Draw the paddle
            canvas.drawRect(middleButton.getRect(), paint);

            // Draw the ball
            canvas.drawRect(middleButton.getRect(), paint);

            // Change the brush color for drawing
            paint.setColor(Color.argb(255,  249, 129, 0));

            for(int i = 0; i < drops.length; i++){
                if(drops[i].getStatus()) {
                    canvas.drawRect(drops[i].getRect(), paint);
                }
            }
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

                paused = true;
                if(motionEvent.getY() > screenY - screenY / 10) {
                }
                break;
        }
        return true;
    }
}
