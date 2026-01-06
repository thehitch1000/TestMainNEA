package io.github.some_example_name;

public class LineEquation {
    private float Gradient, YIntercept;
    private LineDirection direction;

    public enum LineDirection {
        HORIZONTAL, DIAGONAL, VERTICAL
    }

    public LineEquation(float Gradient, float YIntercept, LineDirection direction) {
        this.Gradient = Gradient;
        this.YIntercept = YIntercept;
        this.direction = direction;
    }
    public LineEquation(FloatPoint point1, FloatPoint point2) {
        CalcLine(point1, point2);
        if (point1.getY() == point2.getY()) {
            direction = LineDirection.HORIZONTAL;
        } else if (point1.getX() == point2.getX()) {
            direction = LineDirection.VERTICAL;
        } else {
            direction = LineDirection.DIAGONAL;
        }
    }
    public LineEquation(float Grad, FloatPoint point) {
        this(Grad, point.getY() - (Grad * point.getX()), LineDirection.DIAGONAL);
    }

    public float getGradient() {
        return Gradient;
    }
    public void setGradient(float Gradient) {
        this.Gradient = Gradient;
    }

    public float getYIntercept() {
        return YIntercept;
    }
    public void setYIntercept(float YIntercept) {
        this.YIntercept = YIntercept;
    }

    public LineDirection getDirection() {
        return direction;
    }
    public void setDirection(LineDirection horizontal) {
        this.direction = horizontal;
    }

    public float FindX(float y) {
        return (y - YIntercept) / Gradient;
    }
    public float FindY(float x) {
        return (Gradient * x) + YIntercept;
    }

    public void MoveX(float X) {
        if (direction == LineDirection.DIAGONAL) {
            YIntercept -= (Gradient * X);
        }
    }
    public void MoveY(float Y) {
        if (direction == LineDirection.DIAGONAL) {
            YIntercept += Y;
        }
    }

    public void setLine(float Grad, float YIntercept) {
        this.Gradient = Grad;
        this.YIntercept = YIntercept;
    }

    public void CalcLine(FloatPoint point1, FloatPoint point2) {
        CalcGradient(point1, point2);
        CalcYIntercept(point1);
    }
    public void CalcGradient(FloatPoint point1, FloatPoint point2) {
        Gradient = (point2.getY() - point1.getY()) / (point2.getX() - point1.getX());
    }
    public void CalcYIntercept(FloatPoint point1) {
        YIntercept = point1.getY() - (Gradient * point1.getX());
    }
}
