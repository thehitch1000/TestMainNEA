package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Spike extends Obstacle{
    private int[] x,y;

    public Spike() {
        this.x = new int[4];
        this.y = new int[4];
    }

    public void Draw(ShapeRenderer sr) {
        sr.triangle(x[0], y[0], x[1], y[1], x[2], y[2]);
        sr.triangle(x[2], y[2], x[3], y[3], x[0], y[0]);
    }
}
