package io.github.some_example_name;

public class Player extends Entity {


    public enum Shape {
        SQUARE, ARROW,
    }
    public enum State {
        JUMPING, FALLING, IDLE, DEAD, RESPAWNING
    }

    public Player() {
        type = Type.PLAYER;
        points = new FloatPoint[4];
    }
}
