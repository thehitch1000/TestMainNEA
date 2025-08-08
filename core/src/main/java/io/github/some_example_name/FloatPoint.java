package io.github.some_example_name;

public class FloatPoint {
    private float x,y;

    public FloatPoint (float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }
    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }
    public void setY(float y) {
        this.y = y;
    }

    public void setPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }
    public void setWholePoint(FloatPoint point) {
        this.x = point.getX();
        this.y = point.getY();
    }

    public void MoveX(float X) {
        this.x += X;
    }
    public void MoveY(float Y) {
        this.y += Y;
    }
}
