package io.github.some_example_name;

public class Entity {
    protected Type type;


    public enum Type {
        PLAYER, MONSTER, NULL

    }

    public Entity(Type type) {
        this.type = Type.NULL;
    }

    public Type getType() {
        return type;
    }
}
