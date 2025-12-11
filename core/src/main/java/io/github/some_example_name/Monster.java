package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.util.ArrayList;
import java.util.List;

public class Monster {
    private float speed;
    private boolean awake;
    List<LineSegment> currentPath;
    List<Missile> missiles;
    Shape shape, healthShape;
    FloatPoint midPoint;


    public Monster() {
        shape = new Rect(40, 40, Color.RED);
        healthShape = new Rect(40, 40, Color.RED);

        currentPath = new ArrayList<>();
        missiles = new ArrayList<>();

        this.speed = 8;
        this.awake = false;
    }

    public void PrintPath(ShapeRenderer sr) {
        for (LineSegment line : currentPath) {
            line.Draw(sr);
        }
    }

    public float getSpeed() {
        return speed;
    }

    public void setAwake(boolean awake) {
        this.awake = awake;
    }

    public void setNewPath(ArrayList<LineSegment> path) {
        this.currentPath = path;
    }

    public void setPosition(float x, float y) {
        shape.setShape(x, y);
        healthShape.setShape(x, y);
    }

    public void CalcMidPoint() {
        midPoint = new FloatPoint(shape.getX() + shape.getWidth()/2, shape.getY() + shape.getHeight()/2);
    }

    public void Draw(ShapeRenderer sr) {
        if (awake) {
            shape.Draw(sr);
            healthShape.Draw(sr);
        }
    }

    public void MoveX(float x) {
        shape.MoveX(x);
        healthShape.MoveX(x);
    }
    public void MoveY(float y) {
        shape.MoveY(y);
        healthShape.MoveY(y);
    }
}
