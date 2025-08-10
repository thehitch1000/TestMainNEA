package io.github.some_example_name;

public class Monster extends Entity {


    public enum State {
        AWAke, DEAD,
    }

    public Monster() {
        type = Type.MONSTER;
        points = new FloatPoint[4];
    }
}
