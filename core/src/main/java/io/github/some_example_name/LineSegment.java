package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class LineSegment {
    public enum Direction {
        VERTICAL, HORIZONTAL, DIAGONAL
    }

    FloatPoint startPoint, endPoint;
    private float angle;
    Direction direction;

    public LineSegment(FloatPoint startPoint, FloatPoint endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        setSegment();
    }
    public LineSegment(float x1, float y1, float x2, float y2) {
        this.startPoint = new FloatPoint(x1, y1);
        this.endPoint = new FloatPoint(x2, y2);
        setSegment();
    }

    public float getAngle() {
        return angle;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setSegment() {
        if (startPoint.getX() == endPoint.getX()) {
            direction = Direction.VERTICAL;
            angle = (float) Math.PI / 2;
        } else if (startPoint.getY() == endPoint.getY()) {
            direction = Direction.HORIZONTAL;
            angle = 0;
        } else {
            direction = Direction.DIAGONAL;
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

    public float Distance() {
        float minX = Math.min(startPoint.getX(), endPoint.getX());
        float maxX = Math.max(startPoint.getX(), endPoint.getX());
        float minY = Math.min(startPoint.getY(), endPoint.getY());
        float maxY = Math.max(startPoint.getY(), endPoint.getY());

        return (float) Math.sqrt(Math.pow(maxX - minX, 2) + Math.pow(maxY - minY, 2));
    }

    public void Draw(ShapeRenderer sr) {
        sr.line(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    }

    public float FindY(float X) {
        return startPoint.getY() + ((X - startPoint.getX()) * (float)Math.tan(angle));
    }
    private float radians(float angle) {
        return (float) (angle * Math.PI / 180);
    }
}
