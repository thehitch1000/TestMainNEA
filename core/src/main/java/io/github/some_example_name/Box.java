package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Box extends Obstacle {
    private int x, y, width, height;

    public Box() {
        type = Type.BOX;
    }
}
