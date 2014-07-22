package com.famigo.rawsmacktest.app.view;

/**
 * Created by adam.fitzgerald on 7/21/14.
 */
public class StrokeEvent {

    public float x;
    public float y;
    public int action;
    public int index;

    public StrokeEvent(float x, float y, int action, int index) {
        this.x =x;
        this.y = y;
        this.action = action;
        this.index = index;
    }

    @Override
    public String toString() {
        return String.format("x:%.2f, y:%.2f, %d, %d",x,y,action, index);
    }
}
