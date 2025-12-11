package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;

public class Missile {
    public enum Type {
        PLAYER, MONSTER
    }

    FloatPoint startPoint, endPoint;
    Color colour;
    Shape shape;
    ArrayList<LineSegment> path;
    Type type;
    Level level;
    private float speed, nextFlashTime, explosionRadius, damage;
    private boolean flash;

    public Missile(FloatPoint startPoint, FloatPoint endPoint, float speed, Level level) {
        this.speed = speed;
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.flash = false;
        this.nextFlashTime = 0;
        this.type = null;

        this.level = level;

        shape = new Circle(startPoint.getX(), startPoint.getY(), 10);

        path = new ArrayList<>();
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void Draw(ShapeRenderer sr) {
        flash = (path.size() == 1);
        if (flash) {
            if (GameData.getInstance().getElapsedTime() > nextFlashTime) {
                nextFlashTime = GameData.getInstance().getElapsedTime() + 500;
                if (colour == Color.WHITE) {
                    colour = Color.RED;
                } else {
                    colour = Color.WHITE;
                }
            }
        } else {
            colour = Color.WHITE;
        }
        sr.setColor(colour);
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

    public void setPath(ArrayList<LineSegment> path) {
        this.path = path;
    }

    public void PrintPath(ShapeRenderer sr) {
        for (LineSegment line : path) {
            if (line == null) continue;
            sr.line(line.startPoint.getX(), line.startPoint.getY(), line.endPoint.getX(), line.endPoint.getY());
        }
    }

    public void MoveAlongPath() {
        if (path.isEmpty()) {

            return;
        }

        float angleRad = path.get(0).getAngle();

        float xDifference = path.get(0).endPoint.getX() - shape.getX();
        float yDifference = path.get(0).endPoint.getY() - shape.getY();
        float distanceToEnd = (float) Math.sqrt(Math.pow(xDifference, 2) + Math.pow(yDifference, 2));


        if (distanceToEnd <= speed) {
            shape.MoveX(path.get(0).endPoint.getX() - shape.getX());
            shape.MoveY(path.get(0).endPoint.getY() - shape.getY());
            path.remove(0);
        } else {
            shape.MoveX((float) Math.cos(angleRad) * speed);
            shape.MoveY((float) Math.sin(angleRad) * speed);
        }
    }

    public void Explode() {
        if (type == Type.PLAYER) {

        } else {
            FloatPoint closestPoint = new FloatPoint(Clamp(level.monster.shape.getX(), level.monster.shape.getX() + level.monster.shape.getWidth(), shape.getX()), Clamp(level.monster.shape.getY(), level.monster.shape.getY() + level.monster.shape.getHeight(), shape.getY()));
            float distance = distance(closestPoint, new FloatPoint(shape.getX(), shape.getY()));
            if (distance < explosionRadius) {
                float multi = distance / explosionRadius;
                level.player.TakeDamage( damage * (1 - multi));
            }
        }
    }

    private float Clamp(float min, float max, float value) {
        if (value < min) value = min;
        if (value > max) value = max;
        return value;
    }
    private float distance(FloatPoint a, FloatPoint b) {
        return (float) Math.sqrt(Math.pow(b.getX() - a.getX(), 2) + Math.pow(b.getY() - a.getY(), 2));
    }
}
