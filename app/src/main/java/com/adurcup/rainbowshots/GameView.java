package com.adurcup.rainbowshots;

import android.content.Context;
import android.view.SurfaceView;

/**
 * Created by kshivang on 15/05/16.
 */
public class GameView extends SurfaceView implements Runnable{

    private Context context;

    private Thread gameThread = null;

    public GameView(Context context, int x, int y) {
        super(context);

        this.context = context;
    }

    @Override
    public void run(){

    }

    public void resume(){
    }

    public void pause(){
    }
}
