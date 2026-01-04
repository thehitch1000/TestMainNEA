package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface IntBarrier {
    void Draw(ShapeRenderer sr);
    void MoveX(float X);
    void MoveY(float Y);
    void setX(float X);
}

abstract class Barrier implements IntBarrier {
    protected Shape shape;

    public void MoveX(float X) {
        shape.MoveX(X);
    }
    public void MoveY(float Y) {
        shape.MoveY(Y);
    }
    public void setX(float X) {}
    public void setY(float Y) {}
    public void setHeight(float height) {}
    public void setWidth(float width) {}
    public float getX() {return 0;}
    public float getY() {return 0;}
    public float getWidth() {return 0;}
    public float getHeight() {return 0;}
    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }
}

class RectPath extends Barrier {
    private boolean bottom;

    public RectPath(boolean bottom) {
        shape = new Rect(0, 0);
        this.bottom = bottom;
    }
    public RectPath(float x, float y, float width, float height, boolean bottom) {
        shape = new Rect(x, y, width, height);
        this.bottom = bottom;
    }

    public void setX (float X) {
        shape.setX(X);
    }
    public void setY(float Y) {
        shape.setY(Y);
    }
    public void setHeight(float height) {
        shape.setHeight(height);
    }
    public void setWidth(float width) {
        shape.setWidth(width);
    }
    public float getX() {
        return shape.getX();
    }
    public float getY() {
        return shape.getY();
    }
    public float getHeight() {
        return shape.getHeight();
    }
    public float getWidth() {
        return shape.getWidth();
    }

    public boolean isBottom() {
        return bottom;
    }
    public void setBottom(boolean bottom) {
        this.bottom = bottom;
    }
}

class TriPath extends Barrier {
    private boolean bottom;
    public TriPath(boolean bottom) {
        shape = new Tri(new FloatPoint(0,0), new FloatPoint(0,0), new FloatPoint(0,0));
        this.bottom = bottom;
    }
    public TriPath(FloatPoint point1, FloatPoint point2, FloatPoint point3, boolean bottom) {
        shape = new Tri(point1, point2, point3);
        this.bottom = bottom;
    }

    public boolean isBottom() {
        return bottom;
    }
    public void setBottom(boolean bottom) {
        this.bottom = bottom;
    }
}
