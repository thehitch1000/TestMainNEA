package io.github.some_example_name;

public class Entity {
    protected Type type;
    protected FloatPoint[] points;


    public enum Type {
        PLAYER, MONSTER, NULL

    }

    public Entity() {

    }

    public Type getType() {
        return type;
    }

    public void MoveX (float X) {
        for (FloatPoint point : points) {
            point.MoveX(X);
        }
    }
}
