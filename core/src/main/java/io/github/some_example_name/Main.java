package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

import static com.badlogic.gdx.Gdx.input;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    Level level;
    ShapeRenderer sr;
    LineSegment BaseLine;
    Shape base;
    Background background;
    List<Rect> baseRects;

    @Override
    public void create() {
        level = new Level();
        sr = new ShapeRenderer();
        BaseLine = new LineSegment(new FloatPoint(0, 200), new FloatPoint(GameData.getInstance().getScreenWidth(), 200));
        base = new Rect(0, 0, GameData.getInstance().getScreenWidth(), 200, new Color(0.2f, 0.38f, 0.66f, 1f));
        background = new Background();
        baseRects = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            baseRects.add(new Rect(160 * i, 10, 150, 180, new Color(0.12f, 0.28f, 0.51f, 1f)));
        }
        while(background.columns.size() < 20) {
            background.addColumn();
        }

        GameData.getInstance().timers.runRepeating(0.5f, 0.1f, () -> level.player.AddToTrail());

        level.player.setStartingPosition(730,200);

        level.player.ReCalcSolidPoints();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.2f, 0.38f, 0.66f, 1f);
        GameData.getInstance().Maintenance();

        if (input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }

        if (!GameData.getInstance().isStop()) {
            // Background and Base Movement
            for (Rect rect : baseRects) {
                rect.MoveX(GameData.getInstance().getBackgroundBaseSpeed());
            }
            if (baseRects.get(0).getX() < -baseRects.get(0).getWidth()) {
                baseRects.remove(0);
                baseRects.add(new Rect(1770, 10, 150, 180, new Color(0.12f, 0.28f, 0.51f, 1f)));
            }
            background.MoveX(-GameData.getInstance().getBackgroundSpeed());
            for (int i = background.columns.size() - 1; i >= 0; i--) {
                if (!background.columns.get(i).onScreen()) {
                    background.columns.remove(i);
                }
            }
            if (background.columns.size() < 20) {
                background.addColumn();
            }

            level.PlayerMaintenance();
        }


        sr.begin(ShapeRenderer.ShapeType.Filled);

        base.Draw(sr);
        baseRects.forEach(rect -> rect.Draw(sr));
        background.Draw(sr);

        level.playerAmmo.forEach(ammo -> ammo.Draw(sr));

        sr.end();

        sr.begin(ShapeRenderer.ShapeType.Line);

        sr.setColor(Color.WHITE);
        BaseLine.Draw(sr);

        sr.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        sr.begin(ShapeRenderer.ShapeType.Filled);

        for (Rect rect : level.player.trail) {
            rect.Draw(sr);
        }
        level.player.Draw(sr);

        sr.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }
}
