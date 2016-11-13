package com.adurcup.rainbowshots;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import static com.adurcup.rainbowshots.R.drawable.blue002;

/**
 * Created by kshivang on 15/05/16.
 *
 */
class Drop {

    private float x;
    private float y;
    private int drawRes[][] = new int[4][9];

    private Rect rect;

    // Which way is it shooting
    private final int UP = 0;
    final int DOWN = 1;

    private int colorChooser[] = {R.color.colorBlueDrop, R.color.colorYellowDrop,
            R.color.colorPinkDrop, R.color.colorGreenDrop};

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
        drawRes[0][0] = R.drawable.blue000;
        drawRes[0][1] = blue002;
        drawRes[0][2] = R.drawable.blue005;
        drawRes[0][3] = R.drawable.blue007;
        drawRes[0][4] = R.drawable.blue009;
        drawRes[0][5] = R.drawable.blue010;
        drawRes[0][6] = R.drawable.blue011;
        drawRes[0][7] = R.drawable.blue012;
        drawRes[0][8] = R.drawable.blue013;

        drawRes[1][0] = R.drawable.yellow000;
        drawRes[1][1] = R.drawable.yellow002;
        drawRes[1][2] = R.drawable.yellow005;
        drawRes[1][3] = R.drawable.yellow007;
        drawRes[1][4] = R.drawable.yellow009;
        drawRes[1][5] = R.drawable.yellow010;
        drawRes[1][6] = R.drawable.yellow011;
        drawRes[1][7] = R.drawable.yellow012;
        drawRes[1][8] = R.drawable.yellow013;

        drawRes[2][0] = R.drawable.pink000;
        drawRes[2][1] = R.drawable.pink002;
        drawRes[2][2] = R.drawable.pink005;
        drawRes[2][3] = R.drawable.pink007;
        drawRes[2][4] = R.drawable.pink009;
        drawRes[2][5] = R.drawable.pink010;
        drawRes[2][6] = R.drawable.pink011;
        drawRes[2][7] = R.drawable.pink012;
        drawRes[2][8] = R.drawable.pink013;

        drawRes[3][0] = R.drawable.green000;
        drawRes[3][1] = R.drawable.green002;
        drawRes[3][2] = R.drawable.green005;
        drawRes[3][3] = R.drawable.green007;
        drawRes[3][4] = R.drawable.green009;
        drawRes[3][5] = R.drawable.green010;
        drawRes[3][6] = R.drawable.green011;
        drawRes[3][7] = R.drawable.green012;
        drawRes[3][8] = R.drawable.green013;

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
        notActiveState = 9;
//        y = 0;
//        reset(width * 4, lane);
//        isActive = false;
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
            drawable = ContextCompat.getDrawable(context, drawRes[randColor][0]);
            color = colorChooser[randColor];

            heading = direction;
            isActive = true;
            return true;
        }

        // Bullet already active
        return false;
    }

    private int notActiveState = -1;
    void update(long fps, Context context) {

        if(notActiveState > 0) {
            drawable = ContextCompat.getDrawable(context,
                    drawRes[colorState][9 - notActiveState]);
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
