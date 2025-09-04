package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.input;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    public enum Stage {
        MENUS, PLAYING, PAUSED, DEADMENU, ENDING
    }

    ShapeRenderer sr;
    SpriteBatch batch;
    BitmapFont font;

    Menu startMenu;
    Level level;
    Stage stage;

    List<Runnable> start1 = new ArrayList<Runnable>() {{
        add(() -> startMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> level.setUpLevel(Level.levelStage.NORMAL));
    }};
    List<Runnable> start2 = new ArrayList<Runnable>() {{
        add(() -> startMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> level.setUpLevel(Level.levelStage.ZIGZAG));
    }};
    List<Runnable> start3 = new ArrayList<Runnable>() {{
        add(() -> Gdx.app.exit());
    }};

    @Override
    public void create() {
        GameData.getInstance().EmptyFile("obstacles.txt");
        GameData.getInstance().EmptyFile("tempObstacles.txt");

        sr = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        startMenu = new Menu();
        level = new Level();
        stage = Stage.MENUS;

        startMenu.AddButton(new Button("Play Normal Level", start1, 750, 600, font));
        startMenu.AddButton(new Button("Play ZigZag Level", start2, 750, 400, font));
        startMenu.AddButton(new Button("Leave", start3, 750, 200, font));
        startMenu.Open();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.2f, 0.38f, 0.66f, 1f);
        GameData.getInstance().Maintenance();

        if (input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        switch (stage) {
            case PLAYING:
                while (level.ReadFile());

                if (!GameData.getInstance().isStop()) {
                    level.CheckBackground();

                    level.Update();
                    level.Checking();
                    level.Move();
                }

                sr.begin(ShapeRenderer.ShapeType.Filled);

                level.base.Draw(sr);
                level.baseRects.forEach(rect -> rect.Draw(sr));
                level.background.Draw(sr);

                level.obstacles.forEach(obstacle -> obstacle.Draw(sr));

                sr.end();

                sr.begin(ShapeRenderer.ShapeType.Line);

                sr.setColor(Color.WHITE);
                level.BaseLine.Draw(sr);

                sr.end();

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                sr.begin(ShapeRenderer.ShapeType.Filled);

                for (Rect rect : level.player.normalTrail) {
                    rect.Draw(sr);
                }
                level.player.Draw(sr);

                sr.end();

                Gdx.gl.glDisable(GL20.GL_BLEND);

                sr.begin(ShapeRenderer.ShapeType.Filled);

                level.player.ammo.forEach(ammo -> ammo.Draw(sr));

                sr.end();
                break;

            case MENUS:
                startMenu.CheckClick();

                startMenu.Draw(sr, batch, font);
                break;
        }
    }
}
