package io.github.some_example_name;

public class Monster {
    Shape shape;

    public enum State {
        AWAke, DEAD,
    }

    public Monster() {
        shape = new Rect(40, 40);
    }
}

