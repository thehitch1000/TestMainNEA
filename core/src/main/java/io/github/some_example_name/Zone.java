package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Arrays;

public class Zone {
    public enum Type {
        UPDIAG, RIGHT, DOWNDIAG, CHANGEDIRE
    }
    Quad quad;
    Type type;
    private boolean used;
    private boolean ghosted;


    public Zone (Type type) {
        this.type = type;
        Color colour;
        switch (type) {
            case UPDIAG: colour = Color.RED; break;
            case RIGHT: colour = Color.GREEN; break;
            case DOWNDIAG: colour = Color.BLUE; break;
            default: colour = Color.YELLOW;
        }
        quad = new Quad(colour);
        quad.setAlpha(0.5f);

        used = false;
        ghosted = false;
    }
    public Zone() {}

    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }

    public boolean isUsed() {
        return used;
    }
    public void used() {
        used = true;
    }

    public boolean isGhosted() {
        return ghosted;
    }
    public void ghosted() {
        ghosted = true;
    }

    public boolean onScreen() {
        return quad.onScreen();
    }

    public void Draw(ShapeRenderer sr) {
        quad.Draw(sr);
    }

    public void MoveX(float x) {
        quad.MoveX(x);
    }
    public void MoveY(float y) {
        quad.MoveY(y);
    }

    public boolean isPointInZone(float x, float y) {
        return quad.isPointInShape(new FloatPoint(x,y));
    }

    public boolean isZoneLeftOfScreen() {
        for (FloatPoint point : quad.points) {
            if (point.getX() >= 0) {
                return false;
            }
        }
        return true;
    }
}
