package com.adurcup.rainbowshots;

import android.graphics.RectF;

/**
 * Created by kshivang on 15/05/16.
 *
 */
class Drop {

    private float x;
    private float y;

    private RectF rect;

    // Which way is it shooting
    private final int UP = 0;
    final int DOWN = 1;

    // Going nowhere
    private int heading = -1;
    private float speed = 350;

    private int width;
    private int height;

    private boolean isActive;

    private int color = 0;

    int getColor() {
        return color;
    }

    Drop(int screenX, int screenY, int lane) {
//        height = screenY / 20;
        height = screenX / 4;
        width = screenX / 4;
        isActive = false;
        rect = new RectF();
        y = 0;
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

    RectF getRecF() {
        return rect;
    }

    boolean getStatus() {
        return isActive;
    }

    void setInactive() {
        isActive = false;
        y = 0;
    }

    float getImpactPointY() {
        if (heading == DOWN) {
            return y + height;
        } else {
            return y;
        }

    }

    boolean shoot(int direction, int colour, float level) {
        if (!isActive) {
            speed = 100 + 100*level;
            y = 0;
            color = colour;
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
        } else {
            y = y + speed / fps;
        }

        // Update rect
        rect.left = x;
        rect.right = x + width;
        rect.top = y;
        rect.bottom = y + height;

    }
}
