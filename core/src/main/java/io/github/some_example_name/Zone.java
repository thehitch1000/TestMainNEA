package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.Arrays;

public class Zone {
    public enum Type {
        UPDIAG, RIGHT, DOWNDIAG, CHANGEDIRE
    }
    polygon polygon;
    Type type;


    public Zone (Type type) {
        this.type = type;
        Color colour;
        switch (type) {
            case UPDIAG: colour = Color.RED; break;
            case RIGHT: colour = Color.GREEN; break;
            case DOWNDIAG: colour = Color.BLUE; break;
            default: colour = Color.YELLOW;
        }
        polygon = new polygon(4,colour);
        polygon.setAlpha(0.5f);
    }
    public Zone() {}

    public Type getType() {
        return type;
    }
    public void setType(Type type) {
        this.type = type;
    }

    public boolean onScreen() {
        return polygon.onScreen();
    }

    public void Draw(ShapeRenderer sr) {
        polygon.Draw(sr);
    }

    public void MoveX(float x) {
        polygon.MoveX(x);
    }
    public void MoveY(float y) {
        polygon.MoveY(y);
    }

    public boolean isPointInZone(float x, float y) {
        return polygon.isPointInShape(new FloatPoint(x,y));
    }

    public boolean isZoneLeftOfScreen() {
        for (FloatPoint point : polygon.points) {
            if (point.getX() >= 0) {
                return false;
            }
        }
        return true;
    }
}
