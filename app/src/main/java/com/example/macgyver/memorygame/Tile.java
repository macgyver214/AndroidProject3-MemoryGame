package com.example.macgyver.memorygame;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.view.View;
import android.widget.ImageButton;

/**
 * Created by MacGyver on 10/2/2016.
 */

public class Tile {



    //references to other objects needed
    private final GameFragment currentGame;
    private View currentView;
    private int picReference;
    private int buttonID;
    private boolean isFlipped;

    public Tile(GameFragment game, int picReference, int buttonID) {
        this.currentGame = game;
        this.picReference = picReference;
        this.buttonID = buttonID;
    }

    public View getCurrentView() {return currentView;}

    public int getPicReference() {return picReference;}

    public int getButtonID() {return buttonID;}

    public void setFlipped(boolean state) {this.isFlipped = state;}

    public boolean getFlipped() { return isFlipped;}

    public void updateDrawableState() {
        if (currentView == null) return;
        Resources res = currentGame.getResources();
        ImageButton ib = (ImageButton) currentGame.getActivity().findViewById(buttonID);
        if(isFlipped) {
            Drawable d = res.getDrawable(currentGame.getPicId(picReference));
            ib.setImageDrawable(d);
        }
        else {
            Drawable d = res.getDrawable(res.getIdentifier("unknown_tile", "drawable", currentGame.getActivity().getPackageName()));
            ib.setImageDrawable(d);
        }


    }

    public void animate() {
        Animator anim = AnimatorInflater.loadAnimator(currentGame.getActivity(), R.animator.flip_tile);
        if(getCurrentView() != null) {
            anim.setTarget(getCurrentView());
            anim.start();
        }
    }

}
