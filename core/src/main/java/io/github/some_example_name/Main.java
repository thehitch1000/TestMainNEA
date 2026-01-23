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
import com.badlogic.gdx.math.Polygon;

import java.awt.*;
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
        add(() -> level.CloseProgram());
    }};

    List<Runnable> pause1 = new ArrayList<Runnable>() {{
        add(() -> pauseMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> level.UnpauseLevel());
        add(() -> GameData.getInstance().timers.runAfter(0.1f, () -> GameData.getInstance().setStop(false)));
    }};
    List<Runnable> pause2 = new ArrayList<Runnable>() {{
        add(() -> level.CloseProgram());
    }};
    List<Runnable> pause3 = new ArrayList<Runnable>() {{
        add(() -> pauseMenu.Close());
        add(() -> level.ResetLevel());
        add(() -> stage = Stage.STARTMENU);
        add(() -> startMenu.Open());
    }};

    List<Runnable> ReturnToHome = new ArrayList<Runnable>() {{
       add(() -> endingMenu.Close());
       add(() -> level.ResetLevel());
       add(() -> stage = Stage.STARTMENU);
       add(() -> startMenu.Open());
    }};
    List<Runnable> Restart = new ArrayList<Runnable>() {{
        add(() -> level.ResetLevel());
        add(() -> endingMenu.Close());
        add(() -> stage = Stage.PLAYING);
        add(() -> level.setUpLevel(false));
    }};
    List<Runnable> Leave = new ArrayList<Runnable>() {{
        add(() -> level.CloseProgram());
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

        level.OpenProgram();

        startMenu.AddButton(new Button("Level 1", start1, 375, 600, font));
        startMenu.AddButton(new Button("Level 2", start2, 750, 600, font));
        startMenu.AddButton(new Button("Level 3", start3, 1225, 600, font));
        startMenu.AddButton(new Button("Level 4", start4, 375, 400, font));
        startMenu.AddButton(new Button("Level 5", start5, 750, 400, font));
        startMenu.AddButton(new Button("Level 6", start6, 1225, 400, font));
        startMenu.AddButton(new Button("Leave", start7, 750, 200, font));
        startMenu.Open();

        pauseMenu.AddButton(new Button("Resume", pause1, 750, 600, font));
        pauseMenu.AddButton(new Button("Return To Home", pause3, 750, 400, font));
        pauseMenu.AddButton(new Button("Leave", pause2, 750, 200, font));
        pauseMenu.Close();

        endingMenu.AddButton(new Button("Return To Home", ReturnToHome, 375, 250, font));
        endingMenu.AddButton(new Button("Restart", Restart, 750, 250, font));
        endingMenu.AddButton(new Button("Leave", Leave, 1125, 250, font));
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

        level.MoveMissiles();

        switch (stage) {
            case PLAYING:
                if (input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    stage = Stage.PAUSED;
                    level.PauseLevel();
                    pauseMenu.Open();
                }

                if (level.CheckLevelEnd()) {
                    stage = Stage.ENDING;
                    level.PauseLevel();
                    endingMenu.Open();
                    endingMenu.body.ClearLines();
                    endingMenu.body.AddLine("Your Time: " + (int) level.getTotalLevelTime() / 1000f + "s", 600, font);
                }

                if (level.player.getLives() <= 0 || level.player.CheckHealth()) {
                    stage = Stage.DEADMENU;
                    level.PauseLevel();
                    endingMenu.Open();
                    endingMenu.body.ClearLines();
                    endingMenu.body.AddLine("You Lost!", 600, font);
                }

                if (input.isKeyJustPressed(Input.Keys.P)) {
                    GameData.getInstance().setStop(!GameData.getInstance().isStop());
                    level.CreateMonsterMissile();
                }

                if (input.isKeyJustPressed(Input.Keys.R)) {
                    ThetaStarProcessor stepper = new ThetaStarProcessor(Level.TypeOfPath.MONSTERMISSILE, level.monster.midPoint, level.finalPoint, level, false);

                    stepper.FindPath();

                    level.ResetGridNodes();
                }

                if (!GameData.getInstance().isStop()) {
                    while (level.ReadFile());

                    level.CheckBackground();

                    level.MovePlayerY(0);
                    level.MoveWorldX();

                    if (input.isButtonJustPressed(Input.Buttons.LEFT)) {
                        level.CreatePlayerMissile(level.player.midPoint, new FloatPoint(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()));
                    }

                    if (level.monster.midPoint.getX() <= 300) {
                        level.MoveMonsterAlongPath();
                    }

                    level.player.CalcMidPoints();
                    level.monster.CalcMidPoint();
                    level.CheckChangePlayerDirection();
                    level.player.UpdatePoints();

                    level.CheckTrailPositioningDisplay();

                    level.CheckZonesOnScreen();

                    level.CheckObstacleCollision();
                }

                sr.begin(ShapeRenderer.ShapeType.Filled);

//                level.background.Draw(sr);
//
//                level.barriers.forEach(obstacle -> obstacle.Draw(sr));
//                level.player.zigTrail.forEach(trail -> trail.Draw(sr));
//                level.player.missiles.forEach(missile -> missile.Draw(sr));

                if (input.isKeyPressed(Input.Keys.N)) {
                    for (Node[] node : level.grid) {
                        for (Node n : node) {
                            n.Draw(sr);
                        }
                    }
                }

                sr.end();

                sr.begin(ShapeRenderer.ShapeType.Line);

                level.monster.currentPath.forEach(line -> line.Draw(sr));

                if (!level.monster.missiles.isEmpty()) {
                    for(LineSegment line : level.monster.missiles.get(0).path) {
                        line.Draw(sr);
                    }
                }


                sr.setColor(Color.PURPLE);

                if (input.isKeyPressed(Input.Keys.L)) {
                    for (Polygon polygon : level.shapes) {
                        for (int i = 0; i < polygon.getVertices().length; i += 2) {
                            sr.line(polygon.getVertices()[i], polygon.getVertices()[i + 1], polygon.getVertices()[Math.floorMod(i + 2, polygon.getVertices().length)], polygon.getVertices()[Math.floorMod(i + 3, polygon.getVertices().length)]);
                        }
                    }
                }

                sr.setColor(Color.WHITE);
                if (level.scoutGhost.getDirectionChangeScalar() != 0) sr.line(0, level.scoutGhost.currentMoveLine.FindY(0), GameData.getInstance().getScreenWidth(), level.scoutGhost.currentMoveLine.FindY(GameData.getInstance().getScreenWidth()));

                sr.end();

                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                sr.begin(ShapeRenderer.ShapeType.Filled);

                level.player.Draw(sr);
                level.monster.Draw(sr);

                level.zones.forEach(zone -> {
                    if (zone.getType() == Zone.Type.CHANGEDIRE) {
                        zone.Draw(sr);
                    }
                });

                sr.end();

                Gdx.gl.glDisable(GL20.GL_BLEND);

                sr.begin(ShapeRenderer.ShapeType.Filled);

                if (level.finalPoint.getX() != 0) {
                    sr.setColor(Color.RED);
                    sr.circle(level.finalPoint.getX(), level.finalPoint.getY(), 5);
                } else {
                    sr.setColor(Color.GREEN);
                    sr.circle(level.scoutGhost.center.getX(), level.scoutGhost.center.getY(), 5);
                }

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
