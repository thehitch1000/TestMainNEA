package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public interface IntAmmo {
    void Draw(ShapeRenderer sr);
    void MoveAlongPath();
}

abstract class Ammo implements IntAmmo {
    protected Shape shape;
    protected List<LineSegment> path;
    protected float speed;

    public void Draw(ShapeRenderer sr){}
    public void MoveAlongPath(){}
}

class Bullet extends Ammo {
    FloatPoint startPoint, endPoint;

    public Bullet(float Speed, FloatPoint startPoint, FloatPoint endPoint) {
        shape = new Tri(new FloatPoint[3]);
        path = new ArrayList<>();

        speed = Speed;

        this.startPoint = startPoint;
        this.endPoint = endPoint;

        // Initialize triangle points at the start position
        shape.points[0] = new FloatPoint(startPoint.getX(), startPoint.getY());
        shape.points[1] = new FloatPoint(startPoint.getX() - 10, startPoint.getY() - 10);
        shape.points[2] = new FloatPoint(startPoint.getX() + 10, startPoint.getY() - 10);

        // Build first path segment and orient the bullet
        path.add(new LineSegment(this.startPoint, this.endPoint));
        float angleRad = path.get(0).getAngle();
        float angleDeg = (float) Math.toDegrees(angleRad);
        shape.Rotate(angleDeg - 90f, shape.points[0]);
    }

    public void setBullet(FloatPoint point) {
        // Ensure path exists before using it and initialize points if needed
        if (shape.points[0] == null) {
            shape.points[0] = new FloatPoint(point.getX(), point.getY());
            shape.points[1] = new FloatPoint(point.getX() - 10, point.getY() - 10);
            shape.points[2] = new FloatPoint(point.getX() + 10, point.getY() - 10);
        } else {
            shape.points[0].setPoint(point.getX(), point.getY());
            shape.points[1].setPoint(point.getX() - 10, point.getY() - 10);
            shape.points[2].setPoint(point.getX() + 10, point.getY() - 10);
        }

        if (path.isEmpty()) {
            path.add(new LineSegment(startPoint, endPoint));
        }

        // Rotate the bullet to align with its movement direction (Rotate expects degrees)
        float angleRad = path.get(0).getAngle();
        float angleDeg = (float) Math.toDegrees(angleRad);
        shape.Rotate(angleDeg - 90f, point);
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }

    public void MoveAlongPath() {
        // Guard against uninitialized path or shape
        if (path.isEmpty() || shape == null || shape.points == null || shape.points.length < 3 || shape.points[0] == null) {
            return;
        }
        float angleRad = path.get(0).getAngle();
        FloatPoint tempPoint = new FloatPoint(
            shape.points[0].getX() + (speed * (float) Math.cos(angleRad)),
            shape.points[0].getY() + (speed * (float) Math.sin(angleRad))
        );
        if (path.get(0).isPointInSegment(tempPoint)) {
            shape.MoveX((float) Math.cos(angleRad) * speed);
            shape.MoveY((float) Math.sin(angleRad) * speed);
        } else {
            shape.MoveX(path.get(0).endPoint.getX() - shape.points[0].getX());
            shape.MoveY(path.get(0).endPoint.getY() - shape.points[0].getY());
        }

    }
}

class Missile extends Ammo {

    Circle shape;
    LineSegment[] path;
    FloatPoint startPoint, endPoint;
    private float speed;

    public Missile(FloatPoint startPoint, FloatPoint endPoint, float speed) {
        this.speed = speed;
        this.startPoint = startPoint;
        this.endPoint = endPoint;

        this.shape = new Circle(startPoint.getX(), startPoint.getY(), 10);

        this.path = new LineSegment[10];
    }
    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }

    public void MoveX(float X) {
        shape.MoveX(X);
    }
    public void MoveY(float Y) {
        shape.MoveY(Y);
    }

    public float getSpeed() {
        return speed;
    }
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void setPath(LineSegment[] path) {
        this.path = path;
    }

    public void PrintPath(ShapeRenderer sr) {
        for (int i = 0; i < path.length; i++) {
            sr.line(path[i].startPoint.getX(), path[i].startPoint.getY(), path[i].endPoint.getX(), path[i].endPoint.getY());
        }
    }
}
