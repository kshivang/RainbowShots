package com.adurcup.rainbowshots;

import android.graphics.RectF;

/**
 * Created by kshivang on 15/05/16.
 */
public class Drop {

    private float x;
    private float y;

    private RectF rect;

    // Which way is it shooting
    public final int UP = 0;
    public final int DOWN = 1;

    // Going nowhere
    int heading = -1;
    float speed = 350;

    private int width;
    private int height;

    private boolean isActive;

    private int color = 0;

    public int getColor() {
        return color;
    }

    public Drop(int screenX, int screenY, int lane) {
        height = screenY / 20;
        width = screenX / 6;
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

    public RectF getRect() {
        return rect;
    }

    public boolean getStatus() {
        return isActive;
    }

    public void setInactive() {
        isActive = false;
        y = 0;
    }

    public float getImpactPointY() {
        if (heading == DOWN) {
            return y + height;
        } else {
            return y;
        }

    }

    public boolean shoot(int direction, int colour, float level) {
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

    public void update(long fps) {

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
