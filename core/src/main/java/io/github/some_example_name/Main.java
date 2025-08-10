package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.ScreenUtils;

import static com.badlogic.gdx.Gdx.input;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    Level level;
    Obstacle[] obstacles;

    @Override
    public void create() {
        level = new Level();

        level.player.points[0].setPoint(100, 100);

    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        if (input.isKeyPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

    }
}
