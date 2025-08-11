package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Box {
    private Shape shape;

    public Box() {
        this.shape = new Rect(false,200,40);
    }

    public void MoveX(float X) {
        shape.MoveX(X);
    }
    public void MoveY(float Y) {
        shape.MoveY(Y);
    }

    public void setSpike(float x, float y) {
        shape.setShape(x,y);
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }

    public boolean onScreen() {

    }
}
