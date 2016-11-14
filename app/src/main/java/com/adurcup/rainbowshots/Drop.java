package com.adurcup.rainbowshots;

import android.graphics.Rect;

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

    private int colorChooser[] = {R.color.colorYellowDrop, R.color.colorBlueDrop,
            R.color.colorGreenDrop, R.color.colorPinkDrop};

    // Going nowhere
    private int heading = -1;
    private float speed = 200;

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
        notActiveState = 8;
    }

    float getImpactPointY() {
        if (heading == DOWN) {
            return y + (5 * height / 7);
        } else {
            return y - (2 * height / 7);
        }

    }

    boolean shoot(int direction, int randColor, float level) {
        if (!isActive) {
            speed = 200 + 25 * level;
            y = 0;
            colorState = randColor;
            color = colorChooser[randColor];

            heading = direction;
            isActive = true;
            return true;
        }

        // Bullet already active
        return false;
    }

    int getNotActiveState() {
        return notActiveState;
    }

    private int notActiveState = -1;
    void update(long fps) {

        if(notActiveState > 0) {
            notActiveState--;
            heading = -1;
        } else if(notActiveState == 0) {
            y = 0;
            reset(width * 4, lane);
            notActiveState = -1;
            isActive = false;
        }

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
