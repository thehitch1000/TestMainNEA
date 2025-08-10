package io.github.some_example_name;

import com.badlogic.gdx.Gdx;

public class GameData {
    private int ScreenWidth, ScreenHeight, PlayerSpeedY, BackgroundBaseSpeed, ObstacleSpeed, TrailSpeed;
    private boolean Stop;
    private float BackgroundSpeed, elapsedTime;

    private static GameData instance = null;

    public static GameData getInstance() {
        if (instance == null) {
            instance = new GameData(0,0,-8,0.25f,-8,-2,false, Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        }
        return instance;
    }

    public GameData(float elapsedTime,
                    int PlayerSpeedY, int BackgroundBaseSpeed, float BackgroundSpeed, int ObstacleSpeed, int TrailSpeed,
                    boolean Stop,
                    int ScreenWidth, int ScreenHeight) {
        this.Stop = Stop;
        this.elapsedTime = elapsedTime;
        this.PlayerSpeedY = PlayerSpeedY;
        this.BackgroundBaseSpeed = BackgroundBaseSpeed;
        this.BackgroundSpeed = BackgroundSpeed;
        this.ObstacleSpeed = ObstacleSpeed;
        this.TrailSpeed = TrailSpeed;
        this.ScreenWidth = ScreenWidth;
        this.ScreenHeight = ScreenHeight;
    }

    public void setStop(boolean stop) {
        this.Stop = stop;
    }
    public boolean isStop() {
        return Stop;
    }

    public void setElapsedTime(float elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    public float getElapsedTime() {
        return elapsedTime;
    }

    public int getPlayerSpeedY() {
        return PlayerSpeedY;
    }
    public void setPlayerSpeedY(int playerSpeed) {
        this.PlayerSpeedY = playerSpeed;
    }

    public int getBackgroundBaseSpeed() {
        return BackgroundBaseSpeed;
    }
    public void setBackgroundBaseSpeed(int backgroundBaseSpeed) {
        this.BackgroundBaseSpeed = backgroundBaseSpeed;
    }

    public float getBackgroundSpeed() {
        return BackgroundSpeed;
    }
    public void setBackgroundSpeed(float backgroundSpeed) {
        this.BackgroundSpeed = backgroundSpeed;
    }

    public int getObstacleSpeed() {
        return ObstacleSpeed;
    }
    public void setObstacleSpeed(int obstacleSpeed) {
        this.ObstacleSpeed = obstacleSpeed;
    }

    public int getTrailSpeed() {
        return TrailSpeed;
    }
    public void setTrailSpeed(int trailSpeed) {
        this.TrailSpeed = trailSpeed;
    }

    public int getScreenWidth() {
        return ScreenWidth;
    }
    public int getScreenHeight() {
        return ScreenHeight;
    }

    public void setDefaultSpeeds() {
        setBackgroundBaseSpeed(-8);
        setBackgroundSpeed(0.25f);
        setObstacleSpeed(-8);
        setTrailSpeed(-2);
    }
    public void setSpeedMulti(float multi) {
        setBackgroundBaseSpeed((int) (-8 * multi));
        setBackgroundSpeed(0.25f * multi);
        setObstacleSpeed((int) (-8 * multi));
        setTrailSpeed((int) (-2 * multi));
    }
    public void setAllSpeedsToZero() {
        setBackgroundBaseSpeed(0);
        setBackgroundSpeed(0);
        setObstacleSpeed(0);
        setTrailSpeed(0);
        setPlayerSpeedY(0);
    }
}
