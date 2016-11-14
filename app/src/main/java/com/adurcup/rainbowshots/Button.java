package com.adurcup.rainbowshots;

import android.graphics.RectF;

/**
 * Created by kshivang on 15/05/16.
 *
 */
public class Button {

    private RectF rect;


    private int buttonHeight;

    public Button(int screenX, int screenY, int position) {
        rect = new RectF();
        reset(screenX, screenY, position);

    }


    int getButtonHeight() {
        return buttonHeight;
    }

    Boolean isTopThresholdReached(int screenY){

        if(buttonHeight < 2 * screenY / 3) {
            buttonHeight = buttonHeight + screenY / 20;
            rect.bottom = screenY - buttonHeight;
            return false;
        }
        return true;
    }

    void reset(int screenX, int screenY, int position) {
        buttonHeight = screenX / 4;
        int buttonWidth = screenX / 4;

        rect.top = screenY;
        rect.bottom = screenY - buttonHeight;
        switch (position) {
            case 0:
                rect.left = 0;
                rect.right = buttonWidth;
                break;
            case 1:
                rect.left = screenX / 4;
                rect.right = screenX / 4 + buttonWidth;
                break;
            case 2:
                rect.left = screenX / 2;
                rect.right = screenX / 2 + buttonWidth;
                break;
            default:
                rect.left = (3 * screenX) / 4;
                rect.right = (3 * screenX) / 4 + buttonWidth;
                break;
        }
    }

    Boolean isBottomThresholdReached(int screenY, int screenX){

        if(buttonHeight > screenX / 4) {
            buttonHeight = buttonHeight - screenY / 80;
            rect.bottom = screenY - buttonHeight;
            return false;
        }
        return true;
    }

    RectF getRect(){
        return rect;
    }
}
