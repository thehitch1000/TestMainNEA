package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;

public class Monster {
    Shape shape, healthShape;

    public Monster() {
        shape = new Polygon(4, Color.RED, 0.5f);
        healthShape = new Polygon(4, Color.RED);
    }
}
