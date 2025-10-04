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
        STARTMENU, PLAYING, PAUSED, DEADMENU, ENDING
    }

    ShapeRenderer sr;
    SpriteBatch batch;
    BitmapFont font;

    Menu startMenu;
    Menu pauseMenu;
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

    List<Runnable> pause1 = new ArrayList<Runnable>() {{
        add(() -> pauseMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> GameData.getInstance().timers.runAfter(0.5f, () -> GameData.getInstance().setStop(false)));
    }};
    List<Runnable> pause2 = new ArrayList<Runnable>() {{
        add(() -> Gdx.app.exit());
    }};

    int a = 0;

    @Override
    public void create() {
        GameData.getInstance().EmptyFile("obstacles.txt");
        GameData.getInstance().EmptyFile("tempObstacles.txt");

        sr = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        startMenu = new Menu();
        pauseMenu = new Menu();
        level = new Level();
        stage = Stage.STARTMENU;

        startMenu.AddButton(new Button("Play Normal Level", start1, 750, 600, font));
        startMenu.AddButton(new Button("Play ZigZag Level", start2, 750, 400, font));
        startMenu.AddButton(new Button("Leave", start3, 750, 200, font));
        startMenu.Open();

        pauseMenu.AddButton(new Button("Resume", pause1, 750, 500, font));
        pauseMenu.AddButton(new Button("Leave", pause2, 750, 300, font));
        pauseMenu.Close();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.2f, 0.38f, 0.66f, 1f);
        GameData.getInstance().Maintenance();

        if (input.isKeyJustPressed(Input.Keys.F1)) {
            a = (a + 1) % 2;
        }
        if (input.isKeyJustPressed(Input.Keys.F2)) {
            Gdx.app.exit();
        }

        switch (stage) {
            case PLAYING:
                if (input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    stage = Stage.PAUSED;
                    GameData.getInstance().setStop(true);
                    pauseMenu.Open();
                }

                while (level.ReadFile());


                if (!GameData.getInstance().isStop()) {
                    level.CheckBackground();

                    if (level.stage == Level.levelStage.NORMAL) {
                        level.Update();
                        level.Checking();
                        level.Move();
                    } else {
                        GameData.getInstance().setStop(true);

                        level.MovePlayerY(0);
                        level.MoveWorldX();

//                        GameData.getInstance().timers.runAfter(1, () -> level.MoveMonsterAlongPath());

                        level.player.CalcMidPoints();
                        level.CheckChangePlayerDirection();
                        level.player.UpdatePoints();

                        level.CheckDisplay();

                        level.CheckObstacleCollision();
                    }
                }

                level.MoveMissiles();

                if (input.isButtonJustPressed(Input.Buttons.LEFT)) {
                    level.CreatePlayerMissile(level.player.midPoint, new FloatPoint(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
                }

                sr.begin(ShapeRenderer.ShapeType.Filled);

                level.base.Draw(sr);
                level.baseRects.forEach(rect -> rect.Draw(sr));
                level.background.Draw(sr);

                level.obstacles.forEach(obstacle -> obstacle.Draw(sr));
                level.player.zigTrail.forEach(trail -> trail.Draw(sr));
                level.player.ammo.forEach(ammo -> ammo.Draw(sr));

                if (input.isKeyPressed(Input.Keys.P)) {
                    for (Node[] gridRow : level.grid) {
                        for (Node node : gridRow) {
                            if (node != null) {
                                node.Draw(sr);
                            }
                        }
                    }
                }

                sr.end();

                sr.begin(ShapeRenderer.ShapeType.Line);

                sr.setColor(Color.WHITE);
                level.BaseLine.Draw(sr);

                if (!level.player.ammo.isEmpty()) {
                    level.player.ammo.forEach(ammo -> ammo.PrintPath(sr));
                }

                if (a == 1) {
                    level.player.PrintLines(sr);
                }

                sr.end();

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                sr.begin(ShapeRenderer.ShapeType.Filled);

                level.player.normalTrail.forEach(trail -> trail.Draw(sr));
                level.player.Draw(sr);
                level.monster.Draw(sr);

                sr.end();

                Gdx.gl.glDisable(GL20.GL_BLEND);

                sr.begin(ShapeRenderer.ShapeType.Filled);

                if (input.isKeyPressed(Input.Keys.P)) {
                    for (Node[] gridRow : level.grid) {
                        for (Node node : gridRow) {
                            if (node != null) {
                                node.Draw(sr);
                            }
                        }
                    }
                }

                level.DrawObstacles(sr);

                sr.end();
                break;

            case STARTMENU:
                startMenu.CheckClick();

                startMenu.Draw(sr, batch, font);
                break;
            case PAUSED:
                pauseMenu.CheckClick();

                pauseMenu.Draw(sr, batch, font);
                break;
        }
    }
}
