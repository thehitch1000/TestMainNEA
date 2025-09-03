package io.github.some_example_name;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Monster {
    Shape shape, healthShape;

    public Monster() {
        this.shape = new Polygon(4, Color.RED, 0.5f);
        this.healthShape = null;
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
        if (healthShape != null) {
            healthShape.Draw(sr);
        }
    }
}
