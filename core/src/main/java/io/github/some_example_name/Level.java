package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

public class Level {
    Player player;
    Monster monster;
    ShapeBuffer[] obstacles;

    public Level() {
        player = new Player(40);
        monster = new Monster();
    }



    public void AddBox() {

    }
    public void AddSpike() {

    }

    public void CheckSlowDown() {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            GameData.getInstance().setSpeedMulti(0.5f);
        } else {
            GameData.getInstance().setDefaultSpeeds();
        }
    }
}
