package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface IntAmmo {
    void Draw(ShapeRenderer sr);
}

abstract class Ammo implements IntAmmo {
    protected Shape shape;
    protected LineSegment[] path;

    public void Draw(ShapeRenderer sr){}

}

class bullet extends Ammo {

    public bullet() {
        shape = new Tri();
        path = new LineSegment[1];
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }
}

class missile extends Ammo {

    public missile(int pathLength) {
        shape = new Circle(0);
        path = new LineSegment[pathLength];
    }

    public void Draw(ShapeRenderer sr) {
        shape.Draw(sr);
    }
}
