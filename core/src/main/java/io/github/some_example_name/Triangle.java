package io.github.some_example_name;

public class Triangle extends Shape {
    private FloatPoint[] points;

    public Triangle(float x, float y, float width, float height) {
        type = Type.TRIANGLE;
        this.points = new FloatPoint[3];
        this.points[0] = new FloatPoint(x, y);
        this.points[1] = new FloatPoint(x + width, y);
        this.points[2] = new FloatPoint(x + (width/2), y + height);
    }

    public void MoveX(float X) {
        for (FloatPoint point : points) {
            point.MoveX(X);
        }
    }
}
