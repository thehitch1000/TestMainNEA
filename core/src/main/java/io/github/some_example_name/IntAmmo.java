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
    }

    public void setBullet(FloatPoint point) {
        shape.points[0].setPoint(point.getX(), point.getY());
        shape.points[1].setPoint(point.getX() - 10, point.getY() - 10);
        shape.points[2].setPoint(point.getX() + 10, point.getY() - 10);

        // Rotate the bullet to align with its movement direction
        shape.Rotate(path.get(0).getAngle() - (float)(Math.PI / 2), point);
m
        path.add(new LineSegment(startPoint, endPoint));
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }

    public void MoveAlongPath() {
        FloatPoint tempPoint = new FloatPoint(shape.points[2].getX() + (speed * CosValue(Radians(path.get(0).getAngle()))), shape.points[2].getY() + (speed * SinValue(Radians(path.get(0).getAngle()))));
        if (path.get(0).isPointInSegment(tempPoint)) {
            shape.MoveX(CosValue(Radians(path.get(0).getAngle())) * speed);
            shape.MoveY(SinValue(Radians(path.get(0).getAngle())) * speed);
        } else {
            shape.MoveX(path.get(0).endPoint.getX() - shape.points[2].getX());
            shape.MoveY(path.get(0).endPoint.getY() - shape.points[2].getY());
        }

    }
    private float CosValue(float radians) {
        return (float) Math.cos(radians);
    }
    private float SinValue(float radians) {
        return (float) Math.sin(radians);
    }
    private float Radians(float angle) {
        return (float) (angle * (Math.PI / 180));
    }
}

class missile extends Ammo {

    public missile() {
        shape = new Circle(0);
        path = new ArrayList<>();
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }

    public void MoveAlongPath() {
        Circle c = (Circle) shape;
        float progress = 1f;
        while (progress > 0) {
            if (path.isEmpty()) break;
            LineSegment line = path.get(0);
            FloatPoint tempPoint = new FloatPoint(c.getX() + (speed * progress * CosValue(Radians(line.getAngle()))), c.getY() + (speed * progress * SinValue(Radians(line.getAngle()))));
            if (line.isPointInSegment(tempPoint)) {
                c.MoveX(CosValue(Radians(line.getAngle())) * speed * progress);
                c.MoveY(SinValue(Radians(line.getAngle())) * speed * progress);
                progress = 0;
            } else {
                Vector2 Diff = new Vector2(line.endPoint.getX() - c.getX(), line.endPoint.getY() - c.getY());
                float multi = Diff.x / (speed * CosValue(Radians(line.getAngle())));
                if (multi > progress) multi = progress;
                c.MoveX(CosValue(Radians(line.getAngle())) * multi * speed);
                c.MoveY(SinValue(Radians(line.getAngle())) * multi * speed);
                progress -= multi;
                path.remove(0);
            }
        }
    }
    private float CosValue(float radians) {
        return (float) Math.cos(radians);
    }
    private float SinValue(float radians) {
        return (float) Math.sin(radians);
    }
    private float Radians(float angle) {
        return (float) (angle * (Math.PI / 180));
    }
}
