package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Zone {
    polygon polygon;
    private int type;

    public Zone (int type) {
        this.type = type;

        if (type == 0) {
            polygon = new polygon(4, new Color(1,0,0,0.5f), 0.5f);
        } else {
            polygon = new polygon(4, new Color(0,1,0, 0.5f), 0.5f);
        }
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
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
}
