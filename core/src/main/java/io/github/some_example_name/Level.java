package io.github.some_example_name;

public class Level {
    Player player;
    Monster monster;

    public Level() {
        player = new Player(40);
        monster = new Monster();
    }
}
