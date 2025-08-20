package io.github.some_example_name;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public interface IntObstacle() {
    
}

abstract class Obstacle implements IntObstacle {
    protected Shape shape;
}

class Box extends Obstacle {

    public Box() {
    
    }
}

class Spike extends Obstacle {
}