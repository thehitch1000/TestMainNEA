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
        STARTMENU, PLAYING, PAUSED, DEADMENU, ENDING, LEVELSTATUSMENU
    }

    ShapeRenderer sr;
    SpriteBatch batch;
    BitmapFont font;

    Menu startMenu;
    Menu pauseMenu;
    Menu endingMenu;
    Menu levelStatusMenu;

    Level level;
    Stage stage;

    List<Runnable> start1 = new ArrayList<Runnable>() {{
        add(() -> startMenu.Close());
        add(() -> stage = Stage.LEVELSTATUSMENU);
        add(() -> levelStatusMenu.Open());
        add(() -> level.setCurrentFileName("obstacles1"));
    }};
    List<Runnable> start2 = new ArrayList<Runnable>() {{
        add(() -> startMenu.Close());
        add(() -> stage = Stage.LEVELSTATUSMENU);
        add(() -> levelStatusMenu.Open());
        add(() -> level.setCurrentFileName("obstacles2"));
    }};
    List<Runnable> start3 = new ArrayList<Runnable>() {{
        add(() -> startMenu.Close());
        add(() -> stage = Stage.LEVELSTATUSMENU);
        add(() -> levelStatusMenu.Open());
        add(() -> level.setCurrentFileName("obstacles3"));
    }};
    List<Runnable> start4 = new ArrayList<Runnable>() {{
        add(() -> startMenu.Close());
        add(() -> stage = Stage.LEVELSTATUSMENU);
        add(() -> levelStatusMenu.Open());
        add(() -> level.setCurrentFileName("obstacles4"));
    }};
    List<Runnable> start5 = new ArrayList<Runnable>() {{
        add(() -> startMenu.Close());
        add(() -> stage = Stage.LEVELSTATUSMENU);
        add(() -> levelStatusMenu.Open());
        add(() -> level.setCurrentFileName("obstacles5"));
    }};
    List<Runnable> start6 = new ArrayList<Runnable>() {{
        add(() -> startMenu.Close());
        add(() -> stage = Stage.LEVELSTATUSMENU);
        add(() -> levelStatusMenu.Open());
        add(() -> level.setCurrentFileName("obstacles6"));
    }};
    List<Runnable> start7 = new ArrayList<Runnable>() {{
        add(() -> Gdx.app.exit());
    }};

    List<Runnable> pause1 = new ArrayList<Runnable>() {{
        add(() -> pauseMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> GameData.getInstance().timers.runAfter(0.1f, () -> GameData.getInstance().setStop(false)));
    }};
    List<Runnable> pause2 = new ArrayList<Runnable>() {{
        add(() -> Gdx.app.exit());
    }};

    List<Runnable> ending1 = new ArrayList<Runnable>() {{
       add(() -> endingMenu.Close());
       add(() -> stage = Stage.STARTMENU);
       add(() -> startMenu.Open());
    }};
    List<Runnable> ending2 = new ArrayList<Runnable>() {{
        add(() -> level.ResetLevel());
        add(() -> endingMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> level.setUpLevel(false));
    }};
    List<Runnable> ending3 = new ArrayList<Runnable>() {{
        add(() -> Gdx.app.exit());
    }};

    List<Runnable> ResetLevel = new ArrayList<Runnable>() {{
        add(() -> levelStatusMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> level.setUpLevel(true));
    }};
    List<Runnable> KeepLevel = new ArrayList<Runnable>() {{
        add(() -> levelStatusMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> level.setUpLevel(false));
    }};

    int a = 0;

    @Override
    public void create() {
        sr = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        startMenu = new Menu();
        pauseMenu = new Menu();
        endingMenu = new Menu();
        levelStatusMenu = new Menu();

        level = new Level();
        stage = Stage.STARTMENU;

        startMenu.AddButton(new Button("Level 1", start1, 375, 600, font));
        startMenu.AddButton(new Button("Level 2", start2, 750, 600, font));
        startMenu.AddButton(new Button("Level 3", start3, 1225, 600, font));
        startMenu.AddButton(new Button("Level 4", start4, 375, 400, font));
        startMenu.AddButton(new Button("Level 5", start5, 750, 400, font));
        startMenu.AddButton(new Button("Level 6", start6, 1225, 400, font));
        startMenu.AddButton(new Button("Leave", start7, 750, 200, font));
        startMenu.Open();

        pauseMenu.AddButton(new Button("Resume", pause1, 750, 500, font));
        pauseMenu.AddButton(new Button("Leave", pause2, 750, 300, font));
        pauseMenu.Close();

        endingMenu.AddButton(new Button("Return To Home", ending1, 375, 250, font));
        endingMenu.AddButton(new Button("Restart", ending2, 750, 250, font));
        endingMenu.AddButton(new Button("Leave", ending3, 1125, 250, font));
        endingMenu.AddBody(new Body(750));
        endingMenu.Close();

        levelStatusMenu.AddButton(new Button("Reset Level", ResetLevel, 750, 500, font));
        levelStatusMenu.AddButton(new Button("Keep Level", KeepLevel, 750, 300, font));
        levelStatusMenu.Close();
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

                if (level.CheckLevelEnd()) {
                    stage = Stage.ENDING;
                    GameData.getInstance().setStop(true);
                    endingMenu.Open();
                    endingMenu.body.ClearLines();
                    endingMenu.body.AddLine("Your Time: " + (int) (GameData.getInstance().getElapsedTime() - level.getStartTime()) / 1000f + "s", 600, font);
                }

                if (level.player.getLives() <= 0 || level.player.CheckHealth()) {
                    stage = Stage.DEADMENU;
                    GameData.getInstance().setStop(true);
                    endingMenu.Open();
                    endingMenu.body.ClearLines();
                    endingMenu.body.AddLine("You Lost!", 600, font);
                }

                while (level.ReadFile());

                if (!GameData.getInstance().isStop()) {
                    level.CheckBackground();

                    level.MovePlayerY(0);
                    level.MoveWorldX();

                    level.MoveMissiles();

                    if (input.isButtonJustPressed(Input.Buttons.LEFT)) {
                        level.CreatePlayerMissile(level.player.midPoint, new FloatPoint(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
                    }

                    if (level.monster.midPoint.getX() < 250) {
                        level.MoveMonsterAlongPath();
                    }

                    level.player.CalcMidPoints();
                    level.monster.CalcMidPoint();
                    level.CheckChangePlayerDirection();
                    level.player.UpdatePoints();

                    level.CheckDisplay();

                    level.CheckObstacleCollision();

                    level.CheckLevelEnd();
                }

                sr.begin(ShapeRenderer.ShapeType.Filled);

                level.background.Draw(sr);

                level.obstacles.forEach(obstacle -> obstacle.Draw(sr));
                level.player.zigTrail.forEach(trail -> trail.Draw(sr));
                level.player.missiles.forEach(missile -> missile.Draw(sr));

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

                level.monster.currentPath.forEach(line -> line.Draw(sr));

                if (a == 1) {
                    level.player.PrintLines(sr);
                }

                sr.end();

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                sr.begin(ShapeRenderer.ShapeType.Filled);

                level.player.Draw(sr);
                level.monster.Draw(sr);

                sr.end();

                Gdx.gl.glDisable(GL20.GL_BLEND);
                break;

            case STARTMENU:
                startMenu.CheckClick();

                startMenu.Draw(sr, batch, font);
                break;
            case PAUSED:
                pauseMenu.CheckClick();

                pauseMenu.Draw(sr, batch, font);
                break;
            case DEADMENU:
            case ENDING:
                endingMenu.CheckClick();

                endingMenu.Draw(sr, batch, font);
                break;
            case LEVELSTATUSMENU:
                levelStatusMenu.CheckClick();

                levelStatusMenu.Draw(sr, batch, font);
                break;
        }
    }
}
