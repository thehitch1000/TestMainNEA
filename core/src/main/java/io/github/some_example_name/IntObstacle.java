package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface IntObstacle() {
    void Draw(ShapeRenderer sr);
    void MoveX(float X);
    void MoveY(float Y);
}

abstract class Obstacle implements IntObstacle {
    protected Shape shape;

    public void MoveX(float X) {
        shape.MoveX(X);
    }
    public void MoveY(float Y) {
        shape.MoveY(Y);
    }
    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }
}

class Box extends Obstacle {

    public Box(float width, float height) {
        shape = new Rect(false, width, height);
    }
}

class Spike extends Obstacle {

    public Spike(float width, float height) {
        shape = new Tri(width, height);
    }
}