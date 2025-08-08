package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Box extends Obstacle {
    private int x, y, width, height;

    public Box() {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    public void Draw(ShapeRenderer sr) {
        sr.rect(x, y, width, height);
    }
}
