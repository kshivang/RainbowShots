package com.adurcup.rainbowshots;

import android.graphics.RectF;

/**
 * Created by kshivang on 15/05/16.
 */
public class Button {

    private RectF rect;

    private boolean isVisible;

    private int buttonWidth, buttonHeight;

    public Button(int screenX, int screenY) {
        buttonHeight = screenY / 10;
        buttonWidth = screenX / 4;

        rect = new RectF();
        rect.left = screenX / 2;
        rect.top = screenY - 20;
        rect.right = screenX / 2 + buttonWidth;
        rect.bottom = screenY - 20 - buttonHeight;
    }

    public RectF getRect(){
        return rect;
    }
}
