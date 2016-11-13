package com.adurcup.rainbowshots;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

/**
 * Created by kshivang on 15/05/16.
 *
 */
class Drop {

    private float x;
    private float y;

    private Rect rect;

    // Which way is it shooting
    private final int UP = 0;
    final int DOWN = 1;

    // Going nowhere
    private int heading = -1;
    private float speed = 250;

    private int width;
    private int height;

    private int lane;

    private boolean isActive;

    private int color = R.color.colorBlueDrop;

    int getColorState() {
        return colorState;
    }

    private int colorState = 0;

    int getColor() {
        return color;
    }

    private Drawable drawable;

    Drop(int screenX, int lane) {
        reset(screenX, lane);
    }

    private void reset(int screenX, int lane) {
        height = screenX / 4;
        width = screenX / 4;
        this.lane = lane;

        isActive = false;
        y = 0;
        if (rect == null)
            rect = new Rect();
        else {
            rect.top = (2 * height / 7);
            rect.bottom = (5 * height / 7);
        }

        switch (lane) {
            case 0:
                x = 0;
                break;
            case 1:
                x = screenX / 4;
                break;
            case 2:
                x = screenX / 2;
                break;
            default:
                x = 3 * screenX / 4;
        }
    }

    Rect getRecF() {
        return rect;
    }

    boolean getStatus() {
        return isActive;
    }

    void setInactive() {
        y = 0;
        reset(width * 4, lane);
        isActive = false;
    }

    float getImpactPointY() {
        if (heading == DOWN) {
            return y + (5 * height / 7);
        } else {
            return y - (2 * height / 7);
        }

    }

    Drawable getDrawable() {
        return drawable;
    }

    boolean shoot(int direction, int randColor, float level, Context context) {
        if (!isActive) {
            speed = 100 + 100*level;
            y = 0;
            colorState = randColor;
            switch (randColor) {
                case 1:
                    color = R.color.colorYellowDrop;
                    drawable = ContextCompat.getDrawable(context, R.drawable.yellow000);
                    break;
                case 2:
                    color = R.color.colorPinkDrop;
                    drawable = ContextCompat.getDrawable(context, R.drawable.pink000);
                    break;
                case 3:
                    drawable = ContextCompat.getDrawable(context, R.drawable.green000);
                    color = R.color.colorGreenDrop;
                    break;
                default:
                    color = R.color.colorBlueDrop;
                    drawable = ContextCompat.getDrawable(context, R.drawable.blue000);
            }

            heading = direction;
            isActive = true;
            return true;
        }

        // Bullet already active
        return false;
    }

    void update(long fps) {

        // Just move up or down
        if (heading == UP) {
            y = y - speed / fps;
        } else if(heading == DOWN) {
            y = y + speed / fps;
        }

        // Update rect
        rect.left = (int) x + (2 * width / 7);
        rect.right = (int) x + (5 * width / 7) ;
        rect.top = (int) y + (2 * height / 7);
        rect.bottom = (int) y + (5 * height / 7);

    }
}
