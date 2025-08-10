package io.github.some_example_name;

public class Obstacle {
    protected Type type;
    public FloatPoint[] points;

    public Obstacle() {
        this.type = Type.NULL;
    }

    public enum Type {
        SPIKE, BOX, SQUARE, NULL
    }

    public Type getType() {
        return type;
    }
}
