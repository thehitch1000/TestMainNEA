package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LineSegment {
    public enum Vertical {
        UP, DOWN, NONE
    }

    public enum Horizontal {
        LEFT, RIGHT, NONE
    }

    FloatPoint startPoint, endPoint;
    private float angle;
    Vertical vertical;
    Horizontal horizontal;

    public LineSegment(FloatPoint startPoint, FloatPoint endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.angle = 0;
        this.vertical = Vertical.NONE;
        this.horizontal = Horizontal.NONE;
        CalcAngle();
    }

    public float getAngle() {
        return angle;
    }

    public Vertical getVertical() {
        return vertical;
    }
    public void setVertical(Vertical vertical) {
        this.vertical = vertical;
    }

    public Horizontal getHorizontal() {
        return horizontal;
    }
    public void setHorizontal(Horizontal horizontal) {
        this.horizontal = horizontal;
    }

    public void setSegment() {
        if (startPoint.getX() == endPoint.getX()) {
            vertical = Vertical.UP;
            horizontal = Horizontal.NONE;
            angle = (float) Math.PI / 2;
        } else if (startPoint.getY() == endPoint.getY()) {
            vertical = Vertical.NONE;
            horizontal = Horizontal.RIGHT;
            angle = 0;
        } else {
            vertical = Vertical.NONE;
            horizontal = Horizontal.NONE;
            angle = (float) Math.atan2(endPoint.getY() - startPoint.getY(), endPoint.getX() - startPoint.getX());
        }
    }

    public void MoveX(float X) {
        startPoint.MoveX(X);
        endPoint.MoveX(X);
    }
    public void MoveY(float Y) {
        startPoint.MoveY(Y);
        endPoint.MoveY(Y);
    }

    public boolean isPointInSegment(float x, float y) {
        float minX = Math.min(startPoint.getX(), endPoint.getX());
        float maxX = Math.max(startPoint.getX(), endPoint.getX());
        float minY = Math.min(startPoint.getY(), endPoint.getY());
        float maxY = Math.max(startPoint.getY(), endPoint.getY());

        return !(x < minX) && !(x > maxX) && !(y < minY) && !(y > maxY);
    }
    public void CalcAngle() {
        angle = (float) Math.atan2(endPoint.getY() - startPoint.getY(), endPoint.getX() - startPoint.getX());
    }

    public void Draw(ShapeRenderer sr) {
        sr.line(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    }
}
